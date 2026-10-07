package com.olam.digital.crs.viewmodel.repo

import android.content.Context
import com.olam.digital.crs.di.endpoints.WebEndPoints
import com.olam.digital.crs.model.PhotoImageDownloadRequestBody
import com.olam.digital.crs.room.CRSDao
import com.olam.digital.crs.room.CrsConsent
import com.olam.digital.crs.room.CrsOptionEntity
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.viewmodel.api.CrsApi.saveImageToLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CrsHomeRepo @Inject constructor(
    private val farmerDao: CRSDao,
    private val api: WebEndPoints
) {
    /** Insert or replace image */

    suspend fun getRespondentCode(respondentId: Int): String
    {
       return farmerDao.getRespondentCodeById(respondentId)?:""
    }
    suspend fun updateUserRespondentId(respondentcode:String,respondentId:Int)
    {
        farmerDao.updateUserRespondentId(respondentcode,respondentId)
    }

    suspend fun getAllCrsProfileIds():List<Int>
    {
        return farmerDao.getAllCrsProfileIds()
    }

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


    fun isImageFile(label: String?): Boolean {
        if (label.isNullOrEmpty()) return false
        return label.endsWith(".jpg", true)
                || label.endsWith(".jpeg", true)
                || label.endsWith(".png", true)
    }


    suspend fun getAllPhotoCheckBoxImage(context: Context):List<CrsOptionEntity>
    {
       return farmerDao.getPhotoCheckboxOptions()
    }

    suspend fun getAllConsentFiles(context: Context): List<CrsConsent> {
        return farmerDao.getConsentFiles()
    }

    suspend fun updateUserImage(context: Context,localPath: String,optionId: Long,questionId:Long){
            farmerDao.updateOptionLabelWithLocalPath(optionId, localPath,questionId)
    }

    suspend fun updateUserCosentData(context: Context,localPath: String,consent: CrsConsent){
            farmerDao.updateOptionLabelWithLocalPath(consent.id, localPath)
    }




    suspend fun processAllPhotoCheckboxImages(context: Context,token: String) = withContext(Dispatchers.IO) {
        val options = farmerDao.getPhotoCheckboxOptions()   // DAO: WHERE label LIKE %.jpg/.png
        for (opt in options) {

            if (!isImageFile(opt.label)) continue


            val request = PhotoImageDownloadRequestBody(
                "crs_survey",
                opt.label
            )
            val localPath = downloadImageForSurveyPhotoCheckBoxImage(context, request,token)

            if (localPath != null) {
                farmerDao.updateOptionLabelWithLocalPath(opt.optionId, localPath,opt.questionId)
            }
        }
    }



    suspend fun downloadImageForSurveyPhotoCheckBoxImage(
        context: Context,
        requestBody: PhotoImageDownloadRequestBody,
        beaereToken: String
    ): String? = withContext(Dispatchers.IO) {

        val file = File(context.filesDir, requestBody.filepath)

        if (file.exists()) {
            Timber.d("Image exists: ${file.absolutePath}")
            return@withContext file.absolutePath
        }

        return@withContext try {
            val token = "Bearer $beaereToken"

            val response = api.downloadSurveyImage(token,requestBody)
            if (response == null || !response.isSuccessful || response.body() == null) {
                Timber.e("Download failed: ${response?.code()}")
                null
            } else {
                saveImageToLocal(context, response.body()!!, requestBody.filepath?:"")
            }

        } catch (e: Exception) {
            Timber.e(e, "Download error")
            null
        }
    }



}