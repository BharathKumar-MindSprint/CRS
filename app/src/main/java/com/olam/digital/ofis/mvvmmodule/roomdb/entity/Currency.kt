package com.olam.digital.ofis.mvvmmodule.roomdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Currency(
    @PrimaryKey(autoGenerate = true)
    var uid: Int,
    var currencyId : Int,
    var code: String? = null,
    var notation: String? = null,
    var name: String? = null,
    var countryId : Int? = null
)
