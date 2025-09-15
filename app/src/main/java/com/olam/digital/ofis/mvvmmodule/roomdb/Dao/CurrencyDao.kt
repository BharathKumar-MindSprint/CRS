package com.olam.digital.ofis.mvvmmodule.roomdb.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.digital.ofis.model.SpinnerDataDto
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Currency

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCurrency(currencies : List<Currency>)

    @Query("SELECT uid as id, name, notation FROM Currency WHERE countryId IN (:countyId)")
    fun getCurrenciesByCountryId(countyId : List<Int>) : MutableList<SpinnerDataDto>

    @Query("DELETE FROM Currency")
    fun deleteAll()
}