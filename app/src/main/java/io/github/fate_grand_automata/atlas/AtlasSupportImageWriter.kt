package io.github.fate_grand_automata.atlas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.github.fate_grand_automata.IStorageProvider
import io.github.fate_grand_automata.SupportImageKind
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Saves a face image downloaded from Atlas Academy as a support image of [servantName].
 *
 * The file lands in `support/servant/<servantName>/`, the same place the Support Image Maker
 * writes to, so the support selection picks it up like any other added servant.
 */
@Singleton
class AtlasSupportImageWriter @Inject constructor(
    private val storageProvider: IStorageProvider
) {
    fun write(servantName: String, fileName: String, face: ByteArray) {
        val source = BitmapFactory.decodeByteArray(face, 0, face.size)
            ?: throw IOException("Not an image: $fileName")

        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)

        val template = SupportServantImage.render(pixels, source.width, source.height)
        val image = Bitmap.createBitmap(
            template,
            SupportServantImage.WIDTH,
            SupportServantImage.HEIGHT,
            Bitmap.Config.ARGB_8888
        )

        storageProvider
            .writeSupportImage(SupportImageKind.Servant, "$servantName/$fileName")
            .use { stream ->
                if (!image.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    throw IOException("Could not save $fileName")
                }
            }
    }
}
