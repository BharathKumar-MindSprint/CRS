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

    // ===== Assignment =====
    val assignmentId: Long?,
    val assignmentType: String?,
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

    // ✅ ===== Consent fields =====
    val consentId: Long?,
    val consentText: String?,
    val consentType: String?,
    val consentPath: String?
): Serializable

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