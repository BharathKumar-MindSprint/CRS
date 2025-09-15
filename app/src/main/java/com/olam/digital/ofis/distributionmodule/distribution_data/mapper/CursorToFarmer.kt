package com.olam.digital.ofis.distributionmodule.distribution_data.mapper

import android.annotation.SuppressLint
import android.database.Cursor
import com.olam.digital.ofis.OFISDBContract
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.FarmerAndStakeHolderDto

@SuppressLint("Range")
fun cursorToFarmerMapper(cursor: Cursor) : List<FarmerAndStakeHolderDto> {
    val result : MutableList<FarmerAndStakeHolderDto> = mutableListOf()

    cursor.use {
        val nameIndex = it.getColumnIndexOrThrow(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME)
        val idIdx = it.getColumnIndexOrThrow(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)
        val displayIdIdx = it.getColumnIndexOrThrow(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID)

        while (cursor.moveToNext()) {
            val name = it.getString(nameIndex)
            val id = it.getString(idIdx)
            val displayId = it.getString(displayIdIdx)
            result.add(FarmerAndStakeHolderDto(name, id, displayId))
        }
    }

    return result
}

@SuppressLint("Range")
fun cursorToFarmerIdMapper(cursor: Cursor) : List<String> {
    val result : MutableList<String> = mutableListOf()
    cursor.use {
        val idIdx = it.getColumnIndexOrThrow(OFISDBContract.FarmerEntry.COLUMN_NAME_ID)

        while (it.moveToNext()) {
            val id = it.getString(idIdx)
            result.add(id)
        }
    }
    return result
}