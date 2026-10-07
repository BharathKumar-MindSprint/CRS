package com.olam.digital.crs.model.mapper

import androidx.room.ColumnInfo
import java.io.Serializable
data class FarmerModuleView(
    // ===== Module =====
    @ColumnInfo(name = "module_id") val moduleId: Long,
    val name: String?,
    val surveyType: Int?,
    val channel: Int?,
    val moduleRawJson: String?,
    val submitedCrsSurveyFarmerId: String?,
    val surveyInstanceId: Long?,

    // ===== Assignment =====
    val assignmentId: Long?,
    val assignmentType: String?,
    val communityIds: List<Int>? = null,
    val mandatoryQuestionIds: List<Int>? = null,
    val countryId: Long?,
    val startDateEpoch: String?,
    val surveyDuration: Int?,
    val viewModule: Int?,
    val assignmentRawJson: String?,

    // ===== Farmer module progress =====
    val farmerModuleId: Long?,
    val statusStarted: Int?,
    val statusFinished: Int?,
    val currentSubmoduleId: Long?,
    val recordedLat: Double?,
    val recordedLng: Double?,

    // ===== Question count =====
    val questionCount: Int,

    // ===== Consent fields =====
    val consentId: Long?,
    val consentText: String?,
    val consentType: String?,
    val consentPath: String?,

    val isConsentEnabled: Boolean?=null,

    // ===== Recurrence =====
    val recurrenceType: String? = null,
    val recurrenceFrequency: Int? = null,
    val disabledMonths: String? = null
) : Serializable {

    // ✅ Computed properties parse JSON flags safely
    val isChildLabour: Boolean
        get() = parseFlag("isChildLabour")

    val isFarmSurvey: Boolean
        get() = parseFlag("isFarmSurvey")

    val isPlotSurvey: Boolean
        get() = parseFlag("isPlotSurvey")

    val isOfflineDownload: Boolean
        get() = parseFlag("isOfflineDownload")

    // Convert disabledMonths string like "6,7" into a list
    val disabledMonthsList: List<String>
        get() = disabledMonths?.split(",")?.map { it.trim() } ?: emptyList()

    // Helper to parse boolean flags from assignmentRawJson
    private fun parseFlag(key: String): Boolean {
        return try {
            val obj = org.json.JSONObject(assignmentRawJson ?: "{}")
            obj.optBoolean(key, false)
        } catch (e: Exception) {
            false
        }
    }
}



data class FarmerScope(
    val farmerId: String,
    val olamFarmerId: String?,
    val countryId: Long?,
    val productId: Long?,
    val regionId: Long?,
    val districtId: Long?,
    val placeId: Long?,
    val farmerGroupId: Long?,
    val sectionId: Long?,
    val programmeId: Long?,
    val partnerId: Long?
): Serializable