package com.olam.digital.crs.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
    @Query("SELECT * FROM crs_question WHERE moduleId = :moduleId AND (questionStatus IS NULL OR questionStatus = 'ACTIVE')")
    suspend fun getQuestionsByModule(moduleId: Long): List<CRSQuestionEntity>

    @Transaction
    @Query("SELECT * FROM crs_options WHERE questionId = :questionId AND isDeleted = 0")
    suspend fun getOptionsForQuestion(questionId: Long): List<CrsOptionEntity>

    // ResponseType lookups
    @Query("SELECT * FROM crs_question_type")
    suspend fun getAllResponseTypes(): List<CRSResponseTypeDTO>

    @Query("SELECT * FROM crs_question_type WHERE id = :id")
    suspend fun getResponseTypeById(id: Long): CRSResponseTypeDTO?

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
            m.rawJson AS moduleRawJson,

            ma.moduleAssignmentId AS assignmentId,
            ma.moduleAssignmentType AS assignmentType,
            ma.countryId,
            ma.startDateEpoch,
            ma.surveyDuration,
            ma.viewModule,
            ma.rawJson AS assignmentRawJson,
            

            fm.id AS farmerModuleId,
            fm.status_started AS statusStarted,
            fm.status_finished AS statusFinished,
            fm.current_submodule_id AS currentSubmoduleId,
            fm.recorded_lat AS recordedLat,
            fm.recorded_lng AS recordedLng,
(
            SELECT COUNT(*)
            FROM crs_question_modules qx
            WHERE qx.moduleId = m.id
            -- Optional filters, uncomment if you want them:
            -- AND (:onlyActive = 0 OR qx.questionStatus = 'Active')
            -- AND (:moduleStatus IS NULL OR qx.moduleStatus = :moduleStatus)
        ) AS questionCount,



c.id AS consentId,
        c.consentText,
        c.consentType,
        c.consentPath

            

        FROM module_assignment ma
        INNER JOIN crs_modules m ON m.id = ma.moduleId
        LEFT JOIN farmer_modules fm ON fm.module_id = m.id
            AND (fm.farmer_id = :farmerId OR (:olamFarmerId IS NOT NULL AND fm.olam_farmer_id = :olamFarmerId))


LEFT JOIN crs_consents c
        ON c.moduleId = m.id


        WHERE
            -- optional: exclude child labour / plot survey if you carry those flags in JSON:
            -- (ma.rawJson IS NULL OR json_extract(ma.rawJson, '$.childLabour') != 'true')
            -- AND (ma.rawJson IS NULL OR json_extract(ma.rawJson, '$.isPlotSurvey') != 'true')

            -- keep surveyType != 8 if still relevant
            (:excludeSurveyType8 = 0 OR m.surveyType != 8)

            -- country filter (optional)
            AND (:countryId IS NULL OR ma.countryId = :countryId)

            -- assignment scope matching (OR across types). If the farmer's scope is NULL, that branch won't match.
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

            -- auto-sample logic:
            -- If you keep a flag in module rawJson:
            -- AND (
            --   (json_extract(m.rawJson, '$.isAutoSurvey') = 'true' AND :includeAuto = 1 AND m.id IN (:autoSampleIds))
            --   OR (json_extract(m.rawJson, '$.isAutoSurvey') != 'true')
            -- )
            -- If you don't have that flag and you just want to "filter by list when provided", use:
            AND (
                :includeAuto = 0
                OR m.id IN (:autoSampleIds)
            )
        """
    )
    suspend fun getModulesAgainstFarmer(
        // farmer linkage
        farmerId: String,
        olamFarmerId: String?,

        // country & scopes (nullable→ignored)
        countryId: Long?,
        productId: Long?,
        regionId: Long?,
        districtId: Long?,
        placeId: Long?,
        farmerGroupId: Long?,
        sectionId: Long?,
        programmeId: Long?,
        partnerId: Long?,

        // auto-sample
        autoSampleIds: List<Long>,
        includeAuto: Int,           // 1 if autoSampleIds not empty, else 0

        // control flags
        excludeSurveyType8: Int     // 1 to exclude surveyType=8, else 0
    ): List<FarmerModuleView>




        @Insert(onConflict = OnConflictStrategy.ABORT)
        suspend fun insertCrsSurveySyncData(instance: CrsSurveyInstance): Long

        @Update
        suspend fun updateSurveyInstanceSyncData(instance: CrsSurveyInstance)

        @Query("SELECT * FROM crs_survey_instance WHERE id = :id")
        suspend fun getById(id: Long): CrsSurveyInstance?

        @Query("""
        SELECT * FROM crs_survey_instance
        WHERE farmerId = :farmerId AND moduleId = :moduleId
        ORDER BY startedAt DESC LIMIT 1
    """)
        suspend fun getLatestForFarmerModule(farmerId: String, moduleId: Long): CrsSurveyInstance?

        @Query("""
        SELECT * FROM crs_survey_instance
        WHERE syncStatus IN ('PENDING','READY','FAILED')
        ORDER BY startedAt ASC
        LIMIT :limit
    """)
        suspend fun getInstancesNeedingSync(limit: Int = 50): List<CrsSurveyInstance>

        @Query("""
        UPDATE crs_survey_instance
        SET syncStatus = :syncStatus,
            lastSyncAt = :lastSyncAt,
            lastSyncError = :error
        WHERE id = :id
    """)
        suspend fun updateSyncState(id: Long, syncStatus: String, lastSyncAt: Long?, error: String?)



        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun upsert(vararg answers: CrsAnswerEntity)

        @Update
        suspend fun update(vararg answers: CrsAnswerEntity)

        @Query("SELECT * FROM crs_answers WHERE instanceId = :instanceId")
        suspend fun getByInstance(instanceId: Long): List<CrsAnswerEntity>

        @Query("""
        SELECT * FROM crs_answers 
        WHERE instanceId = :instanceId AND isSynced = 1
    """)
        suspend fun getDirtyByInstance(instanceId: Long): List<CrsAnswerEntity>

        @Query("""
        UPDATE crs_answers SET isSynced = 0, updatedAt = :now 
        WHERE instanceId = :instanceId
    """)
        suspend fun markInstanceAnswersSynced(instanceId: Long, now: Long = System.currentTimeMillis())

        @Query("""
        DELETE FROM crs_answers 
        WHERE instanceId IN (
            SELECT id FROM crs_survey_instance 
            WHERE syncStatus = 'SYNCED' 
              AND completedAt IS NOT NULL 
              AND (strftime('%s','now')*1000 - completedAt) > :retentionMs
        )
    """)
        suspend fun purgeOldSynced(retentionMs: Long = 30L * 24 * 60 * 60 * 1000) // 30 days

}

