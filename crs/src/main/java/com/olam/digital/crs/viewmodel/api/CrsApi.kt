package com.olam.digital.crs.viewmodel.api

import android.content.Context
import androidx.room.withTransaction
import com.google.gson.Gson
import com.olam.digital.crs.model.surveysubmit.Submission
import com.olam.digital.crs.room.CRSDao
import com.olam.digital.crs.room.CRSDatabase
import com.olam.digital.crs.room.CRSMandatoryQuestionEntity
import com.olam.digital.crs.room.CRSModuleEntity
import com.olam.digital.crs.room.CRSQuestionEntity
import com.olam.digital.crs.room.CRSResponseTypeDTO
import com.olam.digital.crs.room.ConditionalQuestionDetailEntity
import com.olam.digital.crs.room.ConditionalTypeEntity
import com.olam.digital.crs.room.CountryEntity
import com.olam.digital.crs.room.CrsCommunityProfileMappingEntity
import com.olam.digital.crs.room.CrsCommunityRegionEntity
import com.olam.digital.crs.room.CrsCommunityRegionMappingEntity
import com.olam.digital.crs.room.CrsConsent
import com.olam.digital.crs.room.CrsOptionEntity
import com.olam.digital.crs.room.CrsProfileEntity
import com.olam.digital.crs.room.CrsQuestionModuleXref
import com.olam.digital.crs.room.CrsRepeaterMappingEntity
import com.olam.digital.crs.room.DistrictEntity
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.room.FarmerGroupEntity
import com.olam.digital.crs.room.ModuleAssignmentEntity
import com.olam.digital.crs.room.PlaceEntity
import com.olam.digital.crs.room.ProductEntity
import com.olam.digital.crs.room.RecurrenceTypeEntity
import com.olam.digital.crs.room.RegionEntity
import com.olam.digital.crs.room.RespondentEntity
import com.olam.digital.crs.room.SectionEntity
import com.olam.digital.crs.room.SubDistrictEntity
import com.olam.digital.crs.room.SubmissionContext
import com.olam.digital.crs.room.SurveyDurationTypeEntity
import com.olam.digital.crs.viewmodel.api.CrsApi.CrsApiAsync.updateSurveyRepo
import com.olam.digital.crs.viewmodel.repo.CrsHomeRepo
import com.olam.digital.crs.viewmodel.repo.SubmissionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

/**
 * Java-friendly API surface for CRS data access.
 *
 * - CrsApi: blocking wrappers (simple for Java usage)
 * - CrsApiAsync: async 'fire-and-forget' operations on Dispatchers.IO
 *
 * NOTE:
 * If your caller already has DI (Hilt), inject FarmerRepository instead of using this facade.
 */
object CrsApi {
    var IMAGE_URL: String = ""

    @JvmStatic
    fun setImageUrl(imageUrl: String) {
        IMAGE_URL = imageUrl
    }

    @JvmStatic
    val moduleEnitity: CRSModuleEntity = CRSModuleEntity()


    /** Insert (upsert) a single farmer — blocking. */
    @JvmStatic
    fun insertFarmer(context: Context, farmer: FarmerEntity) {
        val repo = repository(context)
        runBlocking {
            repo.insertFarmer(farmer)
        }
    }

    /** Insert (upsert) multiple farmers — blocking. */
    @JvmStatic
    fun insertFarmers(context: Context, list: List<FarmerEntity>) {
        val repo = repository(context)
        runBlocking {
            repo.deleteFarmers()
            repo.insertFarmers(list)
        }
    }

    @JvmStatic
    fun getPhotoCheckBoxCount(context: Context): Long {
        val repo = repository(context)
        return runBlocking {
            repo.getAllPhotoCheckBoxImage(context).size.toLong()
        }
    }


    @JvmStatic
    fun getPhotoCheckConsentBoxCount(context: Context): Long {
        val repo = repository(context)
        return runBlocking {
            repo.getAllConsentFiles(context).size.toLong()
        }
    }

    @JvmStatic
    fun getPhotoCheckBoxImage(context: Context): List<CrsOptionEntity> {
        val repo = repository(context)
        return runBlocking {
            repo.getAllPhotoCheckBoxImage(context)
        }
    }

    /** Update a farmer — blocking. */
    @JvmStatic
    fun updateFarmer(context: Context, farmer: FarmerEntity) {
        val repo = repository(context)
        runBlocking {
            repo.updateFarmer(farmer)
        }
    }

    /** Delete a farmer — blocking. */
    @JvmStatic
    fun deleteFarmer(context: Context, farmer: FarmerEntity) {
        val repo = repository(context)
        runBlocking {
            repo.deleteFarmer(farmer)
        }
    }

    /** Get all farmers — blocking. */
    @JvmStatic
    fun getAllFarmers(context: Context): List<FarmerEntity> {
        val repo = repository(context)
        return runBlocking {
            repo.getAllFarmers()
        }
    }

    /** Get a farmer by ID — blocking. */
    @JvmStatic
    fun getFarmerById(context: Context, id: String): FarmerEntity? {
        val repo = repository(context)
        return runBlocking {
            repo.getFarmerById(id)
        }
    }

    /** Search farmers by (partial) name — blocking. */
    @JvmStatic
    fun searchFarmers(context: Context, query: String): List<FarmerEntity> {
        val repo = repository(context)
        return runBlocking {
            repo.searchFarmers(query)
        }
    }

    /** Count — blocking. */
    @JvmStatic
    fun getFarmerCount(context: Context): Int {
        val repo = repository(context)
        return runBlocking {
            repo.getFarmerCount()
        }
    }

    /** Clear table — blocking. */
    @JvmStatic
    fun clearFarmers(context: Context) {
        val repo = repository(context)
        runBlocking {
            repo.clearFarmers()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CrsApiEntryPoint {
        fun homeRepo(): CrsHomeRepo
    }

    // --- Internal helpers ---

    /** Build the repository on-demand from Room (no DI dependency). */
    /* private fun repository(context: Context): CrsHomeRepo {
         val db = CRSDatabase.getInstance(context)  // requires the getInstance() we added earlier
         return CrsHomeRepo(db.crsDao())
     }*/
    private fun repository(context: Context): CrsHomeRepo {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            CrsApiEntryPoint::class.java
        )
        return entryPoint.homeRepo()
    }


    // ... existing methods ...

    /**
     * Sync a farmer's module submission into the farmers_crs table.
     * Cleans old answers and updates the submitted_modules JSON field.
     */
    @JvmStatic
    fun syncFarmerModule(
        context: Context,
        farmerId: String,
        moduleId: Int,
        submissionDate: String,
        farmId: String?,
        plotId: String?,
        stakeholderId: String? = null
    ) {
        val db = CRSDatabase.getInstance(context.applicationContext)
        val dao = db.crsDao()

        runBlocking {
            db.withTransaction {
                try {
                    // Build new module JSON entry
                    val newModuleEntry = JSONObject().apply {
                        put("id", moduleId)
                        put("created_at", submissionDate)
                        put("farmId", farmId ?: "null")
                        put(
                            "plotId",
                            if (plotId.isNullOrBlank() || plotId == "0") "null" else plotId
                        )
                    }
                    val newModule = JSONObject().apply {
                        put("module", newModuleEntry)
                    }

                    if (!stakeholderId.isNullOrBlank()) {
                        // Update stakeholder record
                        //val multiStakeHolder = dao.getStakeHolderById(stakeholderId.toLong())
                        // val submittedModulesJSON = multiStakeHolder?.submitted_modules ?: "null"
                        //val updatedModules = updateModules(submittedModulesJSON, newModule)
                        // dao.updateSubmittedModulesById(stakeholderId, updatedModules)
                    } else {
                        // Update farmer record
                        val farmer = dao.getFarmerById(farmerId)
                        if (farmer != null) {
                            val submittedModulesJSON = farmer.submittedModules ?: "null"
                            val updatedModules = updateModules(submittedModulesJSON, newModule)
                            dao.updateFarmerSubmittedModules(farmerId, updatedModules)
                        }
                    }
                } catch (ex: Exception) {
                    Timber.e(ex, "CrsApi: Error syncing farmer module")
                }
            }
        }
    }

    /**
     * Merge new module JSON into existing submittedModules JSON string.
     */
    private fun updateModules(existingModules: String?, newModule: JSONObject): String {
        return try {
            val modulesArray: JSONArray = when {
                existingModules.isNullOrBlank() || existingModules == "null" -> {
                    JSONArray()
                }

                existingModules.trim().startsWith("{") -> {
                    // Root is an object with "modules"
                    val jsonObject = JSONObject(existingModules)
                    if (jsonObject.has("modules") && !jsonObject.isNull("modules")) {
                        jsonObject.getJSONArray("modules")
                    } else {
                        JSONArray()
                    }
                }

                existingModules.trim().startsWith("[") -> {
                    // Root is already an array
                    JSONArray(existingModules)
                }

                else -> JSONArray()
            }

            // Add the new module
            modulesArray.put(newModule)

            // Always wrap back into {"modules":[...]}
            JSONObject().put("modules", modulesArray).toString()
        } catch (e: Exception) {
            Timber.e(e, "CrsApi: Failed to update modules JSON")
            """{"modules":[]}"""
        }
    }


    @JvmStatic
    fun insertAllCrsCommunityProfilesBlocking(
        context: Context,
        rootArray: JSONArray
    ) = runBlocking {
        syncCrsCommunityProfiles(context, rootArray)
    }


    @JvmStatic
    suspend fun syncCrsCommunityProfiles(context: Context, jsonArray: JSONArray?) {
        withContext(Dispatchers.IO) {
            if (jsonArray == null || jsonArray.length() == 0) {
                Timber.w("syncRespondents: Empty or null response")
                return@withContext
            }
            val db = CRSDatabase.getInstance(context.applicationContext)
            val respondentDao = db.crsDao()

            // Clear older data
            respondentDao.deleteAllAlreadySynced()

            val batch = mutableListOf<RespondentEntity>()

            try {
                for (i in 0 until jsonArray.length()) {

                    val obj = jsonArray.optJSONObject(i) ?: continue

                    val id = obj.optInt("id", 0)
                    if (id == 0) continue  // Skip invalid row

                    val entity = RespondentEntity(
                        id = id,
                        name = obj.optString("name", null),
                        countryId = obj.optInt("countryId", 0),
                        regionId = obj.optInt("regionId", 0),
                        profileId = obj.optInt("profileId", 0),
                        productId = obj.optInt("productId", 0),
                        districtId = obj.optInt("districtId", 0),
                        placeId = obj.optInt("placeId", 0),
                        emailId = obj.optString("emailId", null),
                        countryCode = obj.optString("countryCode", null),
                        phoneNumber = obj.optString("phoneNumber", null),
                        respondentCode = obj.optString("respondentCode", null),
                        photo = obj.optString("photo", null),
                        gender = obj.optString("gender", null),
                        dateOfBirth = obj.optString("dateOfBirth", null),
                        isSynced = 0
                    )

                    batch.add(entity)
                }

                respondentDao.insertAllRespondents(batch)

                Timber.d("syncRespondents: Inserted %d respondents", batch.size)

            } catch (ex: Exception) {
                Timber.e(ex, "syncRespondents: Failed to parse or insert respondents")
            }
        }
    }


    @JvmStatic
    fun insertAllCrsProfilesBlocking(
        context: Context,
        rootArray: JSONArray
    ) = runBlocking {
        syncCrsProfiles(context, rootArray)
    }


    @JvmStatic
    suspend fun syncCrsProfiles(context: Context, jsonArray: JSONArray?) {
        withContext(Dispatchers.IO) {

            if (jsonArray == null || jsonArray.length() == 0) {
                Timber.w("syncCrsProfiles: Empty or null response")
                return@withContext
            }

            val db = CRSDatabase.getInstance(context.applicationContext)
            val profileDao = db.crsDao()

            // Clear previous data
            profileDao.deleteAll()

            val batch = mutableListOf<CrsProfileEntity>()
            //val response: JSONObject = jsonArray.getJSONObject("profiles")
            Timber.d("Json object is $jsonArray")
            try {
                for (i in 0 until jsonArray.length()) {
                    val o = jsonArray.optJSONObject(i) ?: continue
                    val id = o.optInt("id", 0)
                    val name = o.optString("profileName", null)
                    val active = o.optBoolean("isActive", true)

                    if (id != 0 && !name.isNullOrBlank()) {
                        batch.add(
                            CrsProfileEntity(
                                id = id,
                                profileName = name,
                                isActive = active
                            )
                        )
                    }
                }

                profileDao.insertAllCrsProfileEntity(batch)
                Timber.d("syncCrsProfiles: Inserted %d profiles", batch.size)

            } catch (ex: Exception) {
                Timber.e(ex, "syncCrsProfiles: Failed to parse or insert profiles")
            }
        }
    }


    @JvmStatic
    fun insertAllQuestionsAndModulesBlocking(
        context: Context,
        rootArray: Array<JSONObject?>,
        accessToken: String
    ) = runBlocking {
        insertAllQuestionsAndModules(context, rootArray, accessToken)
    }

    /**
     *
     *THis Method to insert Countries and crs Details in the local database which is used in the survey submission and also to show the details in the survey detail screen
     */

    @JvmStatic
    fun insertCountriesProductDetailsRunBlocking(
        context: Context,
        rootArray: Array<JSONObject?>
    ) = runBlocking {
        insertCountriesProductDetails(context, rootArray)
    }


    @JvmStatic
    suspend fun insertCountriesProductDetails(
        context: Context,
        rootArray: Array<JSONObject?>
    ) = withContext(Dispatchers.IO) {

        Timber.d(
            "CrsApi: insertCountriesProductDetails called with %d root elements",
            rootArray.size
        )

        if (rootArray.isEmpty()) return@withContext
        val db = CRSDatabase.getInstance(context.applicationContext)

        // CLEAR ALL TABLES
        db.crsDao().deleteAllCountries()
        db.crsDao().deleteAllRegions()
        db.crsDao().deleteAllSections()
        db.crsDao().deleteAllFarmerGroups()
        db.crsDao().deleteAllSubDistricts()
        db.crsDao().deleteAllDistricts()
        db.crsDao().deleteAllProducts()
        db.crsDao().deleteAllCrsCommunityMapping()
        db.crsDao().deleteAllCrsCommunityRegion()

        // BATCH LISTS
        val countriesBatch = mutableListOf<CountryEntity>()
        val regionsBatch = mutableListOf<RegionEntity>()
        val districtsBatch = mutableListOf<DistrictEntity>()
        val subDistrictsBatch = mutableListOf<SubDistrictEntity>()
        val placesBatch = mutableListOf<PlaceEntity>()
        val productsBatch = mutableListOf<ProductEntity>()
        val farmerGroupsBatch = mutableListOf<FarmerGroupEntity>()
        val sectionsBatch = mutableListOf<SectionEntity>()
        val communityRegionMappingBatch = mutableListOf<CrsCommunityRegionMappingEntity>()
        val communityRegions = mutableListOf<CrsCommunityRegionEntity>()
        val listOfCommunityProfiles = mutableListOf<CrsCommunityProfileMappingEntity>()

        try {
            rootArray.forEachIndexed { index, root ->

                if (root == null) {
                    Timber.w("CrsApi: rootArray[%d] is NULL; skipping", index)
                    return@forEachIndexed
                }

                val output = root.optJSONObject("outputdata") ?: return@forEachIndexed

                // ----------------------------
                // COUNTRIES
                // ----------------------------
                output.optJSONArray("countries")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue

                        countriesBatch.add(
                            CountryEntity(
                                id = o.optInt("id"),
                                name = o.optString("name"),
                                code = o.optString("code", null),
                                dialling_code = o.optString("dialling_code", null),
                                olam_country_id = o.optInt("olam_country_id").toString(),
                                enumerator_country = o.optInt("enumerator_country").toString(),
                                isSubdistrictCapture = o.optBoolean("isSubdistrictCapture"),
                                countryIsoCode = o.optString("countryIsoCode")
                            )
                        )
                    }
                }

                // ----------------------------
                // CommunityProfiles
                // ----------------------------
                output.optJSONArray("communityProfiles")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue

                        listOfCommunityProfiles.add(
                            CrsCommunityProfileMappingEntity(
                                uniqueId = 0,
                                countryId = o.optInt("country_id", 0),
                                communityProfileId = o.optInt("profile_id")
                            )
                        )
                    }
                }


                // ----------------------------
                // REGIONS
                // ----------------------------
                output.optJSONArray("regions")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        regionsBatch.add(
                            RegionEntity(
                                id = o.optInt("id"),
                                name = o.optString("name"),
                                countryId = o.optInt("country_id")
                            )
                        )
                    }
                }

                // ----------------------------
                // DISTRICTS
                // ----------------------------
                output.optJSONArray("districts")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        districtsBatch.add(
                            DistrictEntity(
                                id = o.optInt("id"),
                                name = o.optString("name"),
                                regionId = o.optInt("region_id")
                            )
                        )
                    }
                }

                // ----------------------------
                // SUBDISTRICTS
                // ----------------------------
                output.optJSONArray("subdistricts")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        subDistrictsBatch.add(
                            SubDistrictEntity(
                                id = o.optInt("id"),
                                name = o.optString("name"),
                                districtId = o.optInt("district_id")
                            )
                        )
                    }
                }

                // ----------------------------
                // PLACES
                // ----------------------------
                output.optJSONArray("places")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        placesBatch.add(
                            PlaceEntity(
                                id = o.optInt("id"),
                                name = o.optString("name"),
                                district_id = o.optInt("district_id"),
                                subdistrictId = o.optInt("subdistrictId"),
                                type = o.optString("type")
                            )
                        )
                    }
                }

                // ----------------------------
                // PRODUCTS
                // ----------------------------
                output.optJSONArray("products")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        productsBatch.add(
                            ProductEntity(
                                id = o.optInt("id"),
                                name = o.optString("name")
                            )
                        )
                    }
                }

                // ----------------------------
                // FARMER GROUPS
                // ----------------------------
                output.optJSONArray("farmergroups")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue

                        farmerGroupsBatch.add(
                            FarmerGroupEntity(
                                id = o.optInt("id"),
                                programmeId = o.optInt("programme_id"),
                                partnerId = o.optInt("partner_id"),
                                productId = o.optInt("product_id"),
                                code = o.optString("code"),
                                name = o.optString("name")
                            )
                        )
                    }
                }

                // ----------------------------
                // SECTIONS
                // ----------------------------
                output.optJSONArray("sections")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue

                        sectionsBatch.add(
                            SectionEntity(
                                id = o.optInt("id"),
                                code = o.optString("code"),
                                name = o.optString("name"),
                                farmerGroupId = o.optInt("farmergroup_id")
                            )
                        )
                    }
                }

                // ----------------------------
                // COMMUNITY REGION MAPPING
                // ----------------------------
                output.optJSONArray("communityRegions")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        communityRegions.add(
                            CrsCommunityRegionEntity(
                                id = o.optInt("id"),
                                name = o.optString("name", null),
                                countryId = o.optInt("country_id").toString()
                            )
                        )
                    }

                    if (communityRegions.isNotEmpty()) {
                        communityRegions.forEach { region ->
                            //first map all the countries
                            val countryList =
                                countriesBatch.find { it.id.toString() == region.countryId }
                            //map all the regions
                            val regionList =
                                regionsBatch.filter { it.id.toString() == region.id.toString() }
                            //map all the districts
                            val districtList =
                                districtsBatch.filter { it.regionId.toString() == regionList.firstOrNull()?.id.toString() }
                            //map all subdistricts
                            val subDistrictList =
                                subDistrictsBatch.filter { it.districtId.toString() == districtList.firstOrNull()?.id.toString() }
                            //map all places
                            val placeList =
                                placesBatch.filter { it.district_id.toString() == districtList.firstOrNull()?.id.toString() || it.subdistrictId.toString() == subDistrictList.firstOrNull()?.id.toString() }
                            //map farmer groups
                            val farmerGroupsList =
                                farmerGroupsBatch.filter { it.productId.toString() == productsBatch.firstOrNull()?.id.toString() }
                            //map productsList
                            val productList =
                                productsBatch.filter { it.id.toString() == farmerGroupsList.firstOrNull()?.productId.toString() }
                            //map sections
                            val sectionList =
                                sectionsBatch.filter { it.farmerGroupId.toString() == farmerGroupsList.firstOrNull()?.id.toString() }

                            //now  iterate to all filtered combine all and insert into crscommunity mapping
                            regionList.forEach { regionEntity ->
                                districtList.forEach { districtEntity ->
                                    subDistrictList.forEach { subDistrictEntity ->
                                        placeList.forEach { placeEntity ->
                                            farmerGroupsList.forEach { farmerGroupEntity ->
                                                productList.forEach { productEntity ->
                                                    sectionList.forEach { sectionEntity ->
                                                        communityRegionMappingBatch.add(
                                                            CrsCommunityRegionMappingEntity(
                                                                mappingId = 0,//auto generate from room
                                                                crsCommunityRegionId = region.id,
                                                                countryId = countryList?.id ?: 0,
                                                                regionId = regionEntity.id,
                                                                districtId = districtEntity.id,
                                                                subdistrictId = subDistrictEntity.id,
                                                                placeId = placeEntity.id,
                                                                farmerGroupId = farmerGroupEntity.id,
                                                                productId = productEntity.id,
                                                                sectionId = sectionEntity.id
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    Timber.d(
                        "CrsApi: Parsed %d community regions and %d mappings",
                        communityRegions.size,
                        communityRegionMappingBatch.size
                    )
                    // INSERT ALL TABLES
                    db.withTransaction {
                        db.crsDao().insertAllCountry(countriesBatch)
                        db.crsDao().insertAllRegion(regionsBatch)
                        db.crsDao().insertAllDistrict(districtsBatch)
                        db.crsDao().insertAllSubDistrict(subDistrictsBatch)
                        db.crsDao().insertCommunityRegion(communityRegions)

                        // insert Place ONLY after all parent tables are inserted
                        db.crsDao().insertAllPlace(placesBatch)
                        db.crsDao().insertAllCommunityProfileMapping(listOfCommunityProfiles)

                        db.crsDao().insertAllProductEntity(productsBatch)
                        db.crsDao().insertAllFarmerGroupEntity(farmerGroupsBatch)
                        db.crsDao().insertAllSectionEntity(sectionsBatch)
                        db.crsDao().insertAllCrsCommunityRegionMapping(communityRegionMappingBatch)
                    }


                    Timber.d("CrsApi: All records inserted successfully.")
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex, "CrsApi: insertCountriesProductDetails crashed")
        }
    }


    /**
     * App module passes an array of JSONObjects (from network).
     * Each element can be:
     *  A) a full payload with "outputdata": { "modules": [...], "crsQuestion": [...] }
     *  B) a single module object { id, name, ... }
     *  C) a single crsQuestion object { questionId, responseType, questionText, moduleId?, options? }
     *
     * This method parses and upserts both Modules and CRS Questions (with links & options)
     * into the CRS Room DB transactionally. Must be called OFF the main thread.
     */

    @JvmStatic
    suspend fun insertAllQuestionsAndModules(
        context: Context,
        rootArray: Array<JSONObject?>,
        accessToken: String
    ) {
        withContext(Dispatchers.IO) {
            Timber.d(
                "CrsApi: insertAllQuestionsAndModules called with %d root elements",
                rootArray.size
            )

            val db = CRSDatabase.getInstance(context.applicationContext)
            if (rootArray.isEmpty()) return@withContext

            //delete all exisiting question
            db.crsDao().deleteAllQuestions()
            db.crsDao().deleteAllModules()
            db.crsDao().questionResponseType()
            db.crsDao().deleteConsentData()
            db.crsDao().deleteOptionBatch()
            db.crsDao().deleteModuleAssignments()
            db.crsDao().deleteFarmerModuleAssignments()
            db.crsDao().deleteMandatoryQuestions()
            db.crsDao().deleteRepeaterCrsQuestions()
            db.crsDao().deleteAllConditionalTypes()


            val modulesBatch = mutableListOf<CRSModuleEntity>()
            val questionsBatch = mutableListOf<CRSQuestionEntity>()
            val repeaterBatch = mutableListOf<CrsRepeaterMappingEntity>()
            val questionResponseType = mutableListOf<CRSResponseTypeDTO>()
            val qModuleLinksBatch = mutableListOf<CrsQuestionModuleXref>()
            val consentData = mutableListOf<CrsConsent>()
            val optionsBatch = mutableListOf<CrsOptionEntity>()
            val moduleAssignments = mutableListOf<ModuleAssignmentEntity>()
            val moduleConditionTypesCode = mutableListOf<ConditionalTypeEntity>()
            try {
                rootArray.forEachIndexed { index, root ->
                    if (root == null) {
                        Timber.w("CrsApi: rootArray[%d] is NULL; skipping", index)
                        return@forEachIndexed
                    }


                    val output = root.optJSONObject("outputdata")
                    if (output != null) {
                        output.optJSONArray("modules")
                            ?.let { addModulesFromArray(it, modulesBatch, repeaterBatch) }

                        output.optJSONArray("crsQuestion")?.let {
                            addQuestionsFromArray(
                                it,
                                questionsBatch,
                                qModuleLinksBatch,
                                optionsBatch,
                                moduleAssignments,
                                questionResponseType
                            )
                        }

                        Timber.d("Modules Codition types ${output.optJSONArray("conditionalType")}")

                        /* output.optJSONArray("conditionalType")?.let {
                             addModuleConditionTypes( it, moduleConditionTypesCode)
                         }*/

                        output.optJSONObject("questionnaireData")?.let {
                            addQuestionnaireDataFromArray(it, db.crsDao(), moduleConditionTypesCode)
                        }

                        output.optJSONArray("crsConsent")?.let {
                            addconsentDataFromArray(it, consentData)
                        }

                        // ✅ NEW: parse module assignments array at the top-level of outputdata
                        output.optJSONArray("moduleassignments")?.let {
                            addModuleAssignmentsFromArray(it, moduleAssignments)
                        }

                        // ✅ NEW: parse response types array at the top-level of outputdata
                        output.optJSONArray("responseType")?.let {
                            addResponseTypesFromArray(it, questionResponseType)
                        }

                        return@forEachIndexed
                    }

                    if (looksLikeQuestionType(root)) {
                        toResponseTypeEntity(root)?.let { questionResponseType.add(it) }
                        return@forEachIndexed
                    }

                    if (looksLikeModule(root)) {
                        toModuleEntity(root)?.let { modulesBatch.add(it) }
                        return@forEachIndexed
                    }

                    if (looksLikeQuestion(root)) {
                        toQuestionEntity(root)?.let { questionsBatch.add(it) }
                        toQuestionModuleLink(root)?.let { qModuleLinksBatch.add(it) }
                        addOptionsFromQuestion(root, optionsBatch)
                    }
                }

                if (
                    modulesBatch.isEmpty() &&
                    questionsBatch.isEmpty() &&
                    qModuleLinksBatch.isEmpty() &&
                    questionResponseType.isEmpty() &&
                    optionsBatch.isEmpty() && consentData.isEmpty()
                ) {
                    Timber.i("CrsApi: Nothing to insert; batches are empty")
                    return@withContext
                }

                if (moduleConditionTypesCode.isEmpty()) {
                    Timber.i("CrsApi: No conditional types found to insert; batch is empty")
                }

                val moduleIdResponse: JSONObject? = rootArray[2]
                val moduleIdsForNormalSurvey =
                    moduleIdResponse?.getJSONArray("modulesWithoutAutoSampling")

                Timber.d(
                    "CrsApi: committing %d modules, %d questions, %d links, %d options",
                    modulesBatch.size,
                    questionsBatch.size,
                    qModuleLinksBatch.size,
                    optionsBatch.size
                )

                // Force create DB file
                db.openHelper.writableDatabase

                // Use Room transaction + suspend DAO calls
                db.withTransaction {

                    Timber.d(
                        "Modules batch size: ${modulesBatch.size}  || ${
                            Gson().toJson(
                                questionsBatch.filter { it -> it.moduleId?.toInt() == 453 })
                        }"
                    )
                    if (modulesBatch.isNotEmpty()) {

                        db.crsDao().upsertModules(modulesBatch)
                        val mandatoryEntities = modulesBatch.flatMap { module ->
                            module.mandatoryQuestionIds?.map { qId ->
                                CRSMandatoryQuestionEntity(
                                    moduleId = module.id,
                                    questionId = qId.toLong()
                                )
                            } ?: emptyList()
                        }
                        if (mandatoryEntities.isNotEmpty()) {
                            db.crsDao().insertMandatoryQuestions(mandatoryEntities)
                        }
                    }
                    if (questionsBatch.isNotEmpty()) db.crsDao()
                        .upsertQuestions(questionsBatch)
                    if (qModuleLinksBatch.isNotEmpty()) db.crsDao()
                        .upsertCrsQuestionModuleXref(qModuleLinksBatch)
                    if (optionsBatch.isNotEmpty()) db.crsDao()
                        .upsertOptions(optionsBatch)
                    if (questionResponseType.isNotEmpty()) db.crsDao()
                        .upsertResponseTypes(questionResponseType)
                    if (moduleAssignments.isNotEmpty()) db.crsDao()
                        .upsertModuleAssignments(moduleAssignments)
                    if (consentData.isNotEmpty()) db.crsDao()
                        .upsertConsents(consentData)
                    if (repeaterBatch.isNotEmpty()) db.crsDao()
                        .insertAllCrsRepeaterMapping(repeaterBatch)
                    if (moduleConditionTypesCode.isNotEmpty()) db.crsDao()
                        .insertAllConditionalTypes(moduleConditionTypesCode)

                    moduleIdsForNormalSurvey?.let { jsonArray ->
                        for (i in 0 until jsonArray.length()) {
                            val moduleId = try {
                                jsonArray.getLong(i)   // safer than getString().toLong()
                            } catch (e: Exception) {
                                Timber.e(e, "Invalid module id at index $i")
                                continue
                            }
                            Timber.d("Modules updating survey: $moduleId")
                            updateSurveyRepo(moduleId, context)
                        }
                    }
                }

                val dbFile = context.applicationContext.getDatabasePath("crs.db")
                Timber.d(
                    "CrsApi: CRS DB path: %s exists=%s",
                    dbFile.absolutePath,
                    dbFile.exists()
                )
                /*CoroutineScope(Dispatchers.IO).launch {
                    repository(context).processAllPhotoCheckboxImages(
                        context,
                        accessToken
                    )
                }*/
                val questions453 = db.crsDao().getQuestionsByModuleId(453)
                Timber.d("Questions in DB for module 453: %s", Gson().toJson(questions453))

            } catch (t: Throwable) {
                Timber.e(t, "CrsApi: insertAllQuestionsAndModules failed")
            }
        }
    }


    @JvmStatic
    fun isImageFile(label: String?): Boolean {
        if (label.isNullOrEmpty()) return false
        return label.endsWith(".jpg", true)
                || label.endsWith(".jpeg", true)
                || label.endsWith(".png", true)
    }

    private fun addconsentDataFromArray(
        arr: JSONArray,
        out: MutableList<CrsConsent>
    ) {
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optLong("id", 0L)
            val consentText = o.optString("consentText", null)
            val consentType = o.optString("consentType", null)
            val moduleName = o.optString("moduleName", null)
            val consentPath = o.optString("consentPath", null)
            val moduleId =
                if (o.has("moduleId") && !o.isNull("moduleId")) o.optLong("moduleId") else null

            if (id == 0L) continue

            out.add(
                CrsConsent(
                    id = id,
                    consentText = consentText,
                    consentType = consentType,
                    moduleName = moduleName,
                    consentPath = consentPath,
                    moduleId = moduleId
                )
            )
        }
    }

    private fun addModuleAssignmentsFromArray(
        arr: JSONArray,
        out: MutableList<ModuleAssignmentEntity>
    ) {
        Timber.d(
            "CrsApi: addModuleAssignmentsFromArray called with %d elements",
            arr.length()
        )
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            toModuleAssignmentEntity(o)?.let { out.add(it) }
        }
    }

    private fun addResponseTypesFromArray(
        arr: JSONArray,
        out: MutableList<CRSResponseTypeDTO>
    ) {
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            toResponseTypeEntity(o)?.let { out.add(it) }
        }
    }


    // ----------------- Parsing helpers -----------------

    private fun addModulesFromArray(
        arr: JSONArray,
        out: MutableList<CRSModuleEntity>,
        repeaterBatch: MutableList<CrsRepeaterMappingEntity>
    ) {
        for (i in 0 until arr.length()) {
            val m = arr.optJSONObject(i) ?: continue
            val moduleEntity = toModuleEntity(m)
            if (moduleEntity != null) {
                out.add(moduleEntity)

                // Parse repeaterMappings for this module
                val repeaterArr = m.optJSONArray("repeaterMappings") ?: continue
                for (r in 0 until repeaterArr.length()) {
                    val repObj = repeaterArr.optJSONObject(r) ?: continue

                    val parentId = repObj.optLong("repeaterQuestionId", 0L)
                    val childIdsArr = repObj.optJSONArray("childQuestionIds") ?: JSONArray()
                    val repeatLabel = repObj.optInt("repeatCount", 0)
                    val displayOrder = repObj.optInt("displayOrder", 0)

                    for (c in 0 until childIdsArr.length()) {
                        val childId = childIdsArr.optLong(c, 0L)
                        if (parentId != 0L && childId != 0L) {
                            repeaterBatch.add(
                                CrsRepeaterMappingEntity(
                                    moduleId = moduleEntity.id,
                                    parentQuestionId = parentId,
                                    childQuestionId = childId,
                                    repeatLabel = repeatLabel,
                                    displayOrder = displayOrder
                                )
                            )
                        }
                    }
                }
            }
        }
    }


    private fun addQuestionsFromArray(
        arr: JSONArray,
        qOut: MutableList<CRSQuestionEntity>,
        linkOut: MutableList<CrsQuestionModuleXref>,
        optOut: MutableList<CrsOptionEntity>,
        moduleAssignments: MutableList<ModuleAssignmentEntity>,
        questionResponseType: MutableList<CRSResponseTypeDTO>
    ) {
        for (i in 0 until arr.length()) {
            val q = arr.optJSONObject(i) ?: continue
            toQuestionEntity(q, i)?.let { qOut.add(it) }
            toQuestionModuleLink(q)?.let { linkOut.add(it) }
            addOptionsFromQuestion(q, optOut)
        }
    }

    private fun saveBinaryImage(
        body: ResponseBody,
        filePath: String,
        context: Context
    ): String? {
        val file = File(context.filesDir, filePath)

        body.byteStream().use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    @JvmStatic
    fun getAllPhotoImages(context: Context, callback: CrsApiAsync.CallbackPhotoNormal) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = repository(context)
                val response = repo.getAllPhotoCheckBoxImage(context)
                callback.onSuccess(response)
            } catch (exception: Exception) {
                callback.onError(exception)
                Timber.e("Exception while getting photo checkbox images ${exception.message}")
            }
        }
    }


    @JvmStatic
    fun getAllConsentFiles(context: Context, callback: CrsApiAsync.CallbackConsentFiles) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = repository(context)
                val response = repo.getAllConsentFiles(context)
                callback.onSuccess(response)
            } catch (exception: Exception) {
                callback.onError(exception)
                Timber.e("Exception while getting consent files ${exception.message}")
            }
        }
    }


    @JvmStatic
    fun updateConsentFilePath(
        context: Context,
        localPath: String,
        consent: CrsConsent
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = repository(context)
                repo.updateUserCosentData(context, localPath, consent)
            } catch (exception: Exception) {
                Timber.e("Exception while updating user image ${exception.message}")
            }
        }
    }


    @JvmStatic
    fun updateUserImage(
        context: Context,
        localPath: String,
        optionId: Long,
        questionId: Long
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = repository(context)
                repo.updateUserImage(context, localPath, optionId, questionId)
            } catch (exception: Exception) {
                Timber.e("Exception while updating user image ${exception.message}")
            }
        }
    }

    @JvmStatic
    suspend fun saveImageToLocal(
        context: Context,
        body: ResponseBody,
        fileName: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                return@withContext saveImageToLocal(context, body, fileName)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    private fun addOptionsFromQuestion(
        q: JSONObject,
        optOut: MutableList<CrsOptionEntity>
    ) {
        val opts = q.optJSONArray("options") ?: return
        val questionId = q.optLong("questionId", 0L)
        if (questionId == 0L) return

        for (j in 0 until opts.length()) {
            val o = opts.optJSONObject(j) ?: continue
            val optionId = o.optLong("optionId", 0L)
            if (optionId == 0L) continue

            optOut.add(
                CrsOptionEntity(
                    optionId = optionId,
                    questionId = questionId,
                    label = o.optString("options", null),
                    isDeleted = if (o.has("isDeleted") && !o.isNull("isDeleted")) {
                        o.optBoolean("isDeleted")
                    } else null,
                    options_fr = o.optString("options_fr", null),
                    options_es = o.optString("options_es", null),
                    options_id = o.optString("options_id", null),
                    options_pt = o.optString("options_pt", null),
                    options_tr = o.optString("options_tr", null),
                    options_lo = o.optString("options_lo", null),
                    options_vi = o.optString("options_vi", null),
                    options_th = o.optString("options_th", null),
                    options_mr = o.optString("options_mr", null)
                )
            )
        }
    }


    private fun looksLikeModuleAssignment(o: JSONObject): Boolean {
        return o.has("id") && o.has("moduleId") && o.has("moduleassignment_id")
    }


    private fun looksLikeQuestionType(o: JSONObject): Boolean {
        return o.has("id") && o.has("responseType")
    }

    // ----------------- Shape checks -----------------

    private fun looksLikeModule(o: JSONObject): Boolean {
        return o.has("id") && o.has("name")
    }

    private fun looksLikeQuestion(o: JSONObject): Boolean {
        return o.has("questionId") && o.has("questionText")
    }


    private fun toModuleAssignmentEntity(o: JSONObject): ModuleAssignmentEntity? {
        val id = o.optLong("id", 0L)
        val moduleId = o.optLong("module_id", 0L)
        val moduleAssignmentId = o.optLong("moduleassignment_id", 0L)
        if (id == 0L || moduleId == 0L || moduleAssignmentId == 0L) return null

        return ModuleAssignmentEntity(
            id = id,
            moduleId = moduleId,
            moduleAssignmentId = moduleAssignmentId,
            moduleAssignmentType = o.optString("moduleassignment_type", null),
            startDateEpoch = o.optString("startDate", null),
            surveyDuration = if (!o.isNull("surveyDuration")) o.optInt("surveyDuration") else null,
            viewModule = if (!o.isNull("view_module")) o.optInt("view_module") else null,
            countryId = if (!o.isNull("country_id")) o.optLong("country_id") else null,
            recurrenceFrequency = if (!o.isNull("recurrence_frequency")) o.optInt("recurrence_frequency") else null,
            recurrenceType = o.optString("recurrence_type", null),
            disabledMonths = o.optString("disabled_months", null),
            active = if (!o.isNull("active")) o.optBoolean("active") else null,
            createdAt = o.optString("created_at", null),
            updatedAt = o.optString("updated_at", null),
            appId = o.optString("app_id", null),
            surveyCode = o.optString("surveyCode", null),
            isChildLabour = if (!o.isNull("isChildLabour")) o.optBoolean("isChildLabour") else null,
            fkFarmerRoleId = if (!o.isNull("fkFarmerRoleId")) o.optLong("fkFarmerRoleId") else null,
            isFarmSurvey = if (!o.isNull("isFarmSurvey")) o.optBoolean("isFarmSurvey") else null,
            isPlotSurvey = if (!o.isNull("isPlotSurvey")) o.optBoolean("isPlotSurvey") else null,
            isOfflineDownload = if (!o.isNull("isOfflineDownload")) o.optBoolean("isOfflineDownload") else null,
            rawJson = o.toString()
        )
    }


    private fun toResponseTypeEntity(o: JSONObject): CRSResponseTypeDTO? {
        val id = o.optInt("id", 0)
        val name = o.optString("responseType", null)
        if (id == 0 || name.isNullOrBlank()) return null

        return CRSResponseTypeDTO(
            id = id,
            name = name,
            isActive = if (o.has("isActive") && !o.isNull("isActive")) o.optBoolean("isActive") else false
        )
    }

    // Null-safe int
    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (has(key) && !isNull(key)) optInt(key) else null

    // Null-safe boolean with default=false
    private fun JSONObject.optBooleanOrFalse(key: String): Boolean =
        if (has(key) && !isNull(key)) optBoolean(key) else false

    // Null-safe string returning null if key missing or value blank
    private fun JSONObject.optStringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) optString(key)?.takeIf { it.isNotBlank() } else null

    // Convert JSONArray to List<Int>, skipping non-numeric entries
    private fun JSONArray.toIntList(): List<Int>? {
        val out = mutableListOf<Int>()
        for (i in 0 until length()) {
            val v = get(i)
            when (v) {
                is Number -> out.add(v.toInt())
                is String -> v.toIntOrNull()?.let(out::add)
            }
        }
        return out.takeIf { it.isNotEmpty() }
    }


    // ----------------- Mappers -----------------

    private fun toModuleEntity(m: JSONObject): CRSModuleEntity? {
        val id = m.optLong("id", 0L)
        val name = m.optString("name", null)
        if (id == 0L || name.isNullOrBlank()) return null

        val surveyType = m.optIntOrNull("surveyType")
        val channel = m.optIntOrNull("channel")
        val isConsentEnabled = m.optBooleanOrFalse("isConsentEnabled")
        val isCommunitySurvey = m.optBooleanOrFalse("isCommunitySurvey")
        val submitedCrsSurveyFarmerId = m.optStringOrNull("submitedCrsSurveyFarmerId")

        // communityIds can be an array or a CSV string (handle both just in case)
        val communityIds: List<Int>? = when {
            m.has("communityIds") && !m.isNull("communityIds") -> {
                val anyVal = m.get("communityIds")
                when (anyVal) {
                    is JSONArray -> anyVal.toIntList()
                    is String -> anyVal
                        .split(',')
                        .mapNotNull { it.trim().toIntOrNull() }
                        .takeIf { it.isNotEmpty() }

                    else -> null
                }
            }

            else -> null
        }

        val mandatoryQuestionIds: List<Int>? = when {
            m.has("mandatoryQuestionIds") && !m.isNull("mandatoryQuestionIds") -> {
                val anyVal = m.get("mandatoryQuestionIds")
                when (anyVal) {
                    is JSONArray -> anyVal.toIntList()?.map { it.toInt() }
                    is String -> anyVal
                        .split(',')
                        .mapNotNull { it.trim().toIntOrNull() }
                        .takeIf { it.isNotEmpty() }

                    else -> null
                }
            }

            else -> null
        }


        return CRSModuleEntity(
            id = id,
            name = name,
            surveyType = surveyType,
            channel = channel,
            isCommunitySurvey = isCommunitySurvey,
            submitedCrsSurveyFarmerId = submitedCrsSurveyFarmerId,
            communityIds = communityIds,
            mandatoryQuestionIds = mandatoryQuestionIds,
            rawJson = m.toString(),
            isConsentEnabled = isConsentEnabled
        )
    }


    private fun toQuestionEntity(q: JSONObject, index: Int? = null): CRSQuestionEntity? {
        Timber.d("CrsApi: Parsing question at index %d: %s", index, q.toString())
        val questionId = q.optLong("questionId", 0L)
        val questionText = q.optString("questionText", null)
        if (questionId == 0L || questionText.isNullOrBlank()) return null
        return CRSQuestionEntity(
            questionId = questionId,
            responseType = (if (q.has("responseType") && !q.isNull("responseType")) q.optInt(
                "responseType"
            )
                .toString().toLong() else null) as Long?,
            questionText = questionText,
            helpText = q.optString("helpText", null),
            questionCode = q.optString("questionCode", null),
            characterLimit = if (!q.isNull("characterLimit")) q.optLong("characterLimit") else null,
            decimalLimit = if (!q.isNull("decimalLimit")) q.optLong("decimalLimit") else null,
            moduleId = q.optInt("moduleId").toString().toLong(),
            moduleName = q.optString("moduleName", null),
            questionStatus = q.optString("questionStatus", null),
            questionText_fr = q.optString("questionText_fr", null),
            questionText_es = q.optString("questionText_es", null),
            questionText_id = q.optString("questionText_id", null),
            questionText_pt = q.optString("questionText_pt", null),
            questionText_tr = q.optString("questionText_tr", null),
            questionText_lo = q.optString("questionText_lo", null),
            questionText_vi = q.optString("questionText_vi", null),
            questionText_th = q.optString("questionText_th", null),
            questionText_mr = q.optString("questionText_mr", null),
            displayOrder = index,
            repeaterTimes = q.optInt("repeaterTimes", 0)
        )
    }

    private fun toQuestionModuleLink(q: JSONObject): CrsQuestionModuleXref? {
        // Some payloads provide moduleId/moduleName/moduleStatus/questionStatus per question
        Timber.d(
            "CrsApi: Parsing question-module link for questionId=%d",
            q.optLong("questionId", 0L)
        )
        if (!q.has("moduleId") || q.isNull("moduleId")) return null
        val questionId = q.optLong("questionId", 0L)
        val moduleId = q.optLong("moduleId", 0L)
        if (questionId == 0L || moduleId == 0L) return null

        return CrsQuestionModuleXref(
            questionId = questionId,
            moduleId = moduleId,
            moduleName = q.optString("moduleName", null),
            moduleStatus = q.optString("moduleStatus", null),
            questionStatus = q.optString("questionStatus", null)
        )
    }

    @JvmStatic
    fun updateRespondentUserId(response: JSONObject, context: Context, respondentId: Int) {
        runBlocking {
            updateUserRespondentId(response, context, respondentId)
        }
    }

    suspend fun updateUserRespondentId(response: JSONObject, context: Context, respondentId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val respondentCode = response.optString("respondentCode", null)
                if (respondentCode.isNullOrBlank()) {
                    Timber.w("CrsApi: No respondentCode in response; cannot update user")
                    return@withContext
                }
                val db = repository(context)
                db.updateUserRespondentId(respondentCode, respondentId)
                Timber.d("CrsApi: Updated user with respondentCode=%s", respondentCode)
            } catch (e: Exception) {
                Timber.e(e, "CrsApi: Failed to update user respondent id")
            }
        }
    }


    fun addModuleConditionTypes(jsonArray: JSONArray, out: MutableList<ConditionalTypeEntity>) {
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue

            val id = obj.optInt("id", 0)
            val value = obj.optString("value", null)
            val operator = obj.optString("operator", null)
            val active = obj.optBoolean("active", true)
            val questionType = obj.optString("questionType", null)
            val crsQuestionType = obj.optString("crsQuestionType", null)

            if (id != 0 && !value.isNullOrBlank()) {
                out.add(
                    ConditionalTypeEntity(
                        id = id.toLong(),
                        value = value,
                        operator = operator,
                        active = active,
                        questionType = questionType,
                        crsQuestionType = crsQuestionType
                    )
                )
            }
        }
    }


  suspend  fun addQuestionnaireDataFromArray(
        output: JSONObject,
        dao: CRSDao,
        moduleConditionTypesCode: MutableList<ConditionalTypeEntity>
    ) {
        try {
            output.optJSONArray("recurrenceType")?.let { arr ->
                val batch = mutableListOf<RecurrenceTypeEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    batch.add(
                        RecurrenceTypeEntity(
                            id = o.optLong("id"),
                            value = o.optString("value", null),
                            active = o.optBoolean("active", true)
                        )
                    )
                }
                dao.deleteAllRecurrenceTypes()
                dao.insertAllRecurrenceTypes(batch)
            }

            output.optJSONArray("surveyDurationType")?.let { arr ->
                val batch = mutableListOf<SurveyDurationTypeEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    batch.add(
                        SurveyDurationTypeEntity(
                            id = o.optLong("id"),
                            value = o.optString("value", null),
                            active = o.optBoolean("active", true)
                        )
                    )
                }
                dao.deleteAllSurveyDurationTypes()
                dao.insertAllSurveyDurationTypes(batch)
            }

            output.optJSONArray("conditionalType")?.let {
                addModuleConditionTypes(it,moduleConditionTypesCode)
            }





        }
        catch (exception: Exception) {
            Timber.e(exception, "CrsApi: Failed to parse questionnaire data")
        }
    }


    @JvmStatic
    fun insertAllConditionalQuestionDetailsBlocking(
        context: Context,
        rootArray: JSONArray
    ) = runBlocking {
        syncConditionalQuestionDetails(context, rootArray)
    }

    @JvmStatic
    suspend fun syncConditionalQuestionDetails(context: Context, jsonArray: JSONArray?) {
        withContext(Dispatchers.IO) {
            if (jsonArray == null || jsonArray.length() == 0) {
                Timber.w("syncConditionalQuestionDetails: Empty or null response")
                return@withContext
            }

            val db = CRSDatabase.getInstance(context.applicationContext)
            val dao = db.crsDao()

            dao.deleteAllConditionalQuestionDetails()

            val batch = mutableListOf<ConditionalQuestionDetailEntity>()

            try {
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i) ?: continue

                    val entity = ConditionalQuestionDetailEntity(
                        id = obj.optLong("id"),
                        questionId = obj.optLong("questionId"),
                        conditionalId = obj.optLong("conditionalId"),
                        conditionalText = obj.optString("conditionalText", null),
                        isAnswered = obj.optBoolean("isAnswered"),
                        moduleId = obj.optLong("moduleId"),
                        subModuleId = obj.optLong("subModuleId"),
                        isDeleted = if (obj.isNull("isDeleted")) null else obj.optBoolean("isDeleted"),
                        lookupValues = obj.optString("lookupValues", null),
                        skipQuestions = obj.optString("skipQuestions", null),
                        lookupIds = obj.optString("lookupIds", null),
                        questionsDraftDtoList = obj.optString("questionsDraftDtoList", null),
                        skipQuestionIds = obj.optString("skipQuestionIds", null),
                        lookUpValueIds = obj.optString("lookUpValueIds", null)
                    )

                    batch.add(entity)
                }

                dao.insertAllConditionalQuestionDetails(batch)
                Timber.d("syncConditionalQuestionDetails: Inserted %d rows", batch.size)

            } catch (ex: Exception) {
                Timber.e(ex, "syncConditionalQuestionDetails: Failed to parse or insert")
            }
        }
    }


    /**
     * Async facade: runs operations on Dispatchers.IO (no return values).
     * Useful if you want to call from Java without blocking the UI thread.
     */
    object CrsApiAsync {

        /** Insert multiple farmers — async fire-and-forget. */
        @JvmStatic
        fun insertFarmersAsync(context: Context, list: List<FarmerEntity>) {
            val repo = repository(context)
            CoroutineScope(Dispatchers.IO).launch {
                repo.insertFarmers(list)
            }
        }

        @JvmStatic
        fun getRespondentCode(context: Context, respondentId: Int): String {
            return runBlocking {
                getRespondentCodeNew(context, respondentId)
            }
        }

        @JvmStatic
        suspend fun getRespondentCodeNew(context: Context, respondentId: Int): String {
            return withContext(Dispatchers.IO)
            {
                try {
                    val repo = repository(context)
                    repo.getRespondentCode(respondentId)
                } catch (exception: Exception) {
                    ""
                    Timber.e("Exception while getting respondent code ${exception.message}")
                } as String
            }
        }


        /** Insert single farmer — async fire-and-forget. */
        @JvmStatic
        fun insertFarmerAsync(context: Context, farmer: FarmerEntity) {
            val repo = repository(context)
            CoroutineScope(Dispatchers.IO).launch {
                repo.insertFarmer(farmer)
            }
        }

        @JvmStatic
        fun getSelectedCrsProfileIds(context: Context): List<Int> {
            return runBlocking {
                getAllCrsProfileIds(context)
            }
        }

        @JvmStatic
        suspend fun getAllCrsProfileIds(context: Context): List<Int> {
            return withContext(Dispatchers.IO) {
                try {
                    val repo = repository(context)
                    repo.getAllCrsProfileIds()
                } catch (exception: Exception) {
                    Timber.e("Exception while getting selected crs profile ids ${exception.message}")
                    Collections.emptyList<Int>()
                }
            } as List<Int>
        }


        @JvmStatic
        fun getAllCrsCommunitySurvey(
            context: Context,
            rootArray: Array<JSONObject?>,
            callback: CallBackSCrsCommunitySurvey
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // BATCH LISTS
                    val countriesBatch = mutableListOf<CountryEntity>()
                    val regionsBatch = mutableListOf<RegionEntity>()
                    val districtsBatch = mutableListOf<DistrictEntity>()
                    val subDistrictsBatch = mutableListOf<SubDistrictEntity>()
                    val placesBatch = mutableListOf<PlaceEntity>()
                    val productsBatch = mutableListOf<ProductEntity>()
                    val farmerGroupsBatch = mutableListOf<FarmerGroupEntity>()
                    val sectionsBatch = mutableListOf<SectionEntity>()
                    val communityRegionMappingBatch =
                        mutableListOf<CrsCommunityRegionMappingEntity>()
                    val communityRegions = mutableListOf<CrsCommunityRegionEntity>()
                    rootArray.forEachIndexed { index, root ->

                        if (root == null) {
                            Timber.w("CrsApi: rootArray[%d] is NULL; skipping", index)
                            return@forEachIndexed
                        }

                        val output = root.optJSONObject("outputdata") ?: return@forEachIndexed

                        // ----------------------------
                        // COUNTRIES
                        // ----------------------------
                        output.optJSONArray("countries")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val o = arr.optJSONObject(i) ?: continue

                                countriesBatch.add(
                                    CountryEntity(
                                        id = o.optInt("id"),
                                        name = o.optString("name"),
                                        code = o.optString("code", null),
                                        dialling_code = o.optString("dialling_code", null),
                                        olam_country_id = o.optInt("olam_country_id").toString(),
                                        enumerator_country = o.optInt("enumerator_country")
                                            .toString(),
                                        isSubdistrictCapture = o.optBoolean("isSubdistrictCapture"),
                                        countryIsoCode = o.optString("countryIsoCode")
                                    )
                                )
                            }
                        }

                        // ----------------------------
                        // REGIONS
                        // ----------------------------
                        output.optJSONArray("regions")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val o = arr.optJSONObject(i) ?: continue
                                regionsBatch.add(
                                    RegionEntity(
                                        id = o.optInt("id"),
                                        name = o.optString("name"),
                                        countryId = o.optInt("country_id")
                                    )
                                )
                            }
                        }

                        // ----------------------------
                        // DISTRICTS
                        // ----------------------------
                        output.optJSONArray("districts")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val o = arr.optJSONObject(i) ?: continue
                                districtsBatch.add(
                                    DistrictEntity(
                                        id = o.optInt("id"),
                                        name = o.optString("name"),
                                        regionId = o.optInt("region_id")
                                    )
                                )
                            }
                        }

                        // ----------------------------
                        // SUBDISTRICTS
                        // ----------------------------
                        output.optJSONArray("subdistricts")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val o = arr.optJSONObject(i) ?: continue
                                subDistrictsBatch.add(
                                    SubDistrictEntity(
                                        id = o.optInt("id"),
                                        name = o.optString("name"),
                                        districtId = o.optInt("district_id")
                                    )
                                )
                            }
                        }

                        // ----------------------------
                        // PLACES
                        // ----------------------------
                        output.optJSONArray("places")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val o = arr.optJSONObject(i) ?: continue
                                placesBatch.add(
                                    PlaceEntity(
                                        id = o.optInt("id"),
                                        name = o.optString("name"),
                                        district_id = o.optInt("district_id"),
                                        subdistrictId = o.optInt("subdistrictId"),
                                        type = o.optString("type")
                                    )
                                )
                            }
                        }

                        // ----------------------------
                        // PRODUCTS
                        // ----------------------------
                        output.optJSONArray("products")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val o = arr.optJSONObject(i) ?: continue
                                productsBatch.add(
                                    ProductEntity(
                                        id = o.optInt("id"),
                                        name = o.optString("name")
                                    )
                                )
                            }
                        }

                        // ----------------------------
                        // FARMER GROUPS
                        // ----------------------------
                        output.optJSONArray("farmergroups")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val o = arr.optJSONObject(i) ?: continue

                                farmerGroupsBatch.add(
                                    FarmerGroupEntity(
                                        id = o.optInt("id"),
                                        programmeId = o.optInt("programme_id"),
                                        partnerId = o.optInt("partner_id"),
                                        productId = o.optInt("product_id"),
                                        code = o.optString("code"),
                                        name = o.optString("name")
                                    )
                                )
                            }
                        }

                        // ----------------------------
                        // SECTIONS
                        // ----------------------------
                        output.optJSONArray("sections")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val o = arr.optJSONObject(i) ?: continue

                                sectionsBatch.add(
                                    SectionEntity(
                                        id = o.optInt("id"),
                                        code = o.optString("code"),
                                        name = o.optString("name"),
                                        farmerGroupId = o.optInt("farmergroup_id")
                                    )
                                )
                            }
                        }

                        // ----------------------------
                        // COMMUNITY REGION MAPPING
                        // ----------------------------
                        output.optJSONArray("communityRegions")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                val o = arr.optJSONObject(i) ?: continue
                                communityRegions.add(
                                    CrsCommunityRegionEntity(
                                        id = o.optInt("id"),
                                        name = o.optString("name", null),
                                        countryId = o.optInt("country_id").toString()
                                    )
                                )
                            }


                            if (communityRegions.isNotEmpty()) {
                                communityRegions.forEach { region ->
                                    //first map all the countries
                                    val countryList =
                                        countriesBatch.find { it.id.toString() == region.countryId }
                                    //map all the regions
                                    val regionList =
                                        regionsBatch.filter { it.id.toString() == region.id.toString() }
                                    //map all the districts
                                    val districtList =
                                        districtsBatch.filter { it.regionId.toString() == regionList.firstOrNull()?.id.toString() }
                                    //map all subdistricts
                                    val subDistrictList =
                                        subDistrictsBatch.filter { it.districtId.toString() == districtList.firstOrNull()?.id.toString() }
                                    //map all places
                                    val placeList =
                                        placesBatch.filter { it.district_id.toString() == districtList.firstOrNull()?.id.toString() || it.subdistrictId.toString() == subDistrictList.firstOrNull()?.id.toString() }
                                    //map farmer groups
                                    val farmerGroupsList =
                                        farmerGroupsBatch.filter { it.productId.toString() == productsBatch.firstOrNull()?.id.toString() }
                                    //map productsList
                                    val productList =
                                        productsBatch.filter { it.id.toString() == farmerGroupsList.firstOrNull()?.productId.toString() }
                                    //map sections
                                    val sectionList =
                                        sectionsBatch.filter { it.farmerGroupId.toString() == farmerGroupsList.firstOrNull()?.id.toString() }

                                    //now  iterate to all filtered combine all and insert into crscommunity mapping
                                    regionList.forEach { regionEntity ->
                                        districtList.forEach { districtEntity ->
                                            subDistrictList.forEach { subDistrictEntity ->
                                                placeList.forEach { placeEntity ->
                                                    farmerGroupsList.forEach { farmerGroupEntity ->
                                                        productList.forEach { productEntity ->
                                                            sectionList.forEach { sectionEntity ->
                                                                communityRegionMappingBatch.add(
                                                                    CrsCommunityRegionMappingEntity(
                                                                        mappingId = 0,//auto generate from room
                                                                        crsCommunityRegionId = region.id,
                                                                        countryId = countryList?.id
                                                                            ?: 0,
                                                                        regionId = regionEntity.id,
                                                                        districtId = districtEntity.id,
                                                                        subdistrictId = subDistrictEntity.id,
                                                                        placeId = placeEntity.id,
                                                                        farmerGroupId = farmerGroupEntity.id,
                                                                        productId = productEntity.id,
                                                                        sectionId = sectionEntity.id
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                            Timber.d("CrsApi: All records inserted successfully.")
                        }
                    }
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        callback.onSuccess(communityRegionMappingBatch)
                    }

                } catch (t: Throwable) {
                    Timber.e("Exception while getting community survey ${t.message}")
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        callback.onError(t)
                    }
                }
            }
        }


        interface CallBackSCrsCommunitySurvey {
            fun onSuccess(result: List<CrsCommunityRegionMappingEntity>)
            fun onError(t: Throwable)
        }

        interface CallbackNormalRespondent {
            fun onSuccess(result: List<RespondentEntity>)
            fun onError(t: Throwable)
        }

        interface CallbackNormal {
            fun onSuccess(result: String)
            fun onError(t: Throwable)
        }

        interface CallbackPhotoNormal {
            fun onSuccess(result: List<CrsOptionEntity>)
            fun onError(t: Throwable)
        }

        interface CallbackConsentFiles {
            fun onSuccess(result: List<CrsConsent>)
            fun onError(t: Throwable)
        }

        interface Callback {
            fun onSuccess(result: List<Submission>)
            fun onError(t: Throwable)
        }


        @JvmStatic
        fun getAllPendingSyncSurveyCount(context: Context, callback: CallbackNormal) {
            val repo = repositorySubmissionRepo(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val listOfPendingSurvey = repo.getAllPendingSyncSurveyCount()
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        callback.onSuccess(listOfPendingSurvey.toString())
                    }
                } catch (t: Throwable) {
                    Timber.e("Exception while saving answers ${t.message}")
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        callback.onError(t)
                    }
                }
            }
        }

        @JvmStatic
        fun updateSurveyRepo(moduleId: Long, context: Context) {
            val repo = repositorySubmissionRepo(context = context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repo.updateSurveyAlreadySubmitted(moduleId)
                } catch (exception: Exception) {
                    Timber.e("Exception while updatin survey ${exception.message}")
                }
            }
        }


        @JvmStatic
        fun getAllPendingRespondent(context: Context, callback: CallbackNormalRespondent) {
            val repo = repositorySubmissionRepo(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val listOfPendingRespondent = repo.getAllPendingRespondentCount()
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        callback.onSuccess(listOfPendingRespondent)
                    }
                } catch (t: Throwable) {
                    Timber.e("Exception while saving answers ${t.message}")
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        callback.onError(t)
                    }
                }
            }
        }


        @JvmStatic
        fun getAllPendingSyncSurveyModulesAsync(
            context: Context,
            submissionContext: SubmissionContext,
            callback: Callback
        ) {
            val repo = repositorySubmissionRepo(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val listOfPendingSurvey =
                        repo.getPendingSubmissionInstanceIds(submissionContext)
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        callback.onSuccess(listOfPendingSurvey)
                    }
                } catch (t: Throwable) {
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        callback.onError(t)
                    }
                }
            }
        }


        @JvmStatic
        fun updateInstanceRespondentAsync(
            surveyInstanceid: Int,
            context: Context,
            callback: CallbackNormal
        ) {
            val repo = repositorySubmissionRepo(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Timber.d("Survey Instance Id is $surveyInstanceid")
                    val updateSurveyInstance = repo.respondentsUpdateCode(surveyInstanceid)
                    withContext(Dispatchers.IO)
                    {
                        callback.onSuccess(updateSurveyInstance.toString())
                    }
                } catch (exception: Exception) {
                    Timber.e("Exception while exception ${exception.message}", exception)
                }
            }
        }

        @JvmStatic
        fun updateInstanceSurveyAsync(
            surveyInstanceid: Long,
            context: Context,
            callback: CallbackNormal
        ) {
            val repo = repositorySubmissionRepo(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Timber.d("Survey Instance Id is $surveyInstanceid")
                    val updateSurveyInstance = repo.updateSurveyInstance(surveyInstanceid)
                    withContext(Dispatchers.IO)
                    {
                        callback.onSuccess(updateSurveyInstance.toString())
                    }
                } catch (exception: Exception) {
                    Timber.e("Exception while exception ${exception.message}", exception)
                }
            }
        }


        /** Clear all — async fire-and-forget. */
        @JvmStatic
        fun clearFarmersAsync(context: Context) {
            val repo = repository(context)
            CoroutineScope(Dispatchers.IO).launch {
                repo.clearFarmers()
            }
        }

        private fun repositorySubmissionRepo(context: Context): SubmissionRepository {
            val db =
                CRSDatabase.getInstance(context)  // requires the getInstance() we added earlier
            return SubmissionRepository(db.crsDao())
        }


        // Build the repository on-demand from Room
        private fun repository(context: Context): CrsHomeRepo {
            val appContext = context.applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(
                appContext,
                CrsApi.CrsApiEntryPoint::class.java
            )
            return entryPoint.homeRepo()
        }

        @EntryPoint
        @InstallIn(SingletonComponent::class)
        interface CrsApiEntryPoint {
            fun homeRepo(): CrsHomeRepo
        }


    }
}
