package com.olam.digital.crs.room

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(time: Long?): Date? = time?.let { Date(it) }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String =
        list?.joinToString(",") ?: ""

    @TypeConverter
    fun toIntList(data: String): List<Int> =
        if (data.isEmpty()) emptyList()
        else data.split(",").map { it.toInt() }
}
