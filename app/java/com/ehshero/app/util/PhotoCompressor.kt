package com.ehshero.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Compresses a photo picked/captured by the user down to a small JPEG and
 * Base64-encodes it for direct storage on the SafetyActivity document (see
 * [com.ehshero.app.data.model.SafetyActivity.photoBase64]) - the default
 * photo path, which needs no Firebase Storage / Blaze billing plan at all.
 *
 * Firestore documents are capped at 1 MiB; this keeps the encoded photo
 * comfortably under that so the rest of the activity's fields always fit
 * too. See README "Photo storage" for the Firebase Storage upgrade path if
 * a project wants full-resolution photos instead.
 */
object PhotoCompressor {

    private const val MAX_DIMENSION = 900
    private const val TARGET_MAX_BYTES = 350_000

    /** Returns a Base64 (NO_WRAP) JPEG string, or null if the image
     * couldn't be read/decoded. Runs synchronously - call from a background
     * coroutine dispatcher, never the main thread. */
    fun compressToBase64(context: Context, uri: Uri): String? {
        val bitmap = decodeSampledBitmap(context, uri) ?: return null
        var quality = 85
        var output: ByteArrayOutputStream
        do {
            output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            quality -= 10
        } while (output.size() > TARGET_MAX_BYTES && quality > 20)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    private fun decodeSampledBitmap(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, boundsOptions) }
            ?: return null

        var sampleSize = 1
        val width = boundsOptions.outWidth
        val height = boundsOptions.outHeight
        while (width / sampleSize > MAX_DIMENSION || height / sampleSize > MAX_DIMENSION) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
    }
}
