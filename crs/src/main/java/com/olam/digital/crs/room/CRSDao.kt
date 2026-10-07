package com.olam.digital.crs.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.olam.digital.crs.model.AnswerRow
import com.olam.digital.crs.model.mapper.FarmerModuleView

@Dao
interface CRSDao {

    // Insert or replace a single farmer
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: FarmerEntity)

    // Insert or replace multiple farmers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmers(farmers: List<FarmerEntity>)

    // Update a farmer record
    @Update
    suspend fun updateFarmer(farmer: FarmerEntity)

    // Delete one farmer
    @Delete
    suspend fun deleteFarmer(farmer: FarmerEntity)

    @Query("Delete from farmers_crs")
    suspend fun deleteFarmers()

    // Get all farmers
    @Query("SELECT * FROM farmers_crs ORDER BY name ASC")
    suspend fun getAllFarmers(): List<FarmerEntity>


    @Query("SELECT * FROM crs_respondents ORDER BY name ASC")
    suspend fun getAllRespondents(): List<RespondentEntity>

    // Get farmer by ID
    @Query("SELECT * FROM farmers_crs WHERE id = :id LIMIT 1")
    suspend fun getFarmerById(id: String): FarmerEntity?

    // Search by farmer name
    @Query("SELECT * FROM farmers_crs WHERE name LIKE '%' || :search || '%' ORDER BY name ASC")
    suspend fun searchFarmers(search: String): List<FarmerEntity>

    // Count farmers
    @Query("SELECT COUNT(*) FROM farmers_crs")
    suspend fun getFarmerCount(): Int

    // Delete all farmers
    @Query("DELETE FROM farmers_crs")
    suspend fun clearFarmers()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<CRSModuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<CRSQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<CrsOptionEntity>)

    // Module + questions (no options)
    @Transaction
    @Query("SELECT * FROM crs_modules WHERE id = :moduleId")
    suspend fun getModuleWithQuestions(moduleId: Long): ModuleWithQuestions?

    // Question + options
    @Transaction
    @Query(
        "SELECT * FROM crs_question WHERE moduleId = :moduleId " +
                "AND (questionStatus IS NULL OR questionStatus = 'ACTIVE')  ORDER BY displayOrder ASC "
    )
    suspend fun getQuestionsByModule(moduleId: Long): List<CRSQuestionEntity>

    @Query("SELECT * FROM conditional_type")
    suspend fun getAllConditionalTypes(): List<ConditionalTypeEntity>
    @Query("SELECT * FROM conditional_question_details WHERE moduleId = :moduleId")
    suspend fun getConditionalQuestionDetails(moduleId: Long): List<ConditionalQuestionDetailEntity>
    @Transaction
    @Query(
        """
    SELECT q.*, 
           CASE WHEN m.questionId IS NOT NULL THEN 1 ELSE 0 END AS isMandatory
    FROM crs_question q
    LEFT JOIN crs_mandatory_questions m 
           ON q.moduleId = m.moduleId AND q.questionId = m.questionId
    WHERE q.moduleId = :moduleId
      AND (q.questionStatus IS NULL OR q.questionStatus = 'ACTIVE')
    ORDER BY q.displayOrder ASC
"""
    )
    suspend fun getQuestionsByModuleWithManitory(moduleId: Long): List<CRSQuestionEntity>

    @Query("SELECT * FROM crs_repeater_mappings WHERE moduleId = :moduleId")
    suspend fun getRepeaterMappingsForModule(moduleId: Long): List<CrsRepeaterMappingEntity>

    @Transaction
    @Query("SELECT * FROM crs_question WHERE moduleId = :moduleId  ORDER BY displayOrder ASC")
    suspend fun getQuestionsByModuleId(moduleId: Long): List<CRSQuestionEntity>

    @Transaction
    @Query("SELECT * FROM crs_options WHERE questionId = :questionId AND isDeleted = 0")
    suspend fun getOptionsForQuestion(questionId: Long): List<CrsOptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCrsRepeaterMapping(mappings: List<CrsRepeaterMappingEntity>)

    @Query("SELECT * FROM crs_repeater_mappings WHERE moduleId = :moduleId AND parentQuestionId = :parentId")
    suspend fun getChildrenForParent(moduleId: Long, parentId: Long): List<CrsRepeaterMappingEntity>


    // ResponseType lookups
    @Query("SELECT * FROM crs_question_type")
    suspend fun getAllResponseTypes(): List<CRSResponseTypeDTO>

    @Query("SELECT * FROM crs_question_type WHERE id = :id")
    suspend fun getResponseTypeById(id: Long): CRSResponseTypeDTO?

    //insert manitory questions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMandatoryQuestions(entities: List<CRSMandatoryQuestionEntity>)


    @Query("DELETE FROM CRS_MANDATORY_QUESTIONS")
    suspend fun deleteMandatoryQuestions()

    @Query("DELETE FROM CRS_REPEATER_MAPPINGS")
    suspend fun deleteRepeaterCrsQuestions()

    // Inserts/Upserts (replace on conflict)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModules(modules: List<CRSModuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestions(questions: List<CRSQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOptions(options: List<CrsOptionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCrsQuestionModuleXref(options: List<CrsQuestionModuleXref>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResponseTypes(types: List<CRSResponseTypeDTO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModuleAssignments(types: List<ModuleAssignmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConsents(types: List<CrsConsent>)


    @Query(
        """
    SELECT DISTINCT
        m.id AS module_id,
        m.name,
        m.surveyType,
        m.channel,
        m.submitedCrsSurveyFarmerId AS submitedCrsSurveyFarmerId,
        m.rawJson AS moduleRawJson,
        m.communityIds,
         m.mandatoryQuestionIds,
         m.isConsentEnabled,
        ma.surveyCode,

        ma.moduleAssignmentId AS assignmentId,
        ma.moduleAssignmentType AS assignmentType,
        ma.countryId,
        ma.startDateEpoch,
        ma.surveyDuration,
        ma.recurrenceType,
        ma.recurrenceFrequency,
        ma.disabledMonths,
        ma.viewModule,
        ma.rawJson AS assignmentRawJson,
        ma.createdAt,
        ma.updatedAt,
        ma.appId,

        fm.id AS farmerModuleId,

        CASE WHEN EXISTS (
            SELECT 1
            FROM crs_survey_instance si
            WHERE si.moduleId = m.id
              AND si.farmerId = :farmerPrimaryKeyId
        ) THEN 1 ELSE 0 END AS statusStarted,

        CASE WHEN EXISTS (
            SELECT 1
            FROM crs_survey_instance si
            WHERE si.moduleId = m.id
              AND si.farmerId = :farmerPrimaryKeyId
              AND si.status = 'COMPLETED'
              AND si.syncStatus = 'SYNCED'
        ) THEN 1 ELSE 0 END AS statusFinished,

        CASE WHEN EXISTS (
            SELECT si.id
            FROM crs_survey_instance si
            WHERE si.moduleId = m.id AND si.farmerId = :farmerPrimaryKeyId
        ) THEN (
            SELECT si.id
            FROM crs_survey_instance si
            WHERE si.moduleId = m.id AND si.farmerId = :farmerPrimaryKeyId
            ORDER BY si.startedAt DESC LIMIT 1
        ) ELSE NULL END AS surveyInstanceId,

        fm.current_submodule_id AS currentSubmoduleId,
        fm.recorded_lat AS recordedLat,
        fm.recorded_lng AS recordedLng,

        (
          SELECT COUNT(*)
          FROM crs_question qx
          WHERE qx.moduleId = m.id
        ) AS questionCount,

        c.id AS consentId,
        c.consentText,
        c.consentType,
        c.consentPath

    FROM module_assignment ma
    INNER JOIN crs_modules m ON m.id = ma.moduleId
    LEFT JOIN farmer_modules fm ON fm.module_id = m.id
        AND (fm.farmer_id = :farmerId OR (:olamFarmerId IS NOT NULL AND fm.olam_farmer_id = :olamFarmerId))
    LEFT JOIN crs_consents c ON c.moduleId = m.id

    WHERE
        (:excludeSurveyType8 = 0 OR m.surveyType != 8)
        AND m.isCommunitySurvey = 1
        AND (:countryId IS NULL OR ma.countryId = :countryId)
        AND (:includeAuto = 0 OR m.id IN (:autoSampleIds))
    GROUP BY m.id
    """
    )
    suspend fun getCrsModulesAgainstFarmer(
        farmerId: String,
        olamFarmerId: String?,
        countryId: Long?,
        autoSampleIds: List<Long>,
        includeAuto: Int,
        excludeSurveyType8: Int,
        farmerPrimaryKeyId: String
    ): List<FarmerModuleView>

    /*
    *      json_extract(ma.rawJson, '$.isChildLabour') AS isChildLabour,
            json_extract(ma.rawJson, '$.isFarmSurvey') AS isFarmSurvey,
            json_extract(ma.rawJson, '$.isPlotSurvey') AS isPlotSurvey,
            json_extract(ma.rawJson, '$.isOfflineDownload') AS isOfflineDownload
    */


    @Query(
        """
    SELECT DISTINCT
        m.id AS module_id,
        m.name,
        m.surveyType,
        m.channel,
        m.submitedCrsSurveyFarmerId AS submitedCrsSurveyFarmerId,
        m.rawJson AS moduleRawJson,
        m.mandatoryQuestionIds,
        m.isConsentEnabled,
        ma.surveyCode,

        ma.moduleAssignmentId AS assignmentId,
        ma.moduleAssignmentType AS assignmentType,
        ma.countryId,
        ma.startDateEpoch,
        ma.surveyDuration,
        ma.recurrenceType,
        ma.recurrenceFrequency,
        ma.disabledMonths,
        ma.viewModule,
        ma.rawJson AS assignmentRawJson,
        ma.createdAt,
        ma.updatedAt,
        ma.appId,

        fm.id AS farmerModuleId,

        CASE WHEN EXISTS (
            SELECT 1
            FROM crs_survey_instance si
            WHERE si.moduleId = m.id
              AND si.farmerId = :farmerPrimaryKeyId
        ) THEN 1 ELSE 0 END AS statusStarted,

        CASE WHEN EXISTS (
            SELECT 1
            FROM crs_survey_instance si
            WHERE si.moduleId = m.id
              AND si.farmerId = :farmerPrimaryKeyId
              AND si.status = 'COMPLETED'
              AND si.syncStatus = 'SYNCED'
        ) THEN 1 ELSE 0 END AS statusFinished,

        CASE WHEN EXISTS (
            SELECT si.id
            FROM crs_survey_instance si
            WHERE si.moduleId = m.id AND si.farmerId = :farmerPrimaryKeyId
        ) THEN (
            SELECT si.id
            FROM crs_survey_instance si
            WHERE si.moduleId = m.id AND si.farmerId = :farmerPrimaryKeyId
            ORDER BY si.startedAt DESC LIMIT 1
        ) ELSE NULL END AS surveyInstanceId,

        fm.current_submodule_id AS currentSubmoduleId,
        fm.recorded_lat AS recordedLat,
        fm.recorded_lng AS recordedLng,

        (
          SELECT COUNT(*)
          FROM crs_question qx
          WHERE qx.moduleId = m.id
        ) AS questionCount,

        c.id AS consentId,
        c.consentText,
        c.consentType,
        c.consentPath

        -- flags from rawJson if needed
   
    FROM module_assignment ma
    INNER JOIN crs_modules m ON m.id = ma.moduleId
    LEFT JOIN farmer_modules fm ON fm.module_id = m.id
        AND (fm.farmer_id = :farmerId OR (:olamFarmerId IS NOT NULL AND fm.olam_farmer_id = :olamFarmerId))
    LEFT JOIN crs_consents c ON c.moduleId = m.id

    WHERE
        (:excludeSurveyType8 = 0 OR m.surveyType != 8)
        AND m.isCommunitySurvey = 0
        AND (:countryId IS NULL OR ma.countryId = :countryId)
        AND (
            (ma.moduleAssignmentType = 'Product'      AND ma.moduleAssignmentId = COALESCE(:productId, -1))
            OR (ma.moduleAssignmentType = 'Region'    AND ma.moduleAssignmentId = COALESCE(:regionId, -1))
            OR (ma.moduleAssignmentType = 'District'  AND ma.moduleAssignmentId = COALESCE(:districtId, -1))
            OR (ma.moduleAssignmentType = 'Place'     AND ma.moduleAssignmentId = COALESCE(:placeId, -1))
            OR (ma.moduleAssignmentType = 'Farmer Group' AND ma.moduleAssignmentId = COALESCE(:farmerGroupId, -1))
            OR (ma.moduleAssignmentType = 'Section'   AND ma.moduleAssignmentId = COALESCE(:sectionId, -1))
            OR (ma.moduleAssignmentType = 'Programme' AND ma.moduleAssignmentId = COALESCE(:programmeId, -1))
            OR (ma.moduleAssignmentType = 'Partner'   AND ma.moduleAssignmentId = COALESCE(:partnerId, -1))
        )
        AND (:includeAuto = 0 OR m.id IN (:autoSampleIds))
    GROUP BY m.id
    """
    )
    suspend fun getModulesAgainstFarmer(
        farmerId: String,
        olamFarmerId: String?,
        countryId: Long?,
        productId: Long?,
        regionId: Long?,
        districtId: Long?,
        placeId: Long?,
        farmerGroupId: Long?,
        sectionId: Long?,
        programmeId: Long?,
        partnerId: Long?,
        autoSampleIds: List<Long>,
        includeAuto: Int,
        excludeSurveyType8: Int,
        farmerPrimaryKeyId: String
    ): List<FarmerModuleView>


    @Query("update crs_modules set submitedCrsSurveyFarmerId=:moduleId where id=:moduleId")
    suspend fun updateModuleAlreadySubmitted(moduleId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCrsSurveySyncData(instance: CrsSurveyInstance): Long

    @Update
    suspend fun updateSurveyInstanceSyncData(instance: CrsSurveyInstance)

    @Query("SELECT * FROM crs_survey_instance WHERE id = :id")
    suspend fun getById(id: Long): CrsSurveyInstance?

    @Query(
        """
        SELECT * FROM crs_survey_instance
        WHERE farmerId = :farmerId AND moduleId = :moduleId
        ORDER BY startedAt DESC LIMIT 1
    """
    )
    suspend fun getLatestForFarmerModule(farmerId: String, moduleId: Long): CrsSurveyInstance?

    @Query(
        """
        SELECT * FROM crs_survey_instance
        WHERE syncStatus IN ('PENDING','READY','FAILED')
        ORDER BY startedAt ASC
        LIMIT :limit
    """
    )
    suspend fun getInstancesNeedingSync(limit: Int = 50): List<CrsSurveyInstance>


    @Query(
        """
        SELECT * FROM crs_survey_instance
        WHERE syncStatus IN ('PENDING','READY','FAILED')
        ORDER BY startedAt ASC
    """
    )
    suspend fun getInstancesNeedingSync(): List<CrsSurveyInstance>


    @Query(
        """
        UPDATE crs_survey_instance
        SET syncStatus = :syncStatus,
            lastSyncAt = :lastSyncAt,
            lastSyncError = :error
        WHERE id = :id
    """
    )
    suspend fun updateSyncState(id: Long, syncStatus: String, lastSyncAt: Long?, error: String?)


    @Query("SELECT * FROM crs_modules WHERE id = :moduleId")
    suspend fun getModuleById(moduleId: Long): CRSModuleEntity?


    @Query("SELECT * FROM crs_options WHERE optionId IN (:ids)")
    suspend fun getOptionsByIds(ids: List<Long>): List<CrsOptionEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg answers: CrsAnswerEntity)

    @Update
    suspend fun update(vararg answers: CrsAnswerEntity)

    @Query("SELECT * FROM crs_answers WHERE instanceId = :instanceId")
    suspend fun getByInstance(instanceId: Long): List<CrsAnswerEntity>

    @Query(
        """
        SELECT * FROM crs_answers 
        WHERE instanceId = :instanceId AND isSynced = 1
    """
    )
    suspend fun getDirtyByInstance(instanceId: Long): List<CrsAnswerEntity>

    @Transaction
    @Query("SELECT * FROM crs_survey_instance WHERE id = :instanceId")
    suspend fun getSurveyWithAnswers(instanceId: Long): SurveyWithAnswers?

    @Query(
        """
        UPDATE crs_answers SET isSynced = 0, updatedAt = :now 
        WHERE instanceId = :instanceId
    """
    )
    suspend fun markInstanceAnswersSynced(instanceId: Long, now: Long = System.currentTimeMillis())

    @Query(
        """
        DELETE FROM crs_answers 
        WHERE instanceId IN (
            SELECT id FROM crs_survey_instance 
            WHERE syncStatus = 'SYNCED' 
              AND completedAt IS NOT NULL 
              AND (strftime('%s','now')*1000 - completedAt) > :retentionMs
        )
    """
    )
    suspend fun purgeOldSynced(retentionMs: Long = 30L * 24 * 60 * 60 * 1000) // 30 days


    @Query("UPDATE crs_survey_instance SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun markCompleted(
        id: Long,
        status: String = "COMPLETED",
        completedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE crs_survey_instance SET syncStatus = :syncStatus, lastSyncAt = :lastSyncAt, lastSyncError = :error WHERE id = :id")
    suspend fun updateSync(id: Long, syncStatus: String, lastSyncAt: Long?, error: String?)


    @Insert
    suspend fun insertAllAnswerEntity(answers: List<CrsAnswerEntity>)

    @Query("DELETE FROM crs_answers WHERE instanceId = :instanceId")
    suspend fun deleteByInstance(instanceId: Long)

    // Optional upsert if you want to overwrite per questionId
    @Transaction
    suspend fun replaceAnswers(instanceId: Long, newAnswers: List<CrsAnswerEntity>) {
        deleteByInstance(instanceId)
        if (newAnswers.isNotEmpty()) insertAllAnswerEntity(newAnswers)
    }


    @Query("DELETE from  crs_survey_instance where id=:surveyInstance")
    suspend fun updateCrsSurveyInstance(surveyInstance: Long): Int


    @Query("select surveyDuration from module_assignment where moduleId=:moduleId")
    suspend fun getDurationsForModule(moduleId: Long): Int


    @Query("select startDateEpoch from module_assignment where moduleId=:moduleId")
    suspend fun getStartDateEpochForModule(moduleId: Long): String?

    @Query("select recurrenceType from module_assignment where moduleId=:moduleId")
    suspend fun getRecurrenceTypeForModule(moduleId: Long): String?


    @Query("select recurrenceFrequency from module_assignment where moduleId=:moduleId")
    suspend fun getRecurrenceFrequencyForModule(moduleId: Long): Int?

    @Transaction
    suspend fun replaceOldCrsSurveyInstanceAfterSyncSuccess(surveyInstanceId: Long): Int {
        val updatedId = updateCrsSurveyInstance(surveyInstanceId)
        deleteByInstance(instanceId = surveyInstanceId)
        return updatedId
    }


    @Query(
        """
        SELECT
            a.questionId     AS questionId,
            a.optionId       AS optionId,
            a.valueText      AS valueText,
            a.valueNumber    AS valueNumber,
            a.valueInt       AS valueInt,
            a.valueBoolean   AS valueBoolean,
            a.valueDate      AS valueDate,
            a.valueJson      AS valueJson,
            a.fileUri        AS fileUri,
            a.remoteUrl      AS remoteUrl,
            a.updatedAt      AS updatedAt,
            a.repeaterId    AS repeaterId,
            a.repeatIndex AS repeatIndex
        FROM crs_answers a
        JOIN crs_survey_instance si ON si.id = a.instanceId
        WHERE si.moduleId = :moduleId
          AND (:surveyInstanceId IS NULL OR a.instanceId      = :surveyInstanceId) """
    )
    suspend fun getAnswersForModule(
        moduleId: Long,
        surveyInstanceId: Long?,   // optional
    ): List<AnswerRow>


    @Query("SELECT * FROM crs_options WHERE questionId IN (:questionIds)")
    suspend fun getOptionsForQuestionIds(questionIds: List<Long>): List<CrsOptionEntity>


    @Query(
        """
        SELECT count(*) FROM crs_survey_instance
        WHERE syncStatus IN ('PENDING','READY','FAILED')
        ORDER BY startedAt ASC
    """
    )
    suspend fun getInstancesCountNeedingSync(): Long


    /*Photo CheckBox*/
    @Query(
        """
    SELECT * FROM crs_options
    WHERE label IS NOT NULL
    AND (
        LOWER(label) LIKE '%.jpg' OR
        LOWER(label) LIKE '%.jpeg' OR
        LOWER(label) LIKE '%.png'
    )
"""
    )
    suspend fun getPhotoCheckboxOptions(): List<CrsOptionEntity>


    @Query(
        """
        SELECT * FROM crs_consents
        WHERE consentPath IS NOT NULL
        AND (
            LOWER(consentPath) LIKE '%.pdf' OR
            LOWER(consentPath) LIKE '%.doc' OR
            LOWER(consentPath) LIKE '%.jpg' OR
            LOWER(consentPath) LIKE '%.jpeg' OR
            LOWER(consentPath) LIKE '%.png' OR
            LOWER(consentPath) LIKE '%.docx'
        )
    """
    )
    suspend fun getConsentFiles(): List<CrsConsent>


    @Query("UPDATE crs_options SET label = :localPath WHERE optionId = :optionId and questionId=:questionId")
    suspend fun updateOptionLabelWithLocalPath(
        optionId: Long,
        localPath: String,
        questionId: Long
    ): Int


    @Query("UPDATE crs_consents SET consentText = :localPath WHERE id=:consentId")
    suspend fun updateOptionLabelWithLocalPath(
        consentId: Long,
        localPath: String
    ): Int


    @Query("delete from crs_options")
    suspend fun deleteOptionBatch()

    @Query("delete from crs_question")
    suspend fun deleteAllQuestions()

    @Query("delete from crs_modules")
    suspend fun deleteAllModules()

    @Query("delete from crs_question_type")
    suspend fun questionResponseType()

    @Query("delete from crs_consents")
    suspend fun deleteConsentData()

    @Query("delete from module_assignment")
    suspend fun deleteModuleAssignments()


    @Query("delete from farmer_modules")
    suspend fun deleteFarmerModuleAssignments()


    /*Crs Countryies Crs*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(entity: CrsCommunityRegionMappingEntity)


    @Query("SELECT * FROM crs_community_region_mapping WHERE crs_community_region_id = :regionId")
    suspend fun getMapping(regionId: Int): CrsCommunityRegionMappingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCrsCommunityRegionMapping(list: List<CrsCommunityRegionMappingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityRegion(list: List<CrsCommunityRegionEntity>)


    @Query("Delete From crs_community_regions")
    suspend fun deleteAllCrsCommunityRegion()

    @Query("Delete From crs_community_region_mapping")
    suspend fun deleteAllCrsCommunityMapping()


    @Query("SELECT * FROM countries")
    suspend fun getAllCountry(): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE id = :id")
    suspend fun getCountryById(id: Int): CountryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCountry(list: List<CountryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CountryEntity)

    @Delete
    suspend fun delete(entity: CountryEntity)

    @Query("Delete From countries")
    suspend fun deleteAllCountries()


    @Query("SELECT * FROM regions")
    suspend fun getAllRegion(): List<RegionEntity>

    @Query("SELECT * FROM regions WHERE id = :id")
    suspend fun getRegionById(id: Int): RegionEntity?

    @Query("SELECT * FROM regions WHERE country_id = :countryId")
    suspend fun getByCountry(countryId: Int): List<RegionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRegion(list: List<RegionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RegionEntity)

    @Query("Delete From regions")
    suspend fun deleteAllRegions()


    @Query("SELECT * FROM districts")
    suspend fun getAllDistrict(): List<DistrictEntity>

    @Query("SELECT * FROM districts WHERE id = :id")
    suspend fun getDistrictById(id: Int): DistrictEntity?

    @Query("SELECT * FROM districts WHERE region_id = :regionId")
    suspend fun getByRegion(regionId: Int): List<DistrictEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDistrict(list: List<DistrictEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DistrictEntity)


    @Query("Delete From districts")
    suspend fun deleteAllDistricts()

    @Query("SELECT * FROM subdistricts")
    suspend fun getAllSubDistricts(): List<SubDistrictEntity>

    @Query("SELECT * FROM subdistricts WHERE id = :id")
    suspend fun getSubDistrictById(id: Int): SubDistrictEntity?

    @Query("SELECT * FROM subdistricts WHERE district_id = :districtId")
    suspend fun getByDistrict(districtId: Int): List<SubDistrictEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSubDistrict(list: List<SubDistrictEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SubDistrictEntity)

    @Query("Delete From subdistricts")
    suspend fun deleteAllSubDistricts()


    @Query("SELECT * FROM places")
    suspend fun getAllPlaces(): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceById(id: Int): PlaceEntity?

    @Query("SELECT * FROM places WHERE district_id = :districtId")
    suspend fun getByDistrictPlace(districtId: Int): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE subdistrictId = :subdistrictId")
    suspend fun getBySubDistrict(subdistrictId: Int): List<PlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlace(list: List<PlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCommunityProfileMapping(list: List<CrsCommunityProfileMappingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PlaceEntity)


    @Query("Delete From places")
    suspend fun deleteAllPlaces()


    @Query("SELECT * FROM farmergroups")
    suspend fun getAllFarmerGroups(): List<FarmerGroupEntity>

    @Query("SELECT * FROM farmergroups WHERE id = :id")
    suspend fun getFarmerGroupById(id: Int): FarmerGroupEntity?

    @Query("SELECT * FROM farmergroups WHERE product_id = :productId")
    suspend fun getByProduct(productId: Int): List<FarmerGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFarmerGroupEntity(list: List<FarmerGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FarmerGroupEntity)

    @Query("Delete From farmergroups")
    suspend fun deleteAllFarmerGroups()


    @Query("SELECT * FROM sections")
    suspend fun getAllSections(): List<SectionEntity>

    @Query("SELECT * FROM sections WHERE id = :id")
    suspend fun getSectionById(id: Int): SectionEntity?

    @Query("SELECT * FROM sections WHERE farmergroup_id = :farmerGroupId")
    suspend fun getByFarmerGroup(farmerGroupId: Int): List<SectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSectionEntity(list: List<SectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SectionEntity)


    @Query("Delete From sections")
    suspend fun deleteAllSections()

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProductEntity(list: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProductEntity)

    @Query("Delete From products")
    suspend fun deleteAllProducts()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRespondent(respondent: RespondentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRespondents(list: List<RespondentEntity>)

    @Query("SELECT * FROM crs_respondents WHERE isSynced=1")
    suspend fun getAllDirtyRespondents(): List<RespondentEntity>


    @Query("update crs_respondents set isSynced=0 where id = :ids")
    suspend fun updateRespondentSynced(ids: Int)

    @Query("SELECT * FROM crs_respondents where profileId = :selectedProfile")
    suspend fun getAllCrsRespondents(selectedProfile: Int): List<RespondentEntity>

    @Query("SELECT * FROM crs_respondents WHERE id = :id")
    suspend fun getRespondentById(id: Int): RespondentEntity?

    @Delete
    suspend fun deleteRespondent(entity: RespondentEntity)

    @Query("DELETE FROM crs_respondents")
    suspend fun deleteAllRespondents()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCrsProfileEntity(profiles: List<CrsProfileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: CrsProfileEntity)

    @Query("SELECT * FROM crs_profiles where id in (select communityProfileId from crs_community_profiles_mapping)")
    suspend fun getAllCrsProfile(): List<CrsProfileEntity>


    @Query(
        """
    SELECT p.*, m.*
    FROM crs_profiles AS p
    INNER JOIN crs_community_profiles_mapping AS m
        ON p.id = m.communityProfileId
"""
    )
    fun getProfilesWithCommunityMapping(): List<ProfileWithMapping>

    @Query("SELECT id FROM crs_profiles")
    suspend fun getAllCrsProfileIds(): List<Int>

    @Query("SELECT * FROM crs_profiles WHERE id = :id")
    suspend fun getById(id: Int): CrsProfileEntity?

    @Query("DELETE FROM crs_profiles")
    suspend fun deleteAll()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRespondentEntity(profiles: List<RespondentEntity>)

    @Query("DELETE FROM crs_respondents where isSynced=0")
    suspend fun deleteAllAlreadySynced()


    @Query("select country_id from crs_community_region_mapping")
    suspend fun getAllCountryIdsFromMapping(): List<Int>

    @Query("select * from countries where id in (select country_id from crs_community_region_mapping)")
    suspend fun getAllCountryNamesFromMapping(): List<CountryEntity>


    @Query("select * from districts where id in (select district_id from crs_community_region_mapping)")
    suspend fun getAllDistrictNamesFromMapping(): List<DistrictEntity>

    @Query("select * from places where id in (select place_id from crs_community_region_mapping)")
    suspend fun getAllPlaceNamesFromMapping(): List<PlaceEntity>

    @Query("select * from products where id in (select product_id from crs_community_region_mapping)")
    suspend fun getAllProductNamesFromMapping(): List<ProductEntity>

    @Query("select * from regions where id in (select id from crs_community_regions)")
    suspend fun getAllRegionNamesFromMapping(): List<RegionEntity>

    @Query("select *  from crs_community_regions")
    suspend fun getAllRegionNamesFromMapped(): List<CrsCommunityRegionEntity>

    @Query("select district_id from crs_community_region_mapping")
    suspend fun getAllDistrictIdsFromMapping(): List<Int>

    @Query("select place_id from crs_community_region_mapping")
    suspend fun getAllPlaceIdsFromMapping(): List<Int>

    @Query("select product_id from crs_community_region_mapping")
    suspend fun getAllProductIdsFromMapping(): List<Int>


    @Query("Select region_id from crs_community_region_mapping")
    suspend fun getAllRegionIdsFromMapping(): List<Int>

    @Query("select * from regions where id in (:listOfRegionIdMapped)")
    fun getAllRegionWhichAreMaped(listOfRegionIdMapped: List<Int>): List<RegionEntity>

    @Query("update crs_respondents set respondentCode = :respondentcode where id = :respondentId")
    fun updateUserRespondentId(respondentcode: String, respondentId: Int)


    @Query("select respondentCode from crs_respondents where id = :respondentId")
    fun getRespondentCodeById(respondentId: Int): String?


    @Query("UPDATE farmers_crs SET submitted_modules = :submittedModules WHERE id = :farmerId")
    suspend fun updateFarmerSubmittedModules(farmerId: String, submittedModules: String)


    /*Conditional Data*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecurrenceTypes(list: List<RecurrenceTypeEntity>)

    @Query("DELETE FROM crs_recurrence_type")
    suspend fun deleteAllRecurrenceTypes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSurveyDurationTypes(list: List<SurveyDurationTypeEntity>)

    @Query("DELETE FROM crs_survey_duration_type")
    suspend fun deleteAllSurveyDurationTypes()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllConditionalTypes(list: List<ConditionalTypeEntity>)

    @Query("DELETE FROM conditional_type")
    suspend fun deleteAllConditionalTypes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllConditionalQuestionDetails(list: List<ConditionalQuestionDetailEntity>)

    @Query("DELETE FROM conditional_question_details")
    suspend fun deleteAllConditionalQuestionDetails()

}

