package com.olam.digital.crs.viewmodel.repo

import com.olam.digital.crs.model.QuestionType
import com.olam.digital.crs.model.UIQuestion
import com.olam.digital.crs.model.mapper.FarmerModuleView
import com.olam.digital.crs.model.toQuestionType
import com.olam.digital.crs.room.CRSDao
import com.olam.digital.crs.room.FarmerEntity
import timber.log.Timber
import javax.inject.Inject


class SurveyRepository @Inject constructor(private val dao: CRSDao) {

        suspend fun getModulesForFarmer(
            farmer: FarmerEntity,
            autoSampleIds: List<Long> = emptyList(),
            excludeSurveyType8: Boolean = true
        ): List<FarmerModuleView> {

            fun parseLongId(id: String?): Long? = id?.toLongOrNull()

            return dao.getModulesAgainstFarmer(
                farmerId = farmer.id,
                olamFarmerId = farmer.olamFarmerId,
                countryId = parseLongId(farmer.countryId),
                productId = parseLongId(farmer.productId),
                regionId = parseLongId(farmer.regionId),
                districtId = parseLongId(farmer.districtId),
                placeId = parseLongId(farmer.placeId),
                farmerGroupId = parseLongId(farmer.farmerGroupId),
                sectionId = parseLongId(farmer.sectionId),
                programmeId = parseLongId(farmer.programmeId),
                partnerId = parseLongId(farmer.partnerId),

                autoSampleIds = autoSampleIds,
                includeAuto = if (autoSampleIds.isNotEmpty()) 1 else 0,

                excludeSurveyType8 = if (excludeSurveyType8) 1 else 0
            )
        }




    suspend fun getAllFarmersCrs():List<FarmerEntity>
    {
        try {
            return dao.getAllFarmers()
        }
        catch (exception: Exception)
        {
            Timber.e("Error fetching farmers CRS: ${exception.message}")
            throw exception
        }
    }


    suspend fun getUIQuestionsForModule(moduleId: Long): List<UIQuestion> {
        val questions = dao.getQuestionsByModule(moduleId)
        val typeMap = dao.getAllResponseTypes().associateBy({ it.id }, { it.toQuestionType() })
        val uiList = mutableListOf<UIQuestion>()
        for (q in questions) {
            val type = typeMap[q.responseType?.toInt()] ?: continue
            val options = when (type) {
                QuestionType.RADIO,
                QuestionType.CHECKBOX,
                QuestionType.PHOTO_CHECKBOX,
                QuestionType.DROPDOWN -> dao.getOptionsForQuestion(q.questionId)
                else -> emptyList()
            }
            uiList += UIQuestion(q, options, type)
        }
        return uiList
    }
}
