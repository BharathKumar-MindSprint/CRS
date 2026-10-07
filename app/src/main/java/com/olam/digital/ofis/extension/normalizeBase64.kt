package com.olam.digital.ofis.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Strip data URI prefix and whitespace/newlines.
 * Supports: data:image/png;base64,XXXX
 */
fun normalizeBase64(input: String): String {
    val trimmed = input.trim()
    // If it's a data URI, get the payload after the comma
    val commaIdx = trimmed.indexOf(',')
    val core = if (trimmed.startsWith("data:", ignoreCase = true) && commaIdx != -1) {
        trimmed.substring(commaIdx + 1)
    } else {
        trimmed
    }
    // Remove whitespace/newlines
    return core.replace("\\s".toRegex(), "")
}

/** Syntactic screen (optional): length multiple of 4 and allowed alphabet. */
fun looksLikeBase64Syntax(s: String): Boolean {
    if (s.isEmpty() || s.length % 4 != 0) return false
    val base64Regex = Regex("^[A-Za-z0-9+/]+={0,2}$")
    return base64Regex.matches(s)
}

/** Returns decoded bytes or null if invalid. */
fun decodeBase64OrNull(input: String): ByteArray? {
    val core = normalizeBase64(input)
    if (core.isEmpty()) return null
    return try {
        // DEFAULT tolerates missing padding and line breaks
        Base64.decode(core, Base64.DEFAULT)
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun saveBitmapToInternal(context: Context, bitmap: Bitmap, fileName: String): String? {
    return try {
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        Timber.e("Exception while saving bitmap to internal ${e.message}")
        e.printStackTrace()
        null
    }
}


/** Quick validity check: can we decode it as Base64? */
fun isValidBase64(input: String, useSyntaxCheck: Boolean = true): Boolean {
    val core = normalizeBase64(input)
    if (core.isEmpty()) return false
    if (useSyntaxCheck && !looksLikeBase64Syntax(core)) return false
    return try {
        Base64.decode(core, Base64.DEFAULT)
        true
    } catch (_: IllegalArgumentException) {
        false
    }
}

/** Check magic bytes to see if the decoded bytes look like a common image format. */
fun isImageBytes(bytes: ByteArray): Boolean {
    // PNG: 89 50 4E 47 0D 0A 1A 0A
    if (bytes.size >= 8 &&
        bytes[0] == 0x89.toByte() &&
        bytes[1] == 0x50.toByte() &&
        bytes[2] == 0x4E.toByte() &&
        bytes[3] == 0x47.toByte() &&
        bytes[4] == 0x0D.toByte() &&
        bytes[5] == 0x0A.toByte() &&
        bytes[6] == 0x1A.toByte() &&
        bytes[7] == 0x0A.toByte()
    ) return true

    // JPEG: FF D8 ... FF D9 (we can check only header)
    if (bytes.size >= 3 &&
        bytes[0] == 0xFF.toByte() &&
        bytes[1] == 0xD8.toByte()
    ) return true

    // GIF: "GIF87a" or "GIF89a"
    if (bytes.size >= 6 &&
        bytes[0] == 'G'.code.toByte() &&
        bytes[1] == 'I'.code.toByte() &&
        bytes[2] == 'F'.code.toByte() &&
        bytes[3] == '8'.code.toByte() &&
        (bytes[4] == '7'.code.toByte() || bytes[4] == '9'.code.toByte()) &&
        bytes[5] == 'a'.code.toByte()
    ) return true

    // WebP: "RIFF"...."WEBP"
    if (bytes.size >= 12 &&
        bytes[0] == 'R'.code.toByte() &&
        bytes[1] == 'I'.code.toByte() &&
        bytes[2] == 'F'.code.toByte() &&
        bytes[3] == 'F'.code.toByte() &&
        bytes[8] == 'W'.code.toByte() &&
        bytes[9] == 'E'.code.toByte() &&
        bytes[10] == 'B'.code.toByte() &&
        bytes[11] == 'P'.code.toByte()
    ) return true

    return false
}

/** True if input is a base64 that decodes to recognizable image bytes. */
fun isBase64Image(input: String): Boolean {
    val bytes = decodeBase64OrNull(input) ?: return false
    if (!isImageBytes(bytes)) return false

    // Optional: sanity check via bounds decode (cheap)
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    return (opts.outWidth > 0 && opts.outHeight > 0)
}

/** Decode base64 image into Bitmap; returns null if invalid/not an image. */
fun base64ToBitmapOrNull(input: String): Bitmap? {
    val bytes = decodeBase64OrNull(input) ?: return null
    // Fast fail if headers don't look like an image
    if (!isImageBytes(bytes)) {
        // You can still try full decode; comment the return if you prefer lenient behavior.
        // return null
    }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}