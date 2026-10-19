package io.github.fate_grand_automata.atlas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.github.fate_grand_automata.util.GameServerResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adds servants to a battle config's support selection from the Atlas Academy DB
 * (https://apps.atlasacademy.io/db).
 *
 * The DB serves the same face art the game shows in its support list, so a servant can be added
 * without having to photograph them in-game with the Support Image Maker.
 */
@Singleton
class AtlasSupportServantRepository @Inject constructor(
    private val api: AtlasAcademyApi,
    private val writer: AtlasSupportImageWriter,
    private val serverResolver: GameServerResolver
) {
    /**
     * Servants whose name contains [query], as named on the server the user plays on.
     *
     * The DB only matches names in the language of the region it is asked about, so a miss on the
     * user's own server is retried against the English names: someone playing on CN who types
     * "Waver" would otherwise find nothing.
     */
    suspend fun search(query: String): List<AtlasServant> {
        val name = query.trim()
        if (name.isEmpty()) return emptyList()

        val region = serverResolver.resolve().atlasRegion
        val servants = api.search(region, name)
        if (servants.isNotEmpty() || region == ATLAS_NA_REGION) return servants

        return api.search(ATLAS_NA_REGION, name)
    }

    /** The face art to show next to [servant], preferring the final ascension's. */
    suspend fun face(servant: AtlasServant): Bitmap? = withContext(Dispatchers.IO) {
        val bytes = try {
            api.downloadFace(finalAscensionFace(servant.face))
        } catch (e: AtlasHttpException) {
            if (e.code != HttpURLConnection.HTTP_NOT_FOUND) throw e

            api.downloadFace(servant.face)
        }

        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /**
     * Downloads every face of [servant] and stores it as a support image.
     *
     * Returns the name the servant is now listed under, which is what the support preferences
     * hold. Throws when the DB has no face for the servant, or when downloading fails.
     */
    suspend fun add(servant: AtlasServant): String = withContext(Dispatchers.IO) {
        val (region, faces) = facesOf(servant)
        if (faces.isEmpty()) throw IOException("${servant.name} has no face image")

        val name = identity(servant, region)
        for (url in faces) {
            writer.write(name, faceFileName(url), api.downloadFace(url))
        }

        Timber.i("Added ${faces.size} support image(s) for $name from $region")

        name
    }

    /**
     * The faces of [servant] together with the region they were found on.
     *
     * The server the user plays on is asked first. A servant that isn't released there yet (found
     * by a search that fell back to English) is looked up on NA next, and then on JP, where the
     * game data originates.
     */
    private suspend fun facesOf(servant: AtlasServant): Pair<String, List<String>> {
        val region = serverResolver.resolve().atlasRegion
        val regions = listOf(region, ATLAS_NA_REGION, ATLAS_JP_REGION).distinct()

        for (candidate in regions) {
            val faces = try {
                api.faces(candidate, servant.id)
            } catch (e: AtlasHttpException) {
                if (e.code != HttpURLConnection.HTTP_NOT_FOUND) throw e

                emptyList()
            }

            if (faces.isNotEmpty()) return candidate to faces
        }

        return region to emptyList()
    }

    /**
     * The name a servant is stored under.
     *
     * Support image names are the identity of a support entry and every image shipped with the
     * app is named the NA way, so an English name is preferred whenever the servant has one. The
     * name of the region the servant was found on is the fallback for those that aren't on NA yet.
     */
    private suspend fun identity(servant: AtlasServant, region: String): String {
        val english = if (region == ATLAS_NA_REGION) servant.name else api.englishName(servant.id)

        return (english ?: servant.name).toSupportImageName()
    }
}
