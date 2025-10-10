package com.olam.digital.ofis.distributionmodule.distribution_data.mapper

import android.annotation.SuppressLint
import android.database.Cursor
import com.olam.digital.ofis.OFISDBContract
import com.olam.digital.ofis.model.SpinnerDataDto

fun cursorToSpinnerDataMapper(cursor: Cursor) : List<SpinnerDataDto> {
    val result = mutableListOf<SpinnerDataDto>()

    cursor.use {
        if (it.moveToFirst()) {
            do {
                val id = it.getInt(0)
                val name = it.getString(1)
                result.add(SpinnerDataDto(name = name, id = id))
            } while (it.moveToNext())
        }
    }

    return result
}
