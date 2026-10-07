package com.olam.digital.crs.room

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.olam.digital.crs.model.surveysubmit.RecurrenceType
import kotlinx.datetime.Clock
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
    var rawJson: String? = null,
    var submitedCrsSurveyFarmerId: String? = null,
    var isCommunitySurvey: Boolean = false,
    var isConsentEnabled: Boolean = false,
    var communityIds: List<Int>? = null,
    var mandatoryQuestionIds: List<Int>? = null // comma-separated list of questionIds that become mandatory if this question is answered in a certain way (requires frontend logic to enforce)
) : Serializable


@Entity(
    tableName = "module_assignment",
    indices = [
        Index(value = ["moduleId"]),
        Index(value = ["moduleAssignmentType"]),
        Index(value = ["moduleAssignmentId"]),
        Index(value = ["countryId"])
    ]
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
    var recurrenceType: String? = null,
    var recurrenceFrequency: Int? = null,
    var disabledMonths: String? = null,

    // Missing fields added
    var active: Boolean? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var appId: String? = null,
    var surveyCode: String? = null,
    var isChildLabour: Boolean? = null,
    var fkFarmerRoleId: Long? = null,
    var isFarmSurvey: Boolean? = null,
    var isPlotSurvey: Boolean? = null,
    var isOfflineDownload: Boolean? = null
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


@Entity(
    tableName = "crs_mandatory_questions",
    primaryKeys = ["moduleId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = CRSModuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["moduleId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index(value = ["moduleId"]), Index(value = ["questionId"])]
)
data class CRSMandatoryQuestionEntity(
    val moduleId: Long,
    val questionId: Long
): Serializable





@Entity(tableName = "crs_question_type")
data class CRSResponseTypeDTO(
    @PrimaryKey
    var id: Int = 0,

    var name: String? = null,
    var isActive: Boolean = false
) : Serializable

@Entity("crs_community_profiles_mapping")
data class CrsCommunityProfileMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val uniqueId: Long = 0,
    val communityProfileId: Int? = null,
    val countryId: Int? = null,
) : Serializable


@Entity(tableName = "crs_question", primaryKeys = ["questionId","moduleId"], indices = [Index(value = ["moduleId"])])
data class CRSQuestionEntity(
    val questionId: Long,
    val responseType: Long? = null,//id for question type
    val questionText: String? = null,
    val moduleId: Long,
    val moduleName: String? = null,//case to check whether need for frontend
    val questionStatus: String? = null,
    val helpText: String? = null,
    val questionCode: String? = null,
    val isMandatory: Boolean? = null,
    val characterLimit: Long? = null,
    val decimalLimit: Long? = null,
    var questionText_fr: String? = null,
    var questionText_es: String? = null,
    var questionText_id: String? = null,
    var questionText_pt: String? = null,
    var questionText_tr: String? = null,
    var questionText_lo: String? = null,
    var questionText_vi: String? = null,
    var questionText_th: String? = null,
    var questionText_mr: String? = null,
    val displayOrder: Int? = null,
    val repeaterTimes: Int? = null,
    val moduleStatus: String? = null
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
    var questionStatus: String? = null,
    var mandatoryQuestionIds: String? = null // comma-separated list of questionIds that become mandatory if this question is answered in a certain way (requires frontend logic to enforce)
) : Serializable



@Entity(
    tableName = "crs_repeater_mappings",
    primaryKeys = ["moduleId", "parentQuestionId", "childQuestionId"],
    indices = [
        Index(value = ["moduleId"]),
        Index(value = ["parentQuestionId"]),
        Index(value = ["childQuestionId"])
    ]
)
data class CrsRepeaterMappingEntity(
    val moduleId: Long,

    // The controlling parent question
    val parentQuestionId: Long,

    // The child question this mapping points to
    val childQuestionId: Long,

    // Optional label for UI (e.g., "Member", "Plot")
    val repeatLabel: Int? = null,

    // Display order for child block (helps when multiple children exist)
    val displayOrder: Int? = null
): Serializable



@Entity(tableName = "conditional_question_details")
data class ConditionalQuestionDetailEntity(
    @PrimaryKey val id: Long,
    val questionId: Long,
    val conditionalId: Long,
    val conditionalText: String?,
    val isAnswered: Boolean?,
    val moduleId: Long?,
    val subModuleId: Long?,
    val isDeleted: Boolean?,
    val lookupValues: String?,
    val skipQuestions: String?,
    val lookupIds: String?,
    val questionsDraftDtoList: String?, // store raw JSON if needed
    val skipQuestionIds: String?,
    val lookUpValueIds: String?
): Serializable

@Entity(tableName = "crs_recurrence_type")
data class RecurrenceTypeEntity(
    @PrimaryKey val id: Long,
    val value: String?,
    val active: Boolean?
):Serializable

@Entity(tableName = "crs_survey_duration_type")
data class SurveyDurationTypeEntity(
    @PrimaryKey val id: Long,
    val value: String?,
    val active: Boolean?
): Serializable

@Entity(
    tableName = "conditional_type",
    primaryKeys = ["id"]
)
data class ConditionalTypeEntity(
    val id: Long,
    val value: String? = null,
    val operator: String? = null,
    val active: Boolean? = null,
    val questionType: String? = null,      // supports multiple values like "2,5,7,11"
    val crsQuestionType: String? = null    // supports multiple values like "3,4,5,7"
) : Serializable




    @Entity("crs_consents")
data class CrsConsent(
    @PrimaryKey
    val id: Long = 0,
    val consentText: String? = null,
    val consentType: String? = null,
    val moduleName: String? = null,
    val consentPath: String? = null,
    val moduleId: Long? = null,
) : Serializable


@Entity(tableName = "crs_options", indices = [Index(value = arrayOf("questionId"))])
data class CrsOptionEntity(
    @PrimaryKey
    var optionId: Long = 0,
    var questionId: Long = 0,
    var label: String? = null,
    var isDeleted: Boolean? = null,
    val options_fr: String? = null,
    val options_es: String? = null,
    val options_id: String? = null,
    val options_pt: String? = null,
    val options_tr: String? = null,
    val options_lo: String? = null,
    val options_vi: String? = null,
    val options_th: String? = null,
    val options_mr: String? = null
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
    @ColumnInfo("isCrsSurvey") val isCrsSurvey: Int = 1, // 1 = true, 0 = false

    @ColumnInfo(name = "crsSignature")
    val crsSignature: String? = null,

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
    @ColumnInfo(name = "serverAnswerId") val serverAnswerId: String? = null,
    @ColumnInfo(name = "repeatIndex") val repeatIndex: Int? = null,   // NEW: distinguish repeated answers
    @ColumnInfo(name = "repeaterId") val repeaterId: Long? = null    // NEW: link to CrsRepeaterMappingEntity if this answer is part of a repeated block
): Serializable


/*Room for Countries*/

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val id: Int,
    val code: String?,
    val name: String?,
    val dialling_code: String?,
    val olam_country_id: String?,
    val enumerator_country: String?,
    val isSubdistrictCapture: Boolean?,
    val countryIsoCode: String?
) : Serializable

@Entity(
    tableName = "regions",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["id"],
            childColumns = ["country_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("country_id")]
)
data class RegionEntity(
    @PrimaryKey val id: Int,
    val name: String?,
    @ColumnInfo(name = "country_id") val countryId: Int
) : Serializable


@Entity(
    tableName = "farmergroups",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("product_id"),
        Index("programme_id"),
        Index("partner_id")
    ]
)
data class FarmerGroupEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "programme_id")
    val programmeId: Int? = null,

    @ColumnInfo(name = "partner_id")
    val partnerId: Int? = null,

    @ColumnInfo(name = "product_id")
    val productId: Int? = null,

    @ColumnInfo(name = "code")
    val code: String? = null,

    @ColumnInfo(name = "name")
    val name: String? = null
) : Serializable

@Entity(
    tableName = "districts",
    foreignKeys = [
        ForeignKey(
            entity = RegionEntity::class,
            parentColumns = ["id"],
            childColumns = ["region_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("region_id")]
)
data class DistrictEntity(
    @PrimaryKey val id: Int,
    val name: String?,
    @ColumnInfo(name = "region_id") val regionId: Int
) : Serializable

@Entity(
    tableName = "subdistricts",
    foreignKeys = [
        ForeignKey(
            entity = DistrictEntity::class,
            parentColumns = ["id"],
            childColumns = ["district_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("district_id")]
)
data class SubDistrictEntity(
    @PrimaryKey val id: Int,
    val name: String?,
    @ColumnInfo(name = "district_id") val districtId: Int
) : Serializable

@Entity(
    tableName = "places",
    indices = [Index("district_id"), Index("subdistrictId")]
)
data class PlaceEntity(
    @PrimaryKey val id: Int,
    val name: String?,
    val district_id: Int,
    val subdistrictId: Int?,
    val type: String?
) : Serializable


@Entity(
    tableName = "sections",
    foreignKeys = [
        ForeignKey(
            entity = FarmerGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmergroup_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("farmergroup_id")]
)
data class SectionEntity(
    @PrimaryKey val id: Int,
    val code: String?,
    val name: String?,
    @ColumnInfo(name = "farmergroup_id") val farmerGroupId: Int
) : Serializable


@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val name: String?
)

@Entity("crs_community_regions")
data class CrsCommunityRegionEntity(
    @PrimaryKey val id: Int,
    val countryId: String? = null,
    val name: String? = null
) : Serializable


@Entity("crs_profiles")
data class CrsProfileEntity(
    @PrimaryKey val id: Int,
    val profileName: String? = null,
    val isActive: Boolean = false
) : Serializable


@Entity(
    tableName = "crs_community_region_mapping",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["id"],
            childColumns = ["country_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RegionEntity::class,
            parentColumns = ["id"],
            childColumns = ["region_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = DistrictEntity::class,
            parentColumns = ["id"],
            childColumns = ["district_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SubDistrictEntity::class,
            parentColumns = ["id"],
            childColumns = ["subdistrict_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["place_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = FarmerGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmergroup_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("crs_community_region_id"),
        Index("country_id"),
        Index("region_id"),
        Index("district_id"),
        Index("subdistrict_id"),
        Index("place_id"),
        Index("farmergroup_id"),
        Index("product_id")
    ]
)
data class CrsCommunityRegionMappingEntity(

    @PrimaryKey(autoGenerate = true)
    val mappingId: Int = 0,

    @ColumnInfo(name = "crs_community_region_id")
    val crsCommunityRegionId: Int,

    @ColumnInfo(name = "country_id")
    val countryId: Int? = null,

    @ColumnInfo(name = "region_id")
    val regionId: Int? = null,

    @ColumnInfo(name = "district_id")
    val districtId: Int? = null,

    @ColumnInfo(name = "subdistrict_id")
    val subdistrictId: Int? = null,

    @ColumnInfo(name = "place_id")
    val placeId: Int? = null,

    @ColumnInfo(name = "farmergroup_id")
    val farmerGroupId: Int? = null,

    @ColumnInfo(name = "product_id")
    val productId: Int? = null,

    @ColumnInfo("sectionId")
    val sectionId: Int? = null
) : Serializable


@Entity(tableName = "crs_respondents")
data class RespondentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String? = null,
    val countryId: Int? = null,
    val regionId: Int? = null,
    val profileId: Int? = null,
    val productId: Int? = null,
    val districtId: Int? = null,
    val placeId: Int? = null,
    val emailId: String? = null,
    val countryCode: String? = null,
    val phoneNumber: String? = null,
    val respondentCode: String? = null,
    var photo: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    @ColumnInfo("isSynced")
    val isSynced: Int = 1, // 1 = needs sync, 0 = synced
    @ColumnInfo("created_at")
    val createdAt: Long = System.currentTimeMillis(),
) : Serializable


//@Entity(tableName = "SurveyDurationType")
data class SurveyDurationType (
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @ColumnInfo(name = "value")
    var value: String? = null,
    @ColumnInfo(name = "active")
    var active: Boolean = false
):Serializable

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


data class SurveyWithAnswers(
    @Embedded val instance: CrsSurveyInstance,
    @Relation(
        parentColumn = "id",
        entityColumn = "instanceId",
        entity = CrsAnswerEntity::class
    )
    val answers: List<CrsAnswerEntity>
) : Serializable


data class SubmissionContext(
    var stakeholderId: Int = 0,
    var olamEnumeratorId: Int = 0,
    var isWebEdit: Boolean = false,
    var fmPreview: Boolean = false,
    var uploadAttempts: Int = 1,
    var uploadId: String = "LOCAL-" + Clock.System.now().toEpochMilliseconds().toString(),
    var lastEditUserId: Int = 0,
    var isReviewEdit: Boolean = false,
    var isUploadJob: Boolean = true,
    var updateReason: String = "Auto-generated from local instance",
    var recurrenceFrequency: Int = 0,
    var recurrenceType: RecurrenceType = RecurrenceType.MONTHLY,
    var appVersion: String? = null
) : Serializable

@Keep
data class ProfileWithMapping(
    @Embedded val profile: CrsProfileEntity,
    @Embedded val mapping: CrsCommunityProfileMappingEntity
) : Serializable


@Keep
data class ConditionalRule(val keyword: String, val childCount: Int) : Serializable

val conditionalRules = listOf(
    ConditionalRule("Besides sugarcane, do you farm any other crop as well?", 4),
    ConditionalRule("Besides rice, did you produce and sell any other crops during the past 12 months?", 8)
    // Add more rules here
)




