package com.olam.digital.crs.viewmodel.repo

import com.google.gson.Gson
import com.olam.digital.crs.model.surveysubmit.Action
import com.olam.digital.crs.model.surveysubmit.Answer
import com.olam.digital.crs.model.surveysubmit.RecurrenceType
import com.olam.digital.crs.model.surveysubmit.SelectedOption
import com.olam.digital.crs.model.surveysubmit.Submission
import com.olam.digital.crs.room.CRSDao
import com.olam.digital.crs.room.CrsAnswerEntity
import com.olam.digital.crs.room.CrsOptionEntity
import com.olam.digital.crs.room.CrsSurveyInstance
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.room.RespondentEntity
import com.olam.digital.crs.room.SubmissionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max


class SubmissionRepository @Inject constructor(private val crsDao: CRSDao) {


    suspend fun updateSurveyInstance(instanceId: Long): Int {
        return withContext(Dispatchers.IO) {
            try {
                crsDao.replaceOldCrsSurveyInstanceAfterSyncSuccess(instanceId)
            } catch (exception: Exception) {
                Timber.e("Exception while updateSurveyInstance ${exception.message}", exception)
                0
            }
        }
    }

    suspend fun getAllPendingRespondentCount():List<RespondentEntity>
    {
        return withContext(Dispatchers.IO)
        {
            try {
                crsDao.getAllDirtyRespondents()
            } catch (exception: Exception) {
                Timber.e("Exception while getAllPendingRespondentCount ${exception.message}", exception)
                emptyList()
            }
        }
    }

    suspend fun getPendingSubmissionInstanceIds(submissionContext: SubmissionContext): List<Submission> {
        try {
            val getAllPendingSubmission = crsDao.getInstancesNeedingSync()
            val submissionList = mutableListOf<Submission>()
            getAllPendingSubmission.forEach {
                val submission = buildSubmissionForInstance(it, submissionContext)
                submissionList.add(submission)
            }
            return submissionList
        } catch (exception: Exception) {
            Timber.d("Exception in getPendingSubmissionInstanceIds: ${exception.localizedMessage}")
            return emptyList()
        }
    }


    suspend fun buildSubmissionForInstance(
        submissionData: CrsSurveyInstance,
        context: SubmissionContext = SubmissionContext()
    ): Submission {
        val instanceId = submissionData.id
        val sw = crsDao.getSurveyWithAnswers(instanceId)
            ?: error("Survey instance $instanceId not found")
        val instance = sw.instance
        var farmerProfile: FarmerEntity? = null
        var respondentEntity: RespondentEntity? = null



        if (submissionData.isCrsSurvey == 1) {
            respondentEntity =  crsDao.getRespondentById(instance.farmerId.toInt())
        }
        else{
            farmerProfile = crsDao.getFarmerById(instance.farmerId)
        }
        val module = crsDao.getModuleById(instance.moduleId)

        // Resolve option labels for all option-backed answers
        val allOptionIds = sw.answers.mapNotNull { it.optionId }.distinct()
        val optionsById: Map<Long, CrsOptionEntity> =
            if (allOptionIds.isEmpty()) emptyMap()
            else crsDao.getOptionsByIds(allOptionIds).associateBy { it.optionId }

        // Timestamps
        val startedAt = epochToLocalDateTime(instance.startedAt)
        val completedAt = instance.completedAt?.let { epochToLocalDateTime(it) } ?: startedAt
        val uploadAt =
            instance.lastSyncAt?.let { epochToLocalDateTime(it) } ?: Clock.System.now().let {
                it.toLocalDateTime(TimeZone.currentSystemDefault())
            }

        // Survey duration in minutes (safe)
        val durationMinutes: Int = instance.completedAt?.let {
            val diffMs = max(0L, it - instance.startedAt)
            (diffMs / 60000L).toInt()
        } ?: 0

        // Build Answers grouped by question
        val answers = mapAnswers(
            sw.answers,
            optionsById,
            olamFarmerId = if (respondentEntity != null) respondentEntity.respondentCode?:"N/A" else farmerProfile?.olamFarmerId ?: "N/A",
                reportingYear = startedAt.year
        )
        Timber.d("Mapped answers ${Gson().toJson(answers)} for instance $instanceId")
        Timber.d("Mapped ${answers.size} answers for instance $instanceId (farmerId=${instance.farmerId}, olamFarmerId=${if (respondentEntity != null) respondentEntity.respondentCode?:"N/A" else farmerProfile?.olamFarmerId ?: "N/A"})")
        val questionsIncluded = answers.joinToString(",") { it.questionId.toString() }//need to remove duplicate question id if same question is repeated in survey, for now we are keeping as is since it is not creating any issue in backend, can be optimized if needed

        return Submission(
            moduleId = instance.moduleId.toInt(),
            stakeholderId = context.stakeholderId,
            submissionDate = epochToIso(instance.completedAt!!),
            uploadDate = epochToIso(instance.lastSyncAt ?: System.currentTimeMillis()),
            olamEnumeratorId = context.olamEnumeratorId,
            imeiNumber = instance.deviceId ?: "",
            fmPreview = false,//for coco product not related to OA product, fill as needed
            farmerId = if (respondentEntity != null) respondentEntity.respondentCode?.toIntOrNull() ?: 0 else farmerProfile?.id?.toIntOrNull() ?: 0,
            farmId = if (respondentEntity != null)  0 else farmerProfile?.farmId?.toIntOrNull() ?: 0,
            recordedLat = instance.lat ?: 0.0,
            recordedLng = instance.lng ?: 0.0,
            uploadAttempts = 0, // local creation; increment on each retry
            uploadId = UUID.randomUUID().toString(),
            questionsIncluded = questionsIncluded,
            answers = answers,
            serverItemId = instance.serverInstanceId ?: "",
            isWebEdit = false,
            submittedModuleId = null, // not tracked locally; fill if you have it
            lastEditUserId = context.lastEditUserId,
            id = respondentEntity?.id?.toInt() ?: (farmerProfile?.id?.toIntOrNull() ?: 0), // matches your sample where id==farmer_id
            countryId = if (respondentEntity!=null) respondentEntity.countryId ?: 0 else farmerProfile?.countryId?.toIntOrNull() ?: 0,
            farmerGroupId = if (respondentEntity!=null)  0 else farmerProfile?.farmerGroupId?.toIntOrNull() ?: 0,
            productId = if (respondentEntity!=null)respondentEntity.productId ?: 0 else farmerProfile?.productId?.toIntOrNull() ?: 0,
            isReviewEdit = false,//Calculation logic questionnaire doesn't have this field, fill as needed
            isUploadJob = context.isUploadJob,
            appVersion = context.appVersion ?: "",
            updateReason = context.updateReason,
            recurrenceFrequency = getSurveyRecurrenceFrequency(instance.moduleId),
            recurrenceType = RecurrenceType.from(getSurveyRecurrenceType(instance.moduleId)),
            surveyDuration = getSurveyDuration(instance.moduleId),
            surveyStartDate = epochToIso(instance.startedAt),
            surveyName = module?.name ?: "N/A",
            olamFarmerId =if (respondentEntity!=null) respondentEntity.respondentCode?:"N/A" else farmerProfile?.olamFarmerId ?: "N/A",
            instanceId,
            consentImageUrl = submissionData.crsSignature ?: "",
            communityRespondentId= respondentEntity?.id ?: (farmerProfile?.id?.toIntOrNull() ?: 0),
            isCrsSurvey =  instance.isCrsSurvey ==1
        )
    }

    suspend fun getSurveyRecurrenceType(moduleId: Long): String {
        return withContext(Dispatchers.IO) {
            try {
                return@withContext crsDao.getRecurrenceTypeForModule(moduleId) ?: ""
            } catch (exception: Exception) {
                Timber.e("Exception while calculating survey recurrence type ${exception.message}", exception)
                "NONE"
            }
        }
    }

    suspend fun getSurveyRecurrenceFrequency(moduleId: Long): Int {
        return withContext(Dispatchers.IO) {
            try {
                return@withContext crsDao.getRecurrenceFrequencyForModule(moduleId)?: 0
            } catch (exception: Exception) {
                Timber.e("Exception while calculating survey recurrence frequency ${exception.message}", exception)
                0
            }
        }
    }


    suspend fun getSurveyDuration(moduleId: Long): Int {
        return withContext(Dispatchers.IO) {
            try {
                return@withContext crsDao.getDurationsForModule(moduleId)
            } catch (exception: Exception) {
                Timber.e("Exception while calculating survey duration ${exception.message}", exception)
                0
            }
        }
    }

    suspend fun getRespondentCode(respondentId: Int)
    {
        try {
            val respondent = crsDao.getRespondentById(respondentId)
            Timber.d("Respondent code for respondentId $respondentId is ${respondent?.respondentCode}")
        }
        catch (exception:Exception)
        {
            Timber.e("Exception while fetching respondent code ${exception.message}", exception)
        }
    }

    suspend fun respondentsUpdateCode(respondentId:Int)
    {
        try {
            crsDao.updateRespondentSynced(respondentId)
        }
        catch (exception:Exception)
        {
            Timber.e("Exception while updating respondent code ${exception.message}", exception)
        }
    }





    private fun mapAnswers(
        rawAnswers: List<CrsAnswerEntity>,
        optionsById: Map<Long, CrsOptionEntity>,
        olamFarmerId: String,
        reportingYear: Int
    ): List<Answer> {
        // Group by questionId + repeaterId
        val byQuestion = rawAnswers.groupBy { Triple(it.questionId, it.repeatIndex,it.repeaterId) }

        return byQuestion.map { (key, items) ->
            Timber.d("Mapping answers for questionId ${key.first} with ${items.size} items (repeaterId=${key.second})")
            val (questionId,repeatIndex, repeaterId) = key
            Timber.d("Processing questionId $questionId with repeaterId $repeaterId")
            Timber.d("Processing questonId $questionId with repeatIndex $repeatIndex")
            Timber.d("Items for questionId $questionId: ${items.joinToString { "id=${it.id}, optionId=${it.optionId}, valueText=${it.valueText}" }}")
            val optionItems = items.filter { it.optionId != null }
            val selectedOptions: List<SelectedOption>? =
                if (optionItems.isNotEmpty()) {
                    optionItems.mapNotNull { row ->
                        val optId = row.optionId ?: return@mapNotNull null
                        val label = optionsById[optId]?.label ?: "Option $optId"
                        SelectedOption(
                            optionId = optId.toInt(),
                            questionsId = questionId.toInt(),
                            optionValue = label
                        )
                    }
                } else null

            val simpleAnswer: String? =
                if (selectedOptions == null) {
                    val row = items.first()
                    coalesceAnswerValue(row)
                } else null

            Answer(
                questionId = questionId.toInt(),
                reportingYear = reportingYear,
                olamFarmerId = olamFarmerId,
                action = Action.CREATE,
                originalId = 0,
                isSubset = false,
                subsetSection = null,
                answer = simpleAnswer,
                globalMetricId = 0,
                localMetricId = 0,
                selectedOptions = selectedOptions,
                repeaterItemGroup = repeatIndex // 🔑 fill repeater group
            )
        }.sortedBy { it.questionId }
    }


    private fun coalesceAnswerValue(row: CrsAnswerEntity): String? {
        return when {
            !row.valueText.isNullOrBlank() -> row.valueText
            row.valueBoolean != null -> if (row.valueBoolean) "Yes" else "No"
            row.valueInt != null -> row.valueInt.toString()
            row.valueNumber != null -> formatNumber(row.valueNumber)
            row.valueDate != null -> epochToIso(row.valueDate)
            !row.fileUri.isNullOrBlank() -> row.fileUri
            !row.valueJson.isNullOrBlank() -> row.valueJson // as-is; consider compacting/validating
            else -> null
        }
    }

    private fun formatNumber(n: Double): String =
        if (n % 1.0 == 0.0) n.toInt().toString() else n.toString()

    private fun epochToIso(epochMs: Long): String {
        val dt = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return "%04d-%02d-%02dT%02d:%02d:%02d".format(
            dt.year, dt.monthNumber, dt.dayOfMonth,
            dt.hour, dt.minute, dt.second
        )
    }


    private fun epochToLocalDateTime(epochMs: Long): LocalDateTime {
        val inst = Instant.fromEpochMilliseconds(epochMs)
        return inst.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    suspend fun getAllPendingSyncSurveyCount(): Long {
        return withContext(Dispatchers.IO)
        {
            try {
                val countData = crsDao.getInstancesCountNeedingSync()
                val countRespondent = crsDao.getAllDirtyRespondents()
                val totalCount = countData + countRespondent.size
                totalCount
            } catch (exception: Exception) {
                0
            }
        }
    }

    suspend fun updateSurveyAlreadySubmitted(moduleId: Long) {
        Timber.d("Update module id after submit survey $moduleId")
        crsDao.updateModuleAlreadySubmitted(moduleId)
    }

}
