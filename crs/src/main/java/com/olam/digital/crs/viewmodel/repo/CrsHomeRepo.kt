package com.olam.digital.crs.viewmodel.repo

import com.olam.digital.crs.room.CRSDao
import com.olam.digital.crs.room.FarmerEntity
import javax.inject.Inject

class CrsHomeRepo @Inject constructor(private val farmerDao: CRSDao) {
        /** Insert or replace one farmer */
        suspend fun insertFarmer(farmer: FarmerEntity) {
            farmerDao.insertFarmer(farmer)
        }

        /** Insert or replace multiple farmers */
        suspend fun insertFarmers(list: List<FarmerEntity>) {
            farmerDao.insertFarmers(list)
        }

        /** Update farmer */
        suspend fun updateFarmer(farmer: FarmerEntity) {
            farmerDao.updateFarmer(farmer)
        }

        /** Delete one farmer */
        suspend fun deleteFarmer(farmer: FarmerEntity) {
            farmerDao.deleteFarmer(farmer)
        }

    /** Delete all farmer */
    suspend fun deleteFarmers() {
        farmerDao.deleteFarmers()
    }

        /** Fetch all farmers */
        suspend fun getAllFarmers(): List<FarmerEntity> {
            return farmerDao.getAllFarmers()
        }

        /** Fetch farmer by ID */
        suspend fun getFarmerById(id: String): FarmerEntity? {
            return farmerDao.getFarmerById(id)
        }

        /** Search farmers by name */
        suspend fun searchFarmers(query: String): List<FarmerEntity> {
            return farmerDao.searchFarmers(query)
        }

        /** Count farmers */
        suspend fun getFarmerCount(): Int {
            return farmerDao.getFarmerCount()
        }

        /** Clear table */
        suspend fun clearFarmers() {
            farmerDao.clearFarmers()
        }

}