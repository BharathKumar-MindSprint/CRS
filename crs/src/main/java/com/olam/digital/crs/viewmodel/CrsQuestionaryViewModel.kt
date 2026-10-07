package com.olam.digital.crs.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olam.digital.crs.extension.CrsSkipLogicEvaluator
import com.olam.digital.crs.extension.postError
import com.olam.digital.crs.extension.postLoading
import com.olam.digital.crs.extension.postNoData
import com.olam.digital.crs.extension.postSuccess
import com.olam.digital.crs.model.AnswerKey
import com.olam.digital.crs.model.QuestionType
import com.olam.digital.crs.model.Resource
import com.olam.digital.crs.model.UIQuestion
import com.olam.digital.crs.model.UIQuestionUi
import com.olam.digital.crs.model.mapper.FarmerModuleView
import com.olam.digital.crs.room.ConditionalQuestionDetailEntity
import com.olam.digital.crs.room.ConditionalTypeEntity
import com.olam.digital.crs.room.CountryEntity
import com.olam.digital.crs.room.CrsCommunityRegionEntity
import com.olam.digital.crs.room.CrsProfileEntity
import com.olam.digital.crs.room.CrsRepeaterMappingEntity
import com.olam.digital.crs.room.DistrictEntity
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.room.PlaceEntity
import com.olam.digital.crs.room.ProductEntity
import com.olam.digital.crs.room.ProfileWithMapping
import com.olam.digital.crs.room.RegionEntity
import com.olam.digital.crs.room.RespondentEntity
import com.olam.digital.crs.viewmodel.repo.SurveyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CrsQuestionaryViewModel @Inject constructor(private val repo: SurveyRepository) :
    ViewModel() {
    private val _questions = MutableLiveData<List<UIQuestion>>()
    val questions: LiveData<List<UIQuestion>> = _questions


    val selectedCrsContentImage = MutableLiveData<Uri>()

    private val _modules = MutableLiveData<Resource<List<FarmerModuleView>>>()
    val modules: LiveData<Resource<List<FarmerModuleView>>> = _modules

    fun loadFarmerModules(farmer: FarmerEntity) = viewModelScope.launch {
        try {
            // If you still use legacy auto-sample lookup, ensure it returns List<Long>
            // val autoIds: List<Long> = repo.getAutoSampleIdList(farmer.id)
            _modules.postLoading()
            val result =
                repo.getModulesForFarmer(farmer, emptyList(), excludeSurveyType8 = true, farmer.id)
            _modules.postSuccess(result)
        } catch (exception: Exception) {
            _modules.postError(exception.message)
            Timber.e("Error loading farmer modules: ${exception.message}")
        }
    }

    private val _modulesCrsCommunity = MutableLiveData<Resource<List<FarmerModuleView>>>()
    val modulesCrsSurvey: LiveData<Resource<List<FarmerModuleView>>> = _modulesCrsCommunity

    fun getAllCrsSurvey(farmer: RespondentEntity) = viewModelScope.launch {
        try {
            // If you still use legacy auto-sample lookup, ensure it returns List<Long>
            // val autoIds: List<Long> = repo.getAutoSampleIdList(farmer.id)
            _modulesCrsCommunity.postLoading()
            val result = repo.getCrsModulesAgainstFarmer(farmer,emptyList(), excludeSurveyType8 = true, farmer.id.toString(),selectedCrsProfileData?.value?.data?.id?:0)
            _modulesCrsCommunity.postSuccess(result)
        } catch (exception: Exception) {
            _modulesCrsCommunity.postError(exception.message)
            Timber.e("Error loading farmer modules: ${exception.message}")
        }
    }

    val isCrsFragment = MutableLiveData<Boolean>(false)


    private val _farmersCrs = MutableLiveData<Resource<List<FarmerEntity>>>()
    val farmersCrs: LiveData<Resource<List<FarmerEntity>>> = _farmersCrs


    fun getAllFarmersCrs() = viewModelScope.launch {
        try {
            _farmersCrs.postLoading()
            val response = repo.getAllFarmersCrs()
            Timber.d("Fetched farmers CRS: ${response.size}")
            _farmersCrs.postSuccess(response)
        } catch (exception: Exception) {
            Timber.e("Error fetching farmers CRS: ${exception.message}")
            _farmersCrs.postError(exception.message)
        }
    }

    private val _crsProfileCommunity = MutableLiveData<Resource<List<RespondentEntity>>>()
    val crsProfileCommunity: LiveData<Resource<List<RespondentEntity>>> = _crsProfileCommunity

    fun getAllCrsRespondents(selectedProfile: Int) = viewModelScope.launch {
        try {
            _crsProfileCommunity.postLoading()
            Timber.d("Fetching CRS respondents for profile ID: $selectedProfile")
            val response = repo.getAllCrsRespondents(selectedProfile)
            Timber.d("Fetched farmers CRS: ${response.size}")
            _crsProfileCommunity.postSuccess(response)
        } catch (exception: Exception) {
            Timber.e("Error fetching farmers CRS: ${exception.message}")
            _crsProfileCommunity.postError(exception.message)
        }
    }


    val selectedModule = MutableLiveData<Resource<FarmerModuleView>>()
    fun selectModule(module: FarmerModuleView) = viewModelScope.launch {
        try {
            selectedModule.postLoading()
            selectedModule.postSuccess(module)
        } catch (exception: Exception) {
            selectedModule.postError(exception.message)
        }
    }


    val selectedUser = MutableLiveData<Resource<FarmerEntity>>()

    fun selectedUser(farmerEntity: FarmerEntity) = viewModelScope.launch {
        try {
            selectedUser.postLoading()
            selectedUser.postSuccess(farmerEntity)
        } catch (exception: Exception) {
            selectedUser.postError(exception.message)
        }
    }


    // condition types (id -> entity), used to resolve operators
    private val _conditionalTypes = MutableLiveData<Map<Long, ConditionalTypeEntity>>(emptyMap())
    val conditionalTypeMap: Map<Long, ConditionalTypeEntity> get() = _conditionalTypes.value.orEmpty()

    val postModuleShimmer  = MutableLiveData<Boolean>(false)
   /* fun loadModule(moduleId: Long) = viewModelScope.launch {
       try {
           postModuleShimmer.postValue(true)
           loadRepeaterMappings(moduleId) // Load repeater mappings first
           _questions.value = repo.getUIQuestionsForModule(moduleId)
           postModuleShimmer.postValue(false)
       }
       catch (exception: Exception) {
           postModuleShimmer.postValue(false)
           Timber.e("Error loading module: ${exception.message}")
       }
    }*/


    val _conditionalDetails = MutableLiveData<List<ConditionalQuestionDetailEntity>>() // questionId → condition expression
    val conditionalDetails: LiveData<List<ConditionalQuestionDetailEntity>> = _conditionalDetails
    fun loadModule(moduleId: Long) = viewModelScope.launch {
        try {
            postModuleShimmer.postValue(true)

            loadRepeaterMappings(moduleId)

            val questions = repo.getUIQuestionsForModule(moduleId)
            val conditionalDetails = repo.getConditionalDetails(moduleId)

            _questions.value = questions
            _conditionalDetails.value = conditionalDetails   // new LiveData
            _conditionalTypes.value = repo.getConditionalTypes().associateBy { it.id }   // add dao/repo method
            postModuleShimmer.postValue(false)
        } catch (exception: Exception) {
            postModuleShimmer.postValue(false)
            Timber.e("Error loading module: ${exception.message}")
        }
    }

    /** Top-level answers keyed by plain questionId (skip-logic triggers live at top level). */
    fun topLevelAnswers(): Map<Long, Any?> =
        _answers.value.orEmpty()
            .filterKeys { it.repeaterId == null }
            .mapKeys { it.key.questionId }

    /** Drops answers of now-hidden questions so they aren't persisted. Returns true if it changed. */
    fun clearAnswersFor(questionIds: Set<Long>): Boolean {
        if (questionIds.isEmpty()) return false
        val current = _answers.value.orEmpty()
        val filtered = current.filterKeys { it.questionId !in questionIds }
        if (filtered.size == current.size) return false
        questionIds.forEach { signatureCache.remove(AnswerKey(it)) }
        _answers.value = filtered          // triggers combine() -> uiQuestions -> observer again
        return true
    }


    private val _repeaterMappings = MutableLiveData<List<CrsRepeaterMappingEntity>>()
    val repeaterMappings: LiveData<List<CrsRepeaterMappingEntity>> = _repeaterMappings

    fun loadRepeaterMappings(moduleId: Long) = viewModelScope.launch {
        try {
            val mappings = repo.getRepeaterMappingsForModule(moduleId)
            _repeaterMappings.postValue(mappings)
        } catch (exception: Exception) {
            Timber.e("Error loading repeater mappings for module $moduleId: ${exception.message}")
        }
    }

    fun getRepeaterMappingsForModule(moduelId: Long) = viewModelScope.launch {
        try {
            repo.getRepeaterMappingsForModule(moduelId)
        } catch (exception: Exception) {
            Timber.e("Error loading repeater mappings for module $moduelId: ${exception.message}")
        }
    }


    // Answers keyed by questionId
    private val _answers = MutableLiveData<Map<AnswerKey, Any?>>(emptyMap())
    val answers: LiveData<Map<AnswerKey, Any?>> = _answers



    // Combined UI list with answers injected
    private val _uiQuestions = MediatorLiveData<List<UIQuestionUi>>().apply {
        addSource(_questions) { combine() }
        addSource(_answers) { combine() }
    }
    val uiQuestions: LiveData<List<UIQuestionUi>> = _uiQuestions

   /* private fun combine() {
        val qs = _questions.value.orEmpty()
        val ans = _answers.value.orEmpty()
        _uiQuestions.value = qs.map { q ->
            UIQuestionUi(
                base = q,
                answer = ans[q.question.questionId]
            )
        }
    }*/




    private fun combine() {
        val qs = _questions.value.orEmpty()
        val ans = _answers.value.orEmpty()

        val uiList = qs.map { q ->
            UIQuestionUi(
                base = q,
                answer = ans[AnswerKey(q.question.questionId)] // normal questions
            )
        }

        val repeaterMappings = _repeaterMappings.value.orEmpty()
        val expandedList = mutableListOf<UIQuestionUi>()

        for ((pos, ui) in uiList.withIndex()) {
            expandedList.add(ui)

            val parentRepeaterTimes = ui.base.question.repeaterTimes
            val parentAnswerCount = (ui.answer as? Int) ?: 0

            if (parentRepeaterTimes != null && parentAnswerCount > 0) {
                val repeatCount = parentAnswerCount.coerceAtMost(parentRepeaterTimes)
                val children = repeaterMappings.filter { it.parentQuestionId == ui.base.question.questionId }

                for (i in 1..repeatCount) {
                    children.forEachIndexed { childIndex, map ->
                        val child = uiList.firstOrNull { it.base.question.questionId == map.childQuestionId } ?: return@forEachIndexed
                        val childKey = AnswerKey(child.base.question.questionId, ui.base.question.questionId, i)

                        expandedList.add(
                            child.copy(
                                base = child.base.copy(
                                    question = child.base.question.copy(
                                        questionText = child.base.question.questionText
                                    )
                                ),
                                answer = ans[childKey],              // ✅ hydrate saved answer
                                repeatIndex = i,                     // ✅ set repeat index
                                repeaterId = ui.base.question.questionId, // ✅ set parent id
                                childIndex = childIndex + 1,         // ✅ set child index
                                childMainQuestionPosition = pos + 1  // optional: for numbering
                            )
                        )
                    }
                }
            }
        }

        _uiQuestions.value = expandedList
    }






    /*  private fun combine() {
          val qs = _questions.value.orEmpty()
          val ans = _answers.value.orEmpty()

          // Build base UI list with normal hydration
          val uiList = qs.map { q ->
              UIQuestionUi(
                  base = q,
                  answer = ans[AnswerKey(q.question.questionId)] // normal questions
              )
          }

          val repeaterMappings = _repeaterMappings.value.orEmpty()
          val expandedList = mutableListOf<UIQuestionUi>()

          for (ui in uiList) {
              expandedList.add(ui)

              val parentRepeaterTimes = ui.base.question.repeaterTimes
              val parentAnswerCount = (ui.answer as? String)?.toIntOrNull() ?: 0

              if (parentRepeaterTimes != null && parentAnswerCount > 0) {
                  val repeatCount = parentAnswerCount.coerceAtMost(parentRepeaterTimes)

                  val children = repeaterMappings.filter { it.parentQuestionId == ui.base.question.questionId }

                  for (i in 1..repeatCount) {
                      for (map in children) {
                          val child = uiList.firstOrNull { it.base.question.questionId == map.childQuestionId } ?: continue

                          val childKey = AnswerKey(
                              questionId = child.base.question.questionId,
                              repeaterId = ui.base.question.questionId, // parent repeater id
                              repeatIndex = i
                          )

                          expandedList.add(
                              child.copy(
                                  base = child.base.copy(
                                      question = child.base.question.copy(
                                          questionText = "${map.repeatLabel ?: ""} $i: ${child.base.question.questionText}"
                                      )
                                  ),
                                  repeatIndex = i,
                                  repeaterId = ui.base.question.questionId,
                                  answer = ans[childKey] // ✅ hydrate saved child answers
                              )
                          )
                      }
                  }
              }
          }

          _uiQuestions.value = expandedList
      }*/


    /** Fast lane for per-keystroke updates. Only emits to observers if this question
     *  is a skip-logic trigger (otherwise the value just sits in the map until blur). */
    fun saveAnswerLive(
        questionId: Long,
        repeaterId: Long? = null,
        repeatIndex: Int? = null,
        value: Any?
    ) {
        val current = _answers.value.orEmpty().toMutableMap()
        val key = AnswerKey(questionId, repeaterId, repeatIndex)
        if (current[key] == value) return  // no-op, don't churn

        current[key] = value

        // Only the top-level skip triggers need to re-render the list on every keystroke.
        val isSkipTrigger = repeaterId == null &&
                conditionalDetails.value.orEmpty().any { it.questionId == questionId && it.isDeleted != true }

        if (isSkipTrigger) {
            _answers.value = current   // triggers combine() → uiQuestions → observer (re-evaluates skip)
        } else {
            // Mutate the map in place without notifying observers; blur will commit it properly.
            // BUT: LiveData backing only emits on setValue/postValue. So for non-trigger fields,
            // we deliberately do NOT set _answers here — the value will be picked up at blur via saveAnswer().
            // We still cache it so saveAnswer() at blur sees the latest. Use a transient buffer:
            pendingTextEdits[key] = value
        }
    }

    // Transient buffer for non-trigger text fields between keystrokes and blur
    private val pendingTextEdits = mutableMapOf<AnswerKey, Any?>()

    /** Called from blur. Flushes pendingTextEdits into _answers in one shot. */
    fun commitPendingTextEdits() {
        if (pendingTextEdits.isEmpty()) return
        val current = _answers.value.orEmpty().toMutableMap()
        var changed = false
        pendingTextEdits.forEach { (k, v) ->
            if (current[k] != v) { current[k] = v; changed = true }
        }
        pendingTextEdits.clear()
        if (changed) _answers.value = current
    }


    fun saveAnswer(questionId: Long, repeaterId:Long?=null,repeatIndex: Int?, value: Any?) {
        val current = _answers.value.orEmpty().toMutableMap()
        current[AnswerKey(questionId, repeaterId,repeatIndex)] = value
        Timber.d("Saved answer for question $questionId (repeaterId: $repeaterId, repeatIndex: $repeatIndex): $value")
        _answers.value = current
    }


    // Optionally: get all answers for submit
    fun getAllAnswers(): Map<AnswerKey, Any?> = _answers.value.orEmpty()



    fun clearAnswers() {
        _answers.value = emptyMap()
        signatureCache.clear()      // ✅ NEW
        pendingTextEdits.clear()    // ✅ also drop any in-flight live edits
    }


/*    fun loadPreviousAnswers(
        moduleId: Long,
        farmerId: String,
        surveyInstanceId: Long? = null
    ) = viewModelScope.launch(Dispatchers.IO) {
        val prev = repo.getPreviousAnswersAsMapNew(
            moduleId = moduleId,
            farmerId = farmerId,
            surveyInstanceId = surveyInstanceId
        )
        _answers.postValue(prev)  // Now Map<AnswerKey, Any?>
    }*/


    fun loadPreviousAnswers(
        moduleId: Long,
        farmerId: String,
        surveyInstanceId: Long? = null
    ) = viewModelScope.launch(Dispatchers.IO) {
        val prev = repo.getPreviousAnswersAsMapNew(
            moduleId = moduleId,
            farmerId = farmerId,
            surveyInstanceId = surveyInstanceId
        )
        _answers.postValue(prev)

        // ✅ NEW: wipe stale signatures from any previous farmer/survey
        signatureCache.clear()

        val sigQuestionIds = repo.getUIQuestionsForModule(moduleId)
            .filter { it.type == QuestionType.SIGNATURE }
            .map { it.question.questionId }
            .toSet()
        prev.forEach { (key, value) ->
            if (key.questionId in sigQuestionIds && key.repeaterId == null && value is String && value.isNotBlank()) {
                signatureCache[AnswerKey(key.questionId)] = value
            }
        }
    }



    // Maps parent questionId → list of child UIQuestionUi
    val repeaterChildrenMap: MutableMap<Long, List<UIQuestionUi>> = mutableMapOf()

    /**
     * Validates required questions: return Pair(isValid, firstErrorMessage?)
     * Adjust the required logic to match your schema (e.g., question.isRequired flag).
     */



 /*   fun validate(): Pair<Boolean, String?> {
        val qs = questions.value.orEmpty()
        val ans = answers.value.orEmpty()

        fun UIQuestion.isRequired(): Boolean = this.question.isMandatory == true

        // --- Skip set from entity-based skip logic (OFIS-parity) -------------------
        val details = conditionalDetails.value ?: emptyList()
        val evaluator = CrsSkipLogicEvaluator(details, conditionalTypeMap)
        val skipped: Set<Long> = evaluator.triggeredQuestionIds(topLevelAnswers())

        // --- Repeater children get validated INSIDE the parent branch, not at top --
        val repeaterChildIds: Set<Long> = repeaterChildrenMap.values.flatten()
            .map { it.base.question.questionId }
            .toSet()

        // --- Hidden keyword-rule conditional children -----------------------------
        // For each (parent → children) pair, check if the parent's current answer
        // makes the children visible. If not, those children are hidden and must
        // NOT be validated.
        val hiddenConditionalChildIds: Set<Long> = buildSet {
            conditionalChildrenMap.forEach { (parentId, children) ->
                val parent = qs.firstOrNull { it.question.questionId == parentId } ?: return@forEach
                val parentAnswer = ans[AnswerKey(parentId)]
                val visible = evaluator.isSatisfied(parentId, parentAnswer)
                    ?: (parentAnswer as? Long ==
                            parent.options.firstOrNull { it.label.equals("Yes", ignoreCase = true) }?.optionId)
                if (!visible) addAll(children.map { it.base.question.questionId })
            }
        }

        // --- Walk top-level questions ---------------------------------------------
        qs.forEach { q ->
            val qId = q.question.questionId

            // Don't validate questions that are hidden or aren't validated at the top
            if (qId in skipped) return@forEach
            if (qId in repeaterChildIds) return@forEach
            if (qId in hiddenConditionalChildIds) return@forEach

            val repeatTimes = q.question.repeaterTimes

            // ── Repeater parent ───────────────────────────────────────────────
            if (repeatTimes != null && repeatTimes > 0) {
                val parentValue = ans[AnswerKey(qId)]
                val parentCount = (parentValue as? Int)
                    ?: (parentValue as? String)?.toIntOrNull()
                    ?: 0

                // Parent count itself is required (count drives the repeats)
                if (q.isRequired() && parentCount <= 0) {
                    return false to "Please fill: ${q.question.questionText}"
                }

                // Validate ONLY the rows the user actually entered
                val limit = parentCount.coerceAtMost(repeatTimes)
                for (i in 1..limit) {
                    repeaterChildrenMap[qId].orEmpty().forEach { child ->
                        if (child.base.question.isMandatory != true) return@forEach
                        val key = AnswerKey(
                            questionId = child.base.question.questionId,
                            repeaterId = qId,
                            repeatIndex = i
                        )
                        if (!isValidAnswer(child.base.type, ans[key])) {
                            return false to "Please fill: ${child.base.question.questionText} ($i)"
                        }
                    }
                }
                return@forEach
            }

            // ── Normal single-instance question ──────────────────────────────
            if (q.isRequired()) {
                val value = ans[AnswerKey(qId)]
                if (!isValidAnswer(q.type, value)) {
                    return false to "Please fill: ${q.question.questionText}"
                }
            }
        }

        return true to null
    }*/


    fun validate(): Pair<Boolean, String?> {
        val qs = questions.value.orEmpty()
        val ans = answers.value.orEmpty()

        fun UIQuestion.isRequired(): Boolean = this.question.isMandatory == true

        // --- Skip set from entity-based skip logic (OFIS-parity) -------------------
        val details = conditionalDetails.value ?: emptyList()
        val evaluator = CrsSkipLogicEvaluator(details, conditionalTypeMap)
        val skipped: Set<Long> = evaluator.triggeredQuestionIds(topLevelAnswers())

        // --- Repeater children get validated INSIDE the parent branch, not at top --
        val repeaterChildIds: Set<Long> = repeaterChildrenMap.values.flatten()
            .map { it.base.question.questionId }
            .toSet()

        // --- Walk top-level questions ---------------------------------------------
        qs.forEach { q ->
            val qId = q.question.questionId

            // Don't validate questions that are hidden or aren't validated at the top
            if (qId in skipped) return@forEach
            if (qId in repeaterChildIds) return@forEach

            val repeatTimes = q.question.repeaterTimes

            // ── Repeater parent ───────────────────────────────────────────────
            if (repeatTimes != null && repeatTimes > 0) {
                val parentValue = ans[AnswerKey(qId)]
                val parentCount = (parentValue as? Int)
                    ?: (parentValue as? String)?.toIntOrNull()
                    ?: 0

                // Parent count itself is required (count drives the repeats)
                if (q.isRequired() && parentCount <= 0) {
                    return false to "Please fill: ${q.question.questionText}"
                }

                // Validate ONLY the rows the user actually entered
                val limit = parentCount.coerceAtMost(repeatTimes)
                for (i in 1..limit) {
                    repeaterChildrenMap[qId].orEmpty().forEach { child ->
                        if (child.base.question.isMandatory != true) return@forEach
                        val key = AnswerKey(
                            questionId = child.base.question.questionId,
                            repeaterId = qId,
                            repeatIndex = i
                        )
                        if (!isValidAnswer(child.base.type, ans[key])) {
                            return false to "Please fill: ${child.base.question.questionText} ($i)"
                        }
                    }
                }
                return@forEach
            }

            // ── Normal single-instance question ──────────────────────────────
            if (q.isRequired()) {
                val value = ans[AnswerKey(qId)]
                if (!isValidAnswer(q.type, value)) {
                    return false to "Please fill: ${q.question.questionText}"
                }
            }
        }

        return true to null
    }



    private fun isValidAnswer(type: QuestionType, value: Any?): Boolean {
        return when (type) {
            QuestionType.CHECKBOX,
            QuestionType.PHOTO_CHECKBOX -> (value as? List<*>)?.isNotEmpty() == true

            QuestionType.EDITTEXT,
            QuestionType.DATEPICKER,
            QuestionType.LOCATION,
            QuestionType.PHOTO,
            QuestionType.FILE_UPLOAD,
            QuestionType.NUMBERIC,
            QuestionType.DECIMAL -> !(value as? String).isNullOrBlank()

            QuestionType.RADIO,
            QuestionType.DROPDOWN -> value is Long

            QuestionType.RATING ->
                (value as? String)?.isNotBlank() == true || (value as? Int)?.let { it > 0 } == true

            QuestionType.YESNO -> value is Boolean

            QuestionType.SIGNATURE -> value != null // or signatureCache.containsKey(questionId)

            else -> value != null
        }
    }




    val signatureCache = mutableMapOf<AnswerKey, String>()

    fun saveSignature(questionId: Long, base64: String?) {
        if (base64 == null) signatureCache.remove(AnswerKey(questionId))
        else signatureCache[AnswerKey(questionId)] = base64
    }

    fun getSignature(questionId: Long): String? {
        return signatureCache[AnswerKey(questionId)]
    }


    private val _submitState = MutableLiveData<Resource<Unit>>()
    val submitState: LiveData<Resource<Unit>> = _submitState


    /**
     * Replace with your real API call. Emits LOADING -> SUCCESS/ERROR.
     */
    fun submitSurvey(
        farmerId: String,
        moduleId: Long,
        deviceId: String?,
        appVersion: String?,
        context: Context,
        submitedId: Long
    ) {
        _submitState.postLoading()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // TODO: call your repository API:
                val uiQs = questions.value.orEmpty()
                val currentAnswers = answers.value.orEmpty()

                repo.persistSurvey(
                    farmerId = farmerId,
                    moduleId = moduleId,
                    uiQuestions = uiQs,
                    answers = currentAnswers,
                    deviceId = deviceId,
                    appVersion = appVersion ?: "",
                    context,
                    submitedId,
                    if (selectedCrsContentImage.value == null) "" else selectedCrsContentImage.value.toString(),
                    signatureCache,
                   isCrsFragment.value ?: false
                )

                // Simulate a small delay

                kotlinx.coroutines.delay(800)
                _submitState.postSuccess()
            } catch (t: Throwable) {
                Timber.e("Survey submission failed: ${t.message}", t)
                _submitState.postError("Submission failed: ${t.message}")
            }
        }
    }

    fun clearSubmitSurvey(lifecycleOwner: LifecycleOwner) {
        _submitState.postNoData()
    }



    private val _selectedCrsProfile = MutableLiveData<Resource<RespondentEntity>>()
    val selectedCrsProfiles : LiveData<Resource<RespondentEntity>> = _selectedCrsProfile
    fun selectedCrsProfile(crsProfileEntity: RespondentEntity) = viewModelScope.launch {
        try {
            _selectedCrsProfile.postLoading()
            _selectedCrsProfile.postSuccess(crsProfileEntity)
        } catch (exception: Exception) {
            Timber.e("Error selecting CRS profile: ${exception.message}")
            _selectedCrsProfile.postError(exception.message)
        }
    }


    private val _selectedCrsProfileStatement = MutableLiveData<Resource<CrsProfileEntity>>()
    val selectedCrsProfileData : LiveData<Resource<CrsProfileEntity>> = _selectedCrsProfileStatement
    fun selectedCrsProfile(crsProfileEntity: CrsProfileEntity) = viewModelScope.launch {
        try {
            _selectedCrsProfileStatement.postLoading()
            Timber.d("Selected CRS profile: ${crsProfileEntity.profileName}")
            _selectedCrsProfileStatement.postSuccess(crsProfileEntity)
        } catch (exception: Exception) {
            Timber.e("Error selecting CRS profile: ${exception.message}")
            _selectedCrsProfileStatement.postError(exception.message)
        }
    }

    private val _CrsProfile = MutableLiveData<Resource<List<CrsProfileEntity>>>()
    val farmersCrsProfile: LiveData<Resource<List<CrsProfileEntity>>> = _CrsProfile


    fun getAllCrsProfile() {
        viewModelScope.launch {
            try {
                _CrsProfile.postLoading()
                val response = repo.getAllCrsProfile()
                Timber.d("Fetched farmers CRS: ${response.size}")
                _CrsProfile.postSuccess(response)
            } catch (exception: Exception) {
                Timber.e("Error fetching farmers CRS: ${exception.message}")
                _CrsProfile.postError(exception.message)
            }
        }
    }

 private val _CrsProfileWithMapping = MutableLiveData<Resource<List<ProfileWithMapping>>>()
    val farmersCrsProfileWithMapping: LiveData<Resource<List<ProfileWithMapping>>> = _CrsProfileWithMapping


    fun getProfilesWithCommunityMapping() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _CrsProfileWithMapping.postLoading()
                val response = repo.getProfilesWithCommunityMapping()
                Timber.d("Fetched farmers CRS from getProfilewithCommunityMapping: ${response.size}")
                _CrsProfileWithMapping.postSuccess(response)
            } catch (exception: Exception) {
                Timber.e("Error fetching farmers getProfilewithCommunityMapping: ${exception.message}")
                _CrsProfileWithMapping.postError(exception.message)
            }
        }
    }



    private val _CrsCountryIds = MutableLiveData<Resource<List<CountryEntity>>>()
    val farmersCrsProfileCountryName: LiveData<Resource<List<CountryEntity>>> = _CrsCountryIds


    fun getAllCountryNameFromMapping() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _CrsCountryIds.postLoading()
                val response = repo.getAllCountryNameMapping()
                Timber.d("Fetched farmers Country getAllCountryNameFromMapping: ${response.size}")
                _CrsCountryIds.postSuccess(response)
            } catch (exception: Exception) {
                Timber.e("Error fetching farmers Country getAllCountryNameFromMapping: ${exception.message}")
                _CrsCountryIds.postError(exception.message)
            }
        }
    }


    //product mapping for region and country
    private val _CrsProductIds = MutableLiveData<Resource<List<ProductEntity>>>()
    val farmersCrsProfileProductName: LiveData<Resource<List<ProductEntity>>> = _CrsProductIds

    fun getAllProductNameFromMapping() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _CrsProductIds.postLoading()
                val response = repo.getAllProductNameMapping()
                Timber.d("Fetched farmers Product getAllProductNameFromMapping: ${response.size}")
                _CrsProductIds.postSuccess(response)
            } catch (exception: Exception) {
                Timber.e("Error fetching farmers Product getAllProductNameFromMapping: ${exception.message}")
                _CrsProductIds.postError(exception.message)
            }
        }
    }

    private val _CrsRegionIds = MutableLiveData<Resource<List<CrsCommunityRegionEntity>>>()
    val farmersCrsProfileRegionName: LiveData<Resource<List<CrsCommunityRegionEntity>>> = _CrsRegionIds


    fun getAllRegionIdsFromMapping() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _CrsRegionIds.postLoading()
                val response = repo.getAllRegionIdsFromMapping()
                Timber.d("Fetched farmers getAllRegionIdsFromMapping: ${response.size}")
                _CrsRegionIds.postSuccess(response)
            } catch (exception: Exception) {
                Timber.e("Error fetching farmers getAllRegionIdsFromMapping: ${exception.message}")
                _CrsRegionIds.postError(exception.message)
            }
        }
    }




    //district mapping for region and country
    private  val _CrsDistrictIds = MutableLiveData<Resource<List<DistrictEntity>>>()
    val farmersCrsProfileDistrictName: LiveData<Resource<List<DistrictEntity>>> = _CrsDistrictIds


    fun getAllDistrictNameFromMapping() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _CrsDistrictIds.postLoading()
                val response = repo.getAllDistrictNameMapping()
                Timber.d("Fetched farmers Product getAllDistrictNameFromMapping: ${response.size}")
                _CrsDistrictIds.postSuccess(response)
            } catch (exception: Exception) {
                Timber.e("Error fetching farmers Product getAllDistrictNameFromMapping: ${exception.message}")
                _CrsDistrictIds.postError(exception.message)
            }
        }
    }


    //place mapping for region and country
    private  val _CrsPlaceIds = MutableLiveData<Resource<List<PlaceEntity>>>()
    val farmersCrsProfilePlaceName: LiveData<Resource<List<PlaceEntity>>> = _CrsPlaceIds


    fun getAllPlaceNameFromMapping() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _CrsPlaceIds.postLoading()
                val response = repo.getAllPlaceNameMapping()
                Timber.d("Fetched farmers Product getAllPlaceNameFromMapping: ${response.size}")
                _CrsPlaceIds.postSuccess(response)
            } catch (exception: Exception) {
                Timber.e("Error fetching farmers Product getAllPlaceNameFromMapping: ${exception.message}")
                _CrsPlaceIds.postError(exception.message)
            }
        }
    }


    private val _CrsProfileSaved = MutableLiveData<Resource<Boolean>>()
    val crsProfileSaved: LiveData<Resource<Boolean>> = _CrsProfileSaved

    fun saveCrsProfile(crsProfileEntity: RespondentEntity) {
        viewModelScope.launch {
            try {
                _CrsProfileSaved.postLoading()
                repo.saveCrsRespondentEntity(crsProfileEntity)
                _CrsProfileSaved.postSuccess(true)
            } catch (exception: Exception) {
                Timber.e("Error saving CRS profile: ${exception.message}")
                _CrsProfileSaved.postError(exception.message)
            }
        }
    }

    fun clearSaveRespondent() {
        _CrsProfileSaved.postNoData()
        selectResponsentImage.postValue("")
    }

    val selectResponsentImage = MutableLiveData<String>()
    fun updateProfileImage(internalPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                selectResponsentImage.postValue(internalPath)
            } catch (exception: Exception) {
                Timber.e("Error updating profile image: ${exception.message}")
                selectResponsentImage.postValue("")
            }
        }
    }

    fun clearData() {
        viewModelScope.launch(Dispatchers.IO)
        {
            selectedUser.postNoData()
            _modules.postNoData()
            _modulesCrsCommunity.postNoData()
            _selectedCrsProfile.postNoData()
        }
    }


}
