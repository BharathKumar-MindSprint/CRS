package com.olam.digital.crs.room

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.io.Serializable


@Entity(tableName = "crs_questionnaire")
data class CRSQuestionnaireDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val status: String? = null,
    val resultCount: Long? = null
) : Serializable

@Entity(tableName = "farmers_crs") //farmer_crs
data class FarmerEntity(

    // Primary ID
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "farm_id") val farmId: String? = null,
    @ColumnInfo(name = "place_id") val placeId: String? = null,
    @ColumnInfo(name = "section_id") val sectionId: String? = null,
    @ColumnInfo(name = "country_id") val countryId: String? = null,

    @ColumnInfo(name = "name") val farmerName: String? = null,
    @ColumnInfo(name = "gender") val gender: String? = null,
    @ColumnInfo(name = "dob") val dob: String? = null,

    @ColumnInfo(name = "olam_farmer_id") val olamFarmerId: String? = null,
    @ColumnInfo(name = "photo") val photo: String? = null,
    @ColumnInfo(name = "signature") val signature: String? = null,
    @ColumnInfo(name = "synced") val synced: Int?,
    @ColumnInfo(name = "created_on_this_phone") val createdLocally: Int?,

    @ColumnInfo(name = "website_farmer_id") val websiteFarmerId: String? = null,
    @ColumnInfo(name = "submitted_modules") val submittedModules: String? = null,

    @ColumnInfo(name = "primary_lat") val primaryLat: Double?,
    @ColumnInfo(name = "primary_lng") val primaryLng: Double?,
    @ColumnInfo(name = "location") val location: String? = null,

    @ColumnInfo(name = "consent_farmer") val consentFarmer: Int?,
    @ColumnInfo(name = "consent_others") val consentOthers: Int?,

    @ColumnInfo(name = "districtId") val districtId: String? = null,
    @ColumnInfo(name = "subdistrictId") val subdistrictId: String? = null,
    @ColumnInfo(name = "farmerGroupId") val farmerGroupId: String? = null,
    @ColumnInfo(name = "regionId") val regionId: String? = null,
    @ColumnInfo(name = "productId") val productId: String? = null,

    @ColumnInfo(name = "registeredDate") val registeredDate: String? = null,
    @ColumnInfo(name = "nidmoderation") val moderationStatus: String? = null,

    @ColumnInfo(name = "farmerLead") val farmerLead: String? = null,
    @ColumnInfo(name = "farmerLeadName") val farmerLeadName: String? = null,

    @ColumnInfo(name = "odFarmerId") val odFarmerId: String? = null,

    @ColumnInfo(name = "plot") val plot: String? = null,
    @ColumnInfo(name = "partner_Id") val partnerId: String? = null,
    @ColumnInfo(name = "programme_Id") val programmeId: String? = null,

    @ColumnInfo(name = "certificationIds") val certificationIds: String? = null,
    @ColumnInfo(name = "certificationText") val certificationText: String? = null,

    @ColumnInfo(name = "age") val age: Int?,

    @ColumnInfo(name = "complainceFlag") val complainceFlag: String? = null,
    @ColumnInfo(name = "conductSurveyForNonCompliant") val conductSurveyForNCF: String? = null,
    @ColumnInfo(name = "conductTrainingForNonCompliant") val conductTrainingForNCF: String? = null,
    @ColumnInfo(name = "polygonCaptureForNonCompliant") val polygonCaptureForNCF: String? = null,

    @ColumnInfo(name = "farmCode") val farmCode: String? = null,
    @ColumnInfo(name = "farmName") val farmName: String? = null,
    @ColumnInfo(name = "farmerCode") val farmerCode: String? = null,
    @ColumnInfo(name = "duplicateFarmerCode") val duplicateFarmerCode: String? = null,

    @ColumnInfo(name = "plotAvailable") val plotAvailable: String? = null,

    @ColumnInfo(name = "isNoNationalId") val isNoNationalId: Int?,
    @ColumnInfo(name = "isNoPhoneNumber") val isNoPhoneNumber: Int?,

    // Additional columns
    @ColumnInfo(name = "householdLat") val householdLat: Double?,
    @ColumnInfo(name = "householdLng") val householdLng: Double?,
    @ColumnInfo(name = "householdId") val householdId: String? = null,

    @ColumnInfo(name = "isConsentAvailable") val isConsentAvailable: Int?,

    @ColumnInfo(name = "displayFarmerId") val displayFarmerId: String? = null
) : Serializable


@Entity(tableName = "crs_modules", indices = [Index(value = arrayOf("name"))])
data class CRSModuleEntity(
    @PrimaryKey
    var id: Long = 0,
    var name: String? = null,
    var surveyType: Int? = null,
    var channel: Int? = null,
    // Keep raw JSON for future-proofing
    var rawJson: String? = null
) : Serializable


@Entity(
    tableName = "module_assignment", indices = [Index(value = ["moduleId"]),
        Index(value = ["moduleAssignmentType"]), Index(value = ["moduleAssignmentId"]),
        Index(value = ["countryId"])]
)
data class ModuleAssignmentEntity(
    @PrimaryKey
    var id: Long = 0,

    var moduleId: Long = 0,

    var moduleAssignmentId: Long = 0,
    var moduleAssignmentType: String? = null,

    var startDateEpoch: String? = null, // millis
    var surveyDuration: Int? = null,
    var viewModule: Int? = null,
    var countryId: Long? = null,

    var rawJson: String? = null,
) : Serializable


@Entity(
    tableName = "farmer_modules",
    indices = [Index(value = ["farmer_id"]), Index(value = ["olam_farmer_id"]), Index(value = ["module_id"])]
)
data class FarmerModuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "farmer_id") val farmerId: String?,
    @ColumnInfo(name = "olam_farmer_id") val olamFarmerId: String?,
    @ColumnInfo(name = "module_id") val moduleId: Long,
    @ColumnInfo(name = "status_started") val statusStarted: Int?,
    @ColumnInfo(name = "status_finished") val statusFinished: Int?,
    @ColumnInfo(name = "current_submodule_id") val currentSubmoduleId: Long?,
    @ColumnInfo(name = "recorded_lat") val recordedLat: Double?,
    @ColumnInfo(name = "recorded_lng") val recordedLng: Double?
)


@Entity(tableName = "crs_question_type")
data class CRSResponseTypeDTO(
    @PrimaryKey
    var id: Int = 0,

    var name: String? = null,
    var isActive: Boolean = false
) : Serializable


@Entity(tableName = "crs_question")
data class CRSQuestionEntity(
    @PrimaryKey val questionId: Long,
    val responseType: Long? = null,//id for question type
    val questionText: String? = null,
    val moduleId: Long? = null,
    val moduleName: String? = null,//case to check whether need for frontend
    val questionStatus: String? = null,
    val helpText: String? = null,
    val questionCode: String? = null
) : Serializable


@Entity(
    tableName = "crs_question_modules",
    primaryKeys = ["questionId", "moduleId"],
    indices = [Index(value = arrayOf("moduleId"))]
)
data class CrsQuestionModuleXref(
    var questionId: Long = 0,
    var moduleId: Long = 0,

    var moduleName: String? = null,
    var moduleStatus: String? = null,
    var questionStatus: String? = null
) : Serializable


@Entity("crs_consents")
data class CrsConsent(
    @PrimaryKey
    val id:Long=0,
    val consentText: String?=null,
    val consentType: String?=null,
    val moduleName: String?=null,
    val consentPath: String?=null,
    val moduleId: Long?=null,
): Serializable


@Entity(tableName = "crs_options", indices = [Index(value = arrayOf("questionId"))])
data class CrsOptionEntity(
    @PrimaryKey
    var optionId: Long = 0,

    var questionId: Long = 0,
    var label: String? = null,
    var isDeleted: Boolean? = null
) : Serializable

@Entity(
    tableName = "crs_survey_instance",
    indices = [
        Index(value = ["farmerId"]),
        Index(value = ["moduleId"]),
        Index(value = ["status"]),
        Index(value = ["syncStatus"]),
        Index(value = ["startedAt"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = FarmerEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CRSModuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CrsSurveyInstance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // identity
    @ColumnInfo(name = "farmerId") val farmerId: String,
    @ColumnInfo(name = "moduleId") val moduleId: Long,

    // lifecycle
    @ColumnInfo(name = "status") val status: String,      // IN_PROGRESS, COMPLETED, CANCELLED
    @ColumnInfo(name = "startedAt") val startedAt: Long,  // epoch millis
    @ColumnInfo(name = "completedAt") val completedAt: Long? = null,

    // sync
    @ColumnInfo(name = "syncStatus") val syncStatus: String, // PENDING, READY, SYNCED, FAILED
    @ColumnInfo(name = "lastSyncAt") val lastSyncAt: Long? = null,
    @ColumnInfo(name = "lastSyncError") val lastSyncError: String? = null,

    // audit (optional)
    @ColumnInfo(name = "deviceId") val deviceId: String? = null,
    @ColumnInfo(name = "appVersion") val appVersion: String? = null,
    @ColumnInfo(name = "lat") val lat: Double? = null,
    @ColumnInfo(name = "lng") val lng: Double? = null,

    // server-side id (if backend creates one)
    @ColumnInfo(name = "serverInstanceId") val serverInstanceId: String? = null
) : Serializable


@Entity(
    tableName = "crs_answers",
    indices = [
        Index(value = ["instanceId"]),
        Index(value = ["questionId"]),
        Index(value = ["isSynced"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = CrsSurveyInstance::class,
            parentColumns = ["id"],
            childColumns = ["instanceId"],
            onDelete = ForeignKey.CASCADE
        )
        // Optionally FK to your crs_question & option tables if you have them
    ]
)
data class CrsAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // link to survey instance
    @ColumnInfo(name = "instanceId") val instanceId: Long,

    // identity
    @ColumnInfo(name = "questionId") val questionId: Long,
    @ColumnInfo(name = "optionId") val optionId: Long? = null,  // used for (PHOTO_)CHECKBOX/RADIO/DROPDOWN

    // value fields (use the relevant one(s))
    @ColumnInfo(name = "valueText") val valueText: String? = null,         // TEXTBOX, SIGNATURE (base64/file name), LOCATION (json), etc.
    @ColumnInfo(name = "valueNumber") val valueNumber: Double? = null,     // ratings, numeric
    @ColumnInfo(name = "valueInt") val valueInt: Int? = null,     // ratings, numeric
    @ColumnInfo(name = "valueBoolean") val valueBoolean: Boolean? = null,  // yes/no, checkbox single flag
    @ColumnInfo(name = "valueDate") val valueDate: Long? = null,           // date epoch millis
    @ColumnInfo(name = "valueJson") val valueJson: String? = null,         // flexible payloads (coordinates list, complex answers)

    // media (PHOTO/FILE_UPLOAD/PHOTO_CHECKBOX item)
    @ColumnInfo(name = "fileUri") val fileUri: String? = null,             // content:// or file://
    @ColumnInfo(name = "mimeType") val mimeType: String? = null,           // image/jpeg, application/pdf
    @ColumnInfo(name = "fileSizeBytes") val fileSizeBytes: Long? = null,
    @ColumnInfo(name = "remoteUrl") val remoteUrl: String? = null,         // set after upload

    // audit
    @ColumnInfo(name = "createdAt") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updatedAt") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "isSynced") val isSynced: Int = 1,  // 1 = needs sync, 0 = synced

    // backend id for individual answer (if server returns)
    @ColumnInfo(name = "serverAnswerId") val serverAnswerId: String? = null
)





data class ModuleWithQuestions(
    @Embedded val module: CRSModuleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "moduleId"
    )
    val questions: List<CRSQuestionEntity>
) : java.io.Serializable

data class QuestionWithOptions(
    @Embedded val question: CRSQuestionEntity,
    @Relation(
        parentColumn = "questionId",
        entityColumn = "questionId"
    )
    val options: List<CrsOptionEntity>
) : java.io.Serializable








