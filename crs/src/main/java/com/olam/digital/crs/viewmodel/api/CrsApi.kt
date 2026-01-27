package com.olam.digital.crs.viewmodel.api

import android.content.Context
import androidx.room.withTransaction
import com.olam.digital.crs.room.CRSDatabase
import com.olam.digital.crs.room.CRSModuleEntity
import com.olam.digital.crs.room.CRSQuestionEntity
import com.olam.digital.crs.room.CRSResponseTypeDTO
import com.olam.digital.crs.room.CrsConsent
import com.olam.digital.crs.room.CrsOptionEntity
import com.olam.digital.crs.room.CrsQuestionModuleXref
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.room.ModuleAssignmentEntity
import com.olam.digital.crs.viewmodel.repo.CrsHomeRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

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

    // --- Internal helpers ---

    /** Build the repository on-demand from Room (no DI dependency). */
    private fun repository(context: Context): CrsHomeRepo {
        val db = CRSDatabase.getInstance(context)  // requires the getInstance() we added earlier
        return CrsHomeRepo(db.crsDao())
    }



    @JvmStatic
    fun insertAllQuestionsAndModulesBlocking(
        context: Context,
        rootArray: Array<JSONObject?>
    ) = runBlocking {
        insertAllQuestionsAndModules(context, rootArray)
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
    suspend fun insertAllQuestionsAndModules(context: Context, rootArray: Array<JSONObject?>) {
        withContext(Dispatchers.IO) {
            Timber.d("CrsApi: insertAllQuestionsAndModules called with %d root elements", rootArray.size)

            val db = CRSDatabase.getInstance(context.applicationContext)
            if (rootArray.isEmpty()) return@withContext

            val modulesBatch = mutableListOf<CRSModuleEntity>()
            val questionsBatch = mutableListOf<CRSQuestionEntity>()

            val questionResponseType = mutableListOf<CRSResponseTypeDTO>()
            val qModuleLinksBatch = mutableListOf<CrsQuestionModuleXref>()
            val consentData  = mutableListOf<CrsConsent>()
            val optionsBatch = mutableListOf<CrsOptionEntity>()
            val moduleAssignments = mutableListOf<ModuleAssignmentEntity>()
            try {
                rootArray.forEachIndexed { index, root ->
                    if (root == null) {
                        Timber.w("CrsApi: rootArray[%d] is NULL; skipping", index)
                        return@forEachIndexed
                    }



                    val output = root.optJSONObject("outputdata")
                    if (output != null) {
                        output.optJSONArray("modules")?.let { addModulesFromArray(it, modulesBatch) }

                        output.optJSONArray("crsQuestion")?.let {
                            addQuestionsFromArray(
                                it, questionsBatch, qModuleLinksBatch, optionsBatch, moduleAssignments, questionResponseType
                            )
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

                    if (looksLikeQuestionType(root))
                    {
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

                Timber.d(
                    "CrsApi: committing %d modules, %d questions, %d links, %d options",
                    modulesBatch.size, questionsBatch.size, qModuleLinksBatch.size, optionsBatch.size
                )

                // Force create DB file
                db.openHelper.writableDatabase

                // Use Room transaction + suspend DAO calls
                db.withTransaction {
                    if (modulesBatch.isNotEmpty()) db.crsDao().upsertModules(modulesBatch)
                    if (questionsBatch.isNotEmpty()) db.crsDao().upsertQuestions(questionsBatch)
                    if (qModuleLinksBatch.isNotEmpty()) db.crsDao().upsertCrsQuestionModuleXref(qModuleLinksBatch)
                    if (optionsBatch.isNotEmpty()) db.crsDao().upsertOptions(optionsBatch)
                    if (questionResponseType.isNotEmpty()) db.crsDao().upsertResponseTypes(questionResponseType)
                    if (moduleAssignments.isNotEmpty()) db.crsDao().upsertModuleAssignments(moduleAssignments)
                    if (consentData.isNotEmpty()) db.crsDao().upsertConsents(consentData)
                }

                val dbFile = context.applicationContext.getDatabasePath("crs.db")
                Timber.d("CrsApi: CRS DB path: %s exists=%s", dbFile.absolutePath, dbFile.exists())
            } catch (t: Throwable) {
                Timber.e(t, "CrsApi: insertAllQuestionsAndModules failed")
            }
        }
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
            val moduleId = if (o.has("moduleId") && !o.isNull("moduleId")) o.optLong("moduleId") else null

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
        Timber.d("CrsApi: addModuleAssignmentsFromArray called with %d elements", arr.length())
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

    private fun addModulesFromArray(arr: JSONArray, out: MutableList<CRSModuleEntity>) {
        for (i in 0 until arr.length()) {
            val m = arr.optJSONObject(i) ?: continue
            toModuleEntity(m)?.let { out.add(it) }
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
            toQuestionEntity(q)?.let { qOut.add(it) }
            toQuestionModuleLink(q)?.let { linkOut.add(it) }
            addOptionsFromQuestion(q, optOut)
        }
    }

    private fun addOptionsFromQuestion(q: JSONObject, optOut: MutableList<CrsOptionEntity>) {
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
                    isDeleted = if (o.has("isDeleted") && !o.isNull("isDeleted")) o.optBoolean("isDeleted") else null
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
        if (id == 0L || moduleId == 0L || moduleAssignmentId==0L) return null
        return ModuleAssignmentEntity(
            id = id,
            moduleId = moduleId,
            moduleAssignmentId = moduleAssignmentId,
            moduleAssignmentType = o.optString("moduleassignment_type", null),
            startDateEpoch =if (o.has("startDate") && !o.isNull("startDate")) o.optString("startDate") else null,
            surveyDuration = if (o.has("surveyDuration") && !o.isNull("surveyDuration")) o.optInt("surveyDuration") else null,
            viewModule = if (o.has("view_module") && !o.isNull("view_module")) o.optInt("view_module") else null,
            countryId = if (o.has("country_id") && !o.isNull("country_id")) o.optLong("country_id") else null,
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

    // ----------------- Mappers -----------------

    private fun toModuleEntity(m: JSONObject): CRSModuleEntity? {
        val id = m.optLong("id", 0L)
        val name = m.optString("name", null)
        if (id == 0L || name.isNullOrBlank()) return null

        return CRSModuleEntity(
            id = id,
            name = name,
            surveyType = if (m.has("surveyType") && !m.isNull("surveyType")) m.optInt("surveyType") else null,
            channel = if (m.has("channel") && !m.isNull("channel")) m.optInt("channel") else null,
            rawJson = m.toString()
        )
    }

    private fun toQuestionEntity(q: JSONObject): CRSQuestionEntity? {
        val questionId = q.optLong("questionId", 0L)
        val questionText = q.optString("questionText", null)
        if (questionId == 0L || questionText.isNullOrBlank()) return null

        return CRSQuestionEntity(
            questionId = questionId,
            responseType = (if (q.has("responseType") && !q.isNull("responseType")) q.optInt("responseType").toString().toLong() else null) as Long?,
            questionText = questionText,
            helpText = q.optString("helpText", null),
            questionCode = q.optString("questionCode", null),
            moduleId = q.optInt("moduleId").toString().toLong(),
            moduleName = q.optString("moduleName", null),
            questionStatus = q.optString("questionStatus", null)
        )
    }

    private fun toQuestionModuleLink(q: JSONObject): CrsQuestionModuleXref? {
        // Some payloads provide moduleId/moduleName/moduleStatus/questionStatus per question
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


    /** Insert single farmer — async fire-and-forget. */
    @JvmStatic
    fun insertFarmerAsync(context: Context, farmer: FarmerEntity) {
        val repo = repository(context)
        CoroutineScope(Dispatchers.IO).launch {
            repo.insertFarmer(farmer)
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

    // Build the repository on-demand from Room
    private fun repository(context: Context): CrsHomeRepo {
        val db = CRSDatabase.getInstance(context)  // requires the getInstance() we added earlier
        return CrsHomeRepo(db.crsDao())
    }
}
