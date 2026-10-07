package com.olam.digital.crs.model.surveysubmit

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SelectedOption(
    @SerialName("optionId") var optionId: Int,
    @SerialName("questionsId") var questionsId: Int,
    @SerialName("optionValue") var optionValue: String
)

@kotlinx.serialization.Serializable
data class Answer(
    @SerialName("question_id") var questionId: Int,
    @SerialName("reporting_year") var reportingYear: Int,
    @SerialName("olam_farmer_id") var olamFarmerId: String,
    @SerialName("action") var action: Action,
    @SerialName("original_id") var originalId: Int,
    @SerialName("is_subset") var isSubset: Boolean,
    @SerialName("subset_section") var subsetSection: String? = null,
    @SerialName("answer") var answer: String? = null,
    @SerialName("globalMetricId") var globalMetricId: Int,
    @SerialName("localMetricId") var localMetricId: Int,
    @SerialName("selectedOptions") var selectedOptions: List<SelectedOption>? = null,
    @SerialName("repeaterItemGroup") var repeaterItemGroup: Int?=null
)

@kotlinx.serialization.Serializable
enum class Action {
    @SerialName("CREATE") CREATE,
    @SerialName("UPDATE") UPDATE,
    @SerialName("DELETE") DELETE;

    companion object {
        fun from(value: String): Action =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CREATE
    }
}

@kotlinx.serialization.Serializable
data class Submission(
    @SerialName("module_id") var moduleId: Int,
    @SerialName("stakeholder_id") var stakeholderId: Int,
    @SerialName("submission_date") var submissionDate: String, // "2026-01-25T10:30:00"
    @SerialName("upload_date") var uploadDate: String,
    @SerialName("olam_enumerator_id") var olamEnumeratorId: Int,
    @SerialName("imei_number") var imeiNumber: String,
    @SerialName("fmPreview") var fmPreview: Boolean,
    @SerialName("farmer_id") var farmerId: Int,
    @SerialName("farm_id") var farmId: Int,
    @SerialName("recorded_lat") var recordedLat: Double,
    @SerialName("recorded_lng") var recordedLng: Double,
    @SerialName("upload_attempts") var uploadAttempts: Int,
    @SerialName("upload_id") var uploadId: String,
    @SerialName("questions_included") var questionsIncluded: String,
    @SerialName("answers") var answers: List<Answer>,
    @SerialName("server_item_id") var serverItemId: String,
    @SerialName("is_web_edit") var isWebEdit: Boolean,
    @SerialName("submitted_module_id") var submittedModuleId: Int? = null,
    @SerialName("last_edit_user_id") var lastEditUserId: Int,
    @SerialName("id") var id: Int,
    @SerialName("country_id") var countryId: Int,
    @SerialName("farmer_group_id") var farmerGroupId: Int,
    @SerialName("product_id") var productId: Int,
    @SerialName("is_review_edit") var isReviewEdit: Boolean,
    @SerialName("isUploadJob") var isUploadJob: Boolean,
    @SerialName("app_version") var appVersion: String,
    @SerialName("updateReason") var updateReason: String,
    @SerialName("recurrenceFrequency") var recurrenceFrequency: Int,
    @SerialName("recurrenceType") var recurrenceType: RecurrenceType,
    @SerialName("surveyDuration") var surveyDuration: Int,
    @SerialName("surveyStartDate") var surveyStartDate: String,
    @SerialName("surveyName") var surveyName: String? = null,
    @SerialName("olamFarmerId")var olamFarmerId: String?=null,
    @SerialName("instanceServiceId")var serviceInstanceId: Long,
    @SerialName("getConsentUrl")var consentImageUrl: String,
    @SerialName("communityRespondentId")var communityRespondentId: Int,
    @SerialName("isCrsSurvey")var isCrsSurvey: Boolean =false
)

@kotlinx.serialization.Serializable
enum class RecurrenceType {
    @SerialName("Daily") DAILY,
    @SerialName("Weekly") WEEKLY,
    @SerialName("Monthly") MONTHLY;

    companion object {
        fun from(value: String): RecurrenceType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MONTHLY
    }
}