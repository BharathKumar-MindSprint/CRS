package com.olam.digital.crs.viewmodel.repo

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.olam.digital.crs.extension.parseYmdToEpoch
import com.olam.digital.crs.model.AnswerKey
import com.olam.digital.crs.model.AnswerRow
import com.olam.digital.crs.model.QuestionType
import com.olam.digital.crs.model.UIQuestion
import com.olam.digital.crs.model.mapper.FarmerModuleView
import com.olam.digital.crs.model.toQuestionType
import com.olam.digital.crs.room.CRSDao
import com.olam.digital.crs.room.ConditionalQuestionDetailEntity
import com.olam.digital.crs.room.ConditionalTypeEntity
import com.olam.digital.crs.room.CountryEntity
import com.olam.digital.crs.room.CrsAnswerEntity
import com.olam.digital.crs.room.CrsCommunityRegionEntity
import com.olam.digital.crs.room.CrsCommunityRegionMappingEntity
import com.olam.digital.crs.room.CrsProfileEntity
import com.olam.digital.crs.room.CrsRepeaterMappingEntity
import com.olam.digital.crs.room.CrsSurveyInstance
import com.olam.digital.crs.room.DistrictEntity
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.room.PlaceEntity
import com.olam.digital.crs.room.ProductEntity
import com.olam.digital.crs.room.ProfileWithMapping
import com.olam.digital.crs.room.RegionEntity
import com.olam.digital.crs.room.RespondentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


class SurveyRepository @Inject constructor(private val dao: CRSDao) {


    suspend fun saveCrsRespondentEntity(profiles: RespondentEntity) {
        try {
            dao.insertRespondent(profiles)
        } catch (exception: Exception) {
            Timber.e("Error saving CRS profiles: ${exception.message}")
            throw exception
        }
    }


    suspend fun getAllCrsProfile(): List<CrsProfileEntity> {
        try {
            return dao.getAllCrsProfile()
        } catch (exception: Exception) {
            Timber.e("Error fetching CRS profiles: ${exception.message}")
            throw exception
        }
    }

    suspend fun getProfilesWithCommunityMapping(): List<ProfileWithMapping> {
        try {
            return dao.getProfilesWithCommunityMapping()
        } catch (exception: Exception) {
            Timber.e("Error fetching CRS profiles: ${exception.message}")
            throw exception
        }
    }

    suspend fun getAllCountryNameMapping(): List<CountryEntity> {
        try {
            return dao.getAllCountryNamesFromMapping()
        } catch (exception: Exception) {
            Timber.e("Error fetching CRS profiles: ${exception.message}")
            throw exception
        }
    }

    suspend fun getAllProductNameMapping(): List<ProductEntity> {
        try {
            return dao.getAllProductNamesFromMapping()
        } catch (exception: Exception) {
            Timber.e("Error fetching CRS profiles: ${exception.message}")
            throw exception
        }
    }


    suspend fun getAllDistrictNameMapping(): List<DistrictEntity> {
        try {
            return dao.getAllDistrictNamesFromMapping()
        } catch (exception: Exception) {
            Timber.e("Error fetching CRS profiles: ${exception.message}")
            throw exception
        }
    }


    suspend fun getAllPlaceNameMapping(): List<PlaceEntity> {
        try {
            return dao.getAllPlaceNamesFromMapping()
        } catch (exception: Exception) {
            Timber.e("Error fetching CRS profiles: ${exception.message}")
            throw exception
        }
    }




    suspend fun getAllRegionIdsFromMapping(): List<CrsCommunityRegionEntity> {
        try {
            return dao.getAllRegionNamesFromMapped()
        } catch (exception: Exception) {
            Timber.e("Error fetching CRS profiles: ${exception.message}")
            throw exception
        }
    }

    suspend fun getModulesForFarmer(
        farmer: FarmerEntity,
        autoSampleIds: List<Long> = emptyList(),
        excludeSurveyType8: Boolean = true,
        farmerPrimaryId: String
    ): List<FarmerModuleView> {

        fun parseLongId(id: String?): Long? = id?.toLongOrNull()
        Timber.d(
            "All Logs variable ${farmer.id}|| ${farmer.olamFarmerId} || ${farmer.countryId} || ${farmer.productId} ||${farmer.regionId} || ${farmer.placeId} || ${farmer.farmerGroupId} || ${farmer.sectionId}" +
                    "|| ${farmer.programmeId} || ${farmer.partnerId} || ${autoSampleIds}||${if (autoSampleIds.isNotEmpty()) 1 else 0} || ${if (excludeSurveyType8) 1 else 0} || $farmerPrimaryId"
        )

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

            excludeSurveyType8 = if (excludeSurveyType8) 1 else 0,
            farmerPrimaryId

        )
    }


    suspend fun getCrsModulesAgainstFarmer(
        farmer: RespondentEntity,
        autoSampleIds: List<Long> = emptyList(),
        excludeSurveyType8: Boolean = true,
        farmerPrimaryId: String,
        selectedProfiles: Int // ← List, not Int
    ): List<FarmerModuleView> {
        fun parseLongId(id: String?): Long? = id?.toLongOrNull()
        Timber.d(
            "All Logs variable ${farmer.id}|| ${farmer.respondentCode} || ${farmer.countryId} || ${farmer.productId} ||${farmer.regionId} || ${farmer.placeId} || ${farmer.districtId} || ${farmer.placeId}" +
                    "|| ${farmer.productId} || ${farmer.profileId} || ${autoSampleIds}||${if (autoSampleIds.isNotEmpty()) 1 else 0} || ${if (excludeSurveyType8) 1 else 0} || $farmerPrimaryId"
        )


        return dao.getCrsModulesAgainstFarmer(
            farmerId = farmer.id.toString(),
            olamFarmerId = farmer.respondentCode,
            countryId = parseLongId(farmer.countryId?.toString()),
            autoSampleIds = autoSampleIds,
            includeAuto = if (autoSampleIds.isNotEmpty()) 1 else 0,
            excludeSurveyType8 = if (excludeSurveyType8) 1 else 0,
            farmerPrimaryKeyId = farmerPrimaryId,
        )
    }


    suspend fun getAllFarmersCrs(): List<FarmerEntity> {
        try {
            return dao.getAllFarmers()
        } catch (exception: Exception) {
            Timber.e("Error fetching farmers CRS: ${exception.message}")
            throw exception
        }
    }

    suspend fun getAllCrsRespondents(selectedProfile: Int): List<RespondentEntity> {
        try {
            Timber.d("Fetching CRS respondents for profile $selectedProfile")
            return dao.getAllCrsRespondents(selectedProfile)
        } catch (exception: Exception) {
            Timber.e("Error fetching farmers CRS: ${exception.message}")
            throw exception
        }
    }





    suspend fun getRepeaterMappingsForModule(moduleId: Long): List<CrsRepeaterMappingEntity> {
        try {
            return dao.getRepeaterMappingsForModule(moduleId)
        } catch (exception: Exception) {
            Timber.e("Error fetching repeater mappings for module $moduleId: ${exception.message}")
            throw exception
        }
    }
    suspend fun getConditionalTypes(): List<ConditionalTypeEntity> {
        return dao.getAllConditionalTypes()
    }

    suspend fun getConditionalDetails(moduleId: Long): List<ConditionalQuestionDetailEntity> {
        return dao.getConditionalQuestionDetails(moduleId)
    }
    suspend fun getUIQuestionsForModule(moduleId: Long): List<UIQuestion> {
        //val questions = dao.getQuestionsByModule(moduleId)
        val questions = dao.getQuestionsByModuleWithManitory(moduleId)
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


    /**
     * Persist the survey: create instance, map answers, save answers, mark instance complete.
     */
    @OptIn(androidx.room.Transaction::class)
    suspend fun persistSurvey(
        farmerId: String,
        moduleId: Long,
        uiQuestions: List<UIQuestion>,                  // original question list
        answers: Map<AnswerKey, Any?>,                       // questionId -> answer
        deviceId: String? = null,
        appVersion: String? = null,
        appContext: Context,
        submitedId: Long,
        surveyContentImage: String,
        signatureCache: MutableMap<AnswerKey, String>,
        isCrs: Boolean
    ): Long {
        return withContext(Dispatchers.IO) {
            // 1) derive lat/lng from answers (if you have a LOCATION question)
            val (lat, lng) = extractLatLng(answers)

            // 2) create instance (IN_PROGRESS -> READY after save)
            val now = System.currentTimeMillis()
            var instanceId = 0L
            if (submitedId != 0L) {
                dao.updateSurveyInstanceSyncData(
                    CrsSurveyInstance(
                        farmerId = farmerId,
                        moduleId = moduleId,
                        status = "IN_PROGRESS",
                        startedAt = now,
                        completedAt = null,
                        syncStatus = "READY",       // READY for outbound sync after local persistence
                        lastSyncAt = null,
                        lastSyncError = null,
                        deviceId = deviceId,
                        appVersion = appVersion,
                        lat = lat,
                        lng = lng,
                        serverInstanceId = null,
                        id = submitedId,
                        crsSignature = surveyContentImage,
                        isCrsSurvey = if (isCrs) 1 else 0
                    )
                )
            } else {
                instanceId = dao.insertCrsSurveySyncData(
                    CrsSurveyInstance(
                        farmerId = farmerId,
                        moduleId = moduleId,
                        status = "IN_PROGRESS",
                        startedAt = now,
                        completedAt = null,
                        syncStatus = "READY",       // READY for outbound sync after local persistence
                        lastSyncAt = null,
                        lastSyncError = null,
                        deviceId = deviceId,
                        appVersion = appVersion,
                        lat = lat,
                        lng = lng,
                        serverInstanceId = null,
                        crsSignature = surveyContentImage,
                        isCrsSurvey = if (isCrs) 1 else 0
                    )
                )
            }

            Timber.e("Signatured answers ${Gson().toJson(signatureCache)}")

            val mergedAnswers = answers.toMutableMap()

            signatureCache.forEach { (questionId, base64) ->
                mergedAnswers[AnswerKey(questionId.questionId)] = base64       // overwrite or insert
            }


            // 3) map answers -> CrsAnswerEntity rows
            val mapped = mapAnswersToEntities(
                instanceId = if (submitedId != 0L) submitedId else instanceId,
                uiQuestions = uiQuestions,
                answers = mergedAnswers,
                resolver = appContext.contentResolver
            )

            Timber.d("Mapped ${mapped.size} answers for instance $instanceId")

            // 4) save answers
            dao.replaceAnswers(if (submitedId != 0L) submitedId else instanceId, mapped)

            // 5) mark instance COMPLETED
            dao.markCompleted(if (submitedId != 0L) submitedId else instanceId)

            if (submitedId != 0L) submitedId else instanceId
        }
    }


    // repository/AnswerMapper.kt
    fun mapAnswersToEntities(
        instanceId: Long,
        uiQuestions: List<UIQuestion>,
        answers: Map<AnswerKey, Any?>,
        resolver: ContentResolver
    ): List<CrsAnswerEntity> {

        val out = mutableListOf<CrsAnswerEntity>()
        Timber.d("Mapping answers for instance $instanceId with ${uiQuestions.size} questions and ${answers.size} answer entries")

        uiQuestions.forEach { q ->
            val qId = q.question.questionId

            // Collect ALL entries for this questionId
            val entries = answers.entries.filter { it.key.questionId == qId }

            entries.forEach { entry ->
                val ans = entry.value
                val repeatIndex = entry.key.repeatIndex
                val repeaterId = entry.key.repeaterId

                // 🔑 Skip invalid repeater entries:
                // If this question has repeaterTimes, only accept entries with repeatIndex != null
             //   if (q.question.repeaterTimes != null && repeatIndex == null) return@forEach

                when (q.type) {
                    QuestionType.EDITTEXT -> {
                        val text = (ans as? String)?.takeIf { it.isNotBlank() }
                        if (text != null) out += CrsAnswerEntity(
                            instanceId = instanceId,
                            questionId = qId,
                            repeatIndex = repeatIndex,
                            valueText = text,
                            repeaterId = repeaterId
                        )
                    }

                    QuestionType.DATEPICKER -> {
                        val dateStr = ans as? String
                        val epoch = dateStr?.let { parseYmdToEpoch(it) }
                        if (epoch != null) out += CrsAnswerEntity(
                            instanceId = instanceId,
                            questionId = qId,
                            repeatIndex = repeatIndex,
                            valueDate = epoch,
                            repeaterId = repeaterId
                        )
                    }

                    QuestionType.RADIO, QuestionType.DROPDOWN -> {
                        val optId = ans as? Long
                        if (optId != null) out += CrsAnswerEntity(
                            instanceId = instanceId,
                            questionId = qId,
                            repeatIndex = repeatIndex,
                            optionId = optId,
                            repeaterId = repeaterId
                        )
                    }

                    QuestionType.CHECKBOX, QuestionType.PHOTO_CHECKBOX -> {
                        val selected = (ans as? List<*>)?.filterIsInstance<Long>().orEmpty()
                        selected.forEach { optId ->
                            out += CrsAnswerEntity(
                                instanceId = instanceId,
                                questionId = qId,
                                repeatIndex = repeatIndex,
                                optionId = optId,
                                valueBoolean = true,
                                repeaterId = repeaterId
                            )
                        }
                    }

                    QuestionType.PHOTO, QuestionType.FILE_UPLOAD -> {
                        val uriStr = ans as? String
                        if (!uriStr.isNullOrBlank()) {
                            val (mime, size) = resolveMeta(resolver, uriStr)
                            out += CrsAnswerEntity(
                                instanceId = instanceId,
                                questionId = qId,
                                repeatIndex = repeatIndex,
                                fileUri = uriStr,
                                mimeType = mime,
                                fileSizeBytes = size,
                                repeaterId = repeaterId
                            )
                        }
                    }

                    QuestionType.LOCATION -> {
                        val locStr = ans as? String
                        if (!locStr.isNullOrBlank()) {
                            out += CrsAnswerEntity(
                                instanceId = instanceId,
                                questionId = qId,
                                repeatIndex = repeatIndex,
                                valueText = locStr,
                                repeaterId = repeaterId
                            )
                        }
                    }

                    QuestionType.SIGNATURE -> {
                        val base64 = ans as? String
                        if (!base64.isNullOrBlank()) {
                            out += CrsAnswerEntity(
                                instanceId = instanceId,
                                questionId = qId,
                                repeatIndex = repeatIndex,
                                valueText = base64,
                                repeaterId = repeaterId
                            )
                        }
                    }

                    else -> {
                        when (ans) {
                            is Number -> out += CrsAnswerEntity(
                                instanceId = instanceId,
                                questionId = qId,
                                repeatIndex = repeatIndex,
                                valueNumber = ans.toDouble(),
                                repeaterId = repeaterId
                            )
                            is String -> if (ans.isNotBlank()) out += CrsAnswerEntity(
                                instanceId = instanceId,
                                questionId = qId,
                                repeatIndex = repeatIndex,
                                valueText = ans,
                                repeaterId = repeaterId
                            )
                        }
                    }
                }
            }
        }

        return out
    }




    /** Resolve MIME and file size for content:// URIs */
    private fun resolveMeta(resolver: ContentResolver, uriStr: String): Pair<String?, Long?> {
        return try {
            val uri = Uri.parse(uriStr)
            var mime: String? = null
            var size: Long? = null

            // MIME
            mime = resolver.getType(uri)

            // Size
            resolver.query(uri, arrayOf(android.provider.OpenableColumns.SIZE), null, null, null)
                ?.use { c ->
                    if (c.moveToFirst()) {
                        val idx = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (idx >= 0) size = c.getLong(idx)
                    }
                }
            mime to size
        } catch (_: Exception) {
            null to null
        }
    }


    /** Parse "lat,lng" or JSON {lat:...,lng:...} stored in answers map */
    private fun extractLatLng(answers: Map<AnswerKey, Any?>): Pair<Double?, Double?> {
        // Find the first string value that looks like lat/lng
        val loc = answers.values.firstOrNull { v ->
            v is String && (v.contains(",") || v.contains("\"lat\""))
        } as? String ?: return null to null

        return when {
            loc.contains(",") -> {
                val parts = loc.split(",")
                val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
                val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
                lat to lng
            }

            loc.contains("\"lat\"") -> {
                val lat = Regex("\"lat\"\\s*:\\s*([\\d\\.-]+)").find(loc)?.groupValues?.get(1)?.toDoubleOrNull()
                val lng = Regex("\"lng\"\\s*:\\s*([\\d\\.-]+)").find(loc)?.groupValues?.get(1)?.toDoubleOrNull()
                lat to lng
            }

            else -> null to null
        }
    }



    /**
     * Returns a map of questionId -> answer (Any?) to prefill the UI.
     * Shapes match your ViewModel's validate() expectations.
     */
    suspend fun getPreviousAnswersAsMap(
        moduleId: Long,
        farmerId: String?,           // FromEntity.id
        surveyInstanceId: Long? = null
    ): Map<Long, Any?> {
        // 1) Pull raw answer rows
        val rows = dao.getAnswersForModule(
            moduleId = moduleId,
            surveyInstanceId = surveyInstanceId
        )
        if (rows.isEmpty()) return emptyMap()

        // 2) Determine per-question type
        val typeMap = dao.getAllResponseTypes().associateBy({ it.id }, { it.toQuestionType() })

        //val questions = dao.getQuestionsByModule(moduleId)
        val questions = dao.getQuestionsByModuleWithManitory(moduleId)
        val questionTypeById: Map<Long, QuestionType> = questions.associate { q ->
            val qt = q.responseType?.toInt()?.let { typeMap[it] } ?: QuestionType.EDITTEXT
            q.questionId to qt
        }

        // 3) Group answers and map to UI shapes
        val byQuestion = rows.groupBy { it.questionId }
        val result = LinkedHashMap<Long, Any?>()

        byQuestion.forEach { (questionId, rws) ->
            val qType = questionTypeById[questionId]

            // sort latest first (nulls last)
            val sorted = rws.sortedWith(
                compareByDescending<AnswerRow> { it.updatedAt ?: Long.MIN_VALUE }
            )

            when (qType) {
                QuestionType.RADIO, QuestionType.DROPDOWN -> {
                    // Latest selected option
                    val selected = sorted.firstOrNull { it.optionId != null }?.optionId
                    if (selected != null) result[questionId] = selected
                }

                QuestionType.CHECKBOX, QuestionType.PHOTO_CHECKBOX -> {
                    // Collect all selected options (distinct)
                    val ids = sorted.mapNotNull { it.optionId }.distinct()
                    if (ids.isNotEmpty()) result[questionId] = ids
                }

                // Text-like: prefer valueText; fallback to formatted number/date/json
                QuestionType.EDITTEXT -> {
                    val text = sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText
                    val fallbackNum =
                        sorted.firstOrNull { it.valueNumber != null }?.valueNumber?.toString()
                            ?: sorted.firstOrNull { it.valueInt != null }?.valueInt?.toString()
                    val value = text ?: fallbackNum
                    if (!value.isNullOrBlank()) result[questionId] = value
                }

                QuestionType.DATEPICKER -> {
                    val text = sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText
                    val dateEpoch = sorted.firstOrNull { it.valueDate != null }?.valueDate
                    val value = text ?: dateEpoch?.let { formatDate(it) }
                    if (!value.isNullOrBlank()) result[questionId] = value
                }

                QuestionType.LOCATION -> {
                    // Prefer JSON; fallback to text
                    val json = sorted.firstOrNull { !it.valueJson.isNullOrBlank() }?.valueJson
                    val text = sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText
                    val value = json ?: text
                    if (!value.isNullOrBlank()) result[questionId] = value
                }

                QuestionType.PHOTO, QuestionType.FILE_UPLOAD -> {
                    // Prefer local fileUri; fallback to remoteUrl; else text
                    val file = sorted.firstOrNull { !it.fileUri.isNullOrBlank() }?.fileUri
                    val remote = sorted.firstOrNull { !it.remoteUrl.isNullOrBlank() }?.remoteUrl
                    val text = sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText
                    val value = file ?: remote ?: text
                    if (!value.isNullOrBlank()) result[questionId] = value
                }

                else -> {
                    // Fallback: prefer option, else text/json/number
                    val selected = sorted.firstOrNull { it.optionId != null }?.optionId
                    val text = sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText
                    val json = sorted.firstOrNull { !it.valueJson.isNullOrBlank() }?.valueJson
                    val num = sorted.firstOrNull { it.valueNumber != null }?.valueNumber?.toString()
                        ?: sorted.firstOrNull { it.valueInt != null }?.valueInt?.toString()
                    val value = selected ?: text ?: json ?: num
                    if (value != null) result[questionId] = value
                }
            }
        }

        return result
    }




    suspend fun getPreviousAnswersAsMapOld(
        moduleId: Long,
        farmerId: String?,
        surveyInstanceId: Long? = null
    ): Map<AnswerKey, Any?> {
        val rows = dao.getAnswersForModule(moduleId, surveyInstanceId)
        if (rows.isEmpty()) return emptyMap()

        val typeMap = dao.getAllResponseTypes().associateBy({ it.id }, { it.toQuestionType() })
        val questions = dao.getQuestionsByModuleWithManitory(moduleId)
        val questionTypeById: Map<Long, QuestionType> = questions.associate { q ->
            val qt = q.responseType?.toInt()?.let { typeMap[it] } ?: QuestionType.EDITTEXT
            q.questionId to qt
        }

        val byQuestion = rows.groupBy { it.questionId }
        val result = LinkedHashMap<AnswerKey, Any?>()

        byQuestion.forEach { (questionId, rws) ->
            val qType = questionTypeById[questionId]
            val sorted = rws.sortedWith(compareByDescending<AnswerRow> { it.updatedAt ?: Long.MIN_VALUE })

            sorted.forEach { row ->
                val key = AnswerKey(questionId, row.repeaterId,row.repeatIndex)

                val value: Any? = when (qType) {
                    QuestionType.RADIO, QuestionType.DROPDOWN -> row.optionId

                    QuestionType.CHECKBOX, QuestionType.PHOTO_CHECKBOX ->
                        sorted.mapNotNull { it.optionId }.distinct()

                    QuestionType.EDITTEXT ->
                        row.valueText ?: row.valueNumber?.toString() ?: row.valueInt?.toString()

                    QuestionType.DATEPICKER ->
                        row.valueText ?: row.valueDate?.let { formatDate(it) }

                    QuestionType.LOCATION ->
                        row.valueJson ?: row.valueText

                    QuestionType.PHOTO, QuestionType.FILE_UPLOAD ->
                        row.fileUri ?: row.remoteUrl ?: row.valueText

                    else ->
                        row.optionId ?: row.valueText ?: row.valueJson
                        ?: row.valueNumber?.toString() ?: row.valueInt?.toString()
                }

                if (value != null) {
                    result[key] = value
                }
            }
        }

        return result
    }


    suspend fun getPreviousAnswersAsMapNew(
        moduleId: Long,
        farmerId: String?,
        surveyInstanceId: Long? = null
    ): Map<AnswerKey, Any?> {
        val rows = dao.getAnswersForModule(moduleId, surveyInstanceId)
        if (rows.isEmpty()) return emptyMap()

        val typeMap = dao.getAllResponseTypes().associateBy({ it.id }, { it.toQuestionType() })
        val questions = dao.getQuestionsByModuleWithManitory(moduleId)
        val questionTypeById: Map<Long, QuestionType> = questions.associate { q ->
            val qt = q.responseType?.toInt()?.let { typeMap[it] } ?: QuestionType.EDITTEXT
            q.questionId to qt
        }

        // Group by the FULL key so repeat #1 and repeat #2 of the same child are separate buckets
        val grouped = rows.groupBy { Triple(it.questionId, it.repeaterId, it.repeatIndex) }
        val result = LinkedHashMap<AnswerKey, Any?>()

        grouped.forEach { (triple, bucketRows) ->
            val (questionId, repeaterId, repeatIndex) = triple
            val qType = questionTypeById[questionId] ?: QuestionType.EDITTEXT
            val sorted = bucketRows.sortedWith(
                compareByDescending<AnswerRow> { it.updatedAt ?: Long.MIN_VALUE }
            )
            val key = AnswerKey(questionId, repeaterId, repeatIndex)

            val value: Any? = when (qType) {
                QuestionType.RADIO, QuestionType.DROPDOWN ->
                    sorted.firstOrNull { it.optionId != null }?.optionId

                QuestionType.CHECKBOX, QuestionType.PHOTO_CHECKBOX ->
                    // distinct option ids WITHIN this (qId, repeaterId, repeatIndex) bucket only
                    sorted.mapNotNull { it.optionId }.distinct().takeIf { it.isNotEmpty() }

                QuestionType.EDITTEXT ->
                    sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText
                        ?: sorted.firstOrNull { it.valueNumber != null }?.valueNumber?.toString()
                        ?: sorted.firstOrNull { it.valueInt != null }?.valueInt?.toString()

                QuestionType.NUMBERIC, QuestionType.DECIMAL ->
                    sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText
                        ?: sorted.firstOrNull { it.valueNumber != null }?.valueNumber?.toString()
                        ?: sorted.firstOrNull { it.valueInt != null }?.valueInt?.toString()

                QuestionType.DATEPICKER ->
                    sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText
                        ?: sorted.firstOrNull { it.valueDate != null }?.valueDate?.let(::formatDate)

                QuestionType.LOCATION ->
                    sorted.firstOrNull { !it.valueJson.isNullOrBlank() }?.valueJson
                        ?: sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText

                QuestionType.PHOTO, QuestionType.FILE_UPLOAD ->
                    sorted.firstOrNull { !it.fileUri.isNullOrBlank() }?.fileUri
                        ?: sorted.firstOrNull { !it.remoteUrl.isNullOrBlank() }?.remoteUrl
                        ?: sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText

                QuestionType.SIGNATURE ->
                    sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText

                else ->
                    sorted.firstOrNull { it.optionId != null }?.optionId
                        ?: sorted.firstOrNull { !it.valueText.isNullOrBlank() }?.valueText
                        ?: sorted.firstOrNull { !it.valueJson.isNullOrBlank() }?.valueJson
                        ?: sorted.firstOrNull { it.valueNumber != null }?.valueNumber?.toString()
                        ?: sorted.firstOrNull { it.valueInt != null }?.valueInt?.toString()
            }

            if (value != null) result[key] = value
        }

        return result
    }



    // --- Helpers ---

    private fun formatDate(epochMillis: Long): String {
        // Return ISO-8601 date or any format your UI wants
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(epochMillis))
    }


    suspend fun updateSurveyAlreadySubmitted(moduleId: Long) {
        dao.updateModuleAlreadySubmitted(moduleId)
    }

}


