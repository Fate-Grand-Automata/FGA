package io.github.fate_grand_automata.atlas

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

private const val API_URL = "https://api.atlasacademy.io"

/**
 * Generous for a 120 KB `nice` payload on mobile data, short enough that a dead connection
 * doesn't leave the dialog spinning.
 */
private const val TIME_OUT_MS = 15_000

private const val SEARCH_LIMIT = 30

private const val USER_AGENT = "Fate-Grand-Automata"

/** Carries the status code so callers can tell a missing servant from a broken connection. */
class AtlasHttpException(val code: Int, url: String) : IOException("HTTP $code for $url")

/** `/nice/<region>/servant/<id>`, of which the support images only need the faces. */
@Serializable
internal data class AtlasServantDetail(
    val extraAssets: AtlasExtraAssets = AtlasExtraAssets()
)

@Serializable
internal data class AtlasExtraAssets(
    val faces: AtlasFaces = AtlasFaces()
)

@Serializable
internal data class AtlasFaces(
    val ascension: Map<String, String> = emptyMap(),
    val costume: Map<String, String> = emptyMap()
)

private val atlasJson = Json { ignoreUnknownKeys = true }

/** Search hits that can actually show up in a support list, see [AtlasServant.isSelectable]. */
internal fun decodeServants(body: String): List<AtlasServant> =
    atlasJson.decodeFromString<List<AtlasServant>>(body).filter { it.isSelectable }

internal fun decodeServant(body: String): AtlasServant = atlasJson.decodeFromString(body)

internal fun decodeFaceUrls(body: String): List<String> =
    decodeServantDetail(body).faceUrls()

internal fun decodeServantDetail(body: String): AtlasServantDetail =
    atlasJson.decodeFromString(body)

/**
 * Every face image of a servant, ascensions first and costumes after.
 *
 * Both maps are keyed by their in-game index (ascension 1..4, costumes 300, 400, …), so ordering
 * by the numeric key keeps the ascension images in their usual order with or without costumes.
 * One face can be listed twice, hence the `distinct`.
 */
internal fun AtlasServantDetail.faceUrls(): List<String> =
    (extraAssets.faces.ascension + extraAssets.faces.costume)
        .toSortedMap(compareBy { it.toIntOrNull() ?: Int.MAX_VALUE })
        .values
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

/** The file name a face URL points at, e.g. `f_23005000.png`. */
internal fun faceFileName(url: String): String =
    url.substringAfterLast('/').substringBefore('?').ifEmpty { "face.png" }

/** The digit the DB gives the face art of a servant's final ascension. */
private const val FINAL_ASCENSION_INDEX = '3'

/**
 * The face art of a servant's final ascension, which is what the dialog shows next to them.
 *
 * Faces are named `f_<servantId><index>.png`, with the ascension index as the last digit, so the
 * final ascension is the same name with that digit replaced. Callers fall back to the base face for
 * servants the DB has no final ascension image for.
 */
internal fun finalAscensionFace(face: String): String {
    val fileName = face.substringAfterLast('/')
    val stem = fileName.substringBeforeLast('.', missingDelimiterValue = "")
    if (stem.isEmpty() || !stem.last().isDigit()) return face

    val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
    val finalAscension = stem.dropLast(1) + FINAL_ASCENSION_INDEX +
        if (extension.isEmpty()) "" else ".$extension"

    return face.dropLast(fileName.length) + finalAscension
}

/**
 * Reads servants and their face images from the Atlas Academy API.
 *
 * It is read only and needs no key, which is why the app can offer the DB's servants without
 * shipping anything for them.
 */
@Singleton
class AtlasAcademyApi @Inject constructor() {
    /** Servants whose name contains [query], named the way the game on [region] names them. */
    suspend fun search(region: String, query: String): List<AtlasServant> =
        decodeServants(
            fetchText(
                "$API_URL/basic/$region/servant/search" +
                    "?name=${URLEncoder.encode(query, "UTF-8")}&limit=$SEARCH_LIMIT"
            )
        )

    /** Every face image URL of [servantId] on [region]. */
    suspend fun faces(region: String, servantId: Int): List<String> =
        decodeFaceUrls(fetchText("$API_URL/nice/$region/servant/$servantId"))

    /**
     * The English (NA) name of [servantId], or `null` when the servant isn't on NA yet.
     *
     * Support image names are the identity of a support entry, and every image shipped with the
     * app is named the NA way, so a servant added from any region is stored under the name a NA
     * user would see.
     */
    suspend fun englishName(servantId: Int): String? = try {
        decodeServant(fetchText("$API_URL/basic/NA/servant/$servantId")).name
    } catch (e: AtlasHttpException) {
        if (e.code != HttpURLConnection.HTTP_NOT_FOUND) throw e

        null
    }

    suspend fun downloadFace(url: String): ByteArray =
        fetch(url, "image/png") { connection ->
            connection.inputStream.use { it.readBytes() }
        }

    private suspend fun fetchText(url: String): String =
        fetch(url, "application/json") { connection ->
            connection.inputStream.bufferedReader().use { it.readText() }
        }

    private suspend fun <T> fetch(
        url: String,
        accept: String,
        read: (HttpURLConnection) -> T
    ): T = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIME_OUT_MS
            readTimeout = TIME_OUT_MS
            setRequestProperty("Accept", accept)
            setRequestProperty("User-Agent", USER_AGENT)
        }

        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw AtlasHttpException(code, url)

            read(connection)
        } finally {
            connection.disconnect()
        }
    }
}
