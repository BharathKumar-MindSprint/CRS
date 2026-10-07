package com.olam.digital.crs.extension

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Date

// Thread-safe access to SimpleDateFormat via ThreadLocal
private val ymdFormatterThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC") // Treat input as UTC calendar date
            isLenient = false
        }
    }
}

fun parseYmdToEpoch(dateStr: String): Long? = try {
    val sdf = ymdFormatterThreadLocal.get()
    // Parses as UTC midnight of the given date
    val date: Date? = sdf.parse(dateStr)
    date?.time
} catch (_: ParseException) {
    null
} catch (_: Exception) {
    null
}

/** Format epoch millis (assumed UTC) back to "yyyy-MM-dd" */
fun formatEpochToYmd(epochMillis: Long): String {
    val sdf = ymdFormatterThreadLocal.get()
    return sdf.format(Date(epochMillis))
}