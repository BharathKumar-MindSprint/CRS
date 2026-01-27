package com.olam.digital.crs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olam.digital.crs.extension.postError
import com.olam.digital.crs.extension.postLoading
import com.olam.digital.crs.extension.postSuccess
import com.olam.digital.crs.model.Resource
import com.olam.digital.crs.model.UIQuestion
import com.olam.digital.crs.model.UIQuestionUi
import com.olam.digital.crs.model.mapper.FarmerModuleView
import com.olam.digital.crs.room.FarmerEntity
import com.olam.digital.crs.viewmodel.repo.SurveyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CrsQuestionaryViewModel @Inject constructor(private val repo: SurveyRepository) :
    ViewModel() {
    private val _questions = MutableLiveData<List<UIQuestion>>()
    val questions: LiveData<List<UIQuestion>> = _questions


    private val _modules = MutableLiveData<Resource<List<FarmerModuleView>>>()
    val modules: LiveData<Resource<List<FarmerModuleView>>> = _modules

    fun loadFarmerModules(farmer: FarmerEntity) = viewModelScope.launch {
        try {
            // If you still use legacy auto-sample lookup, ensure it returns List<Long>
            // val autoIds: List<Long> = repo.getAutoSampleIdList(farmer.id)
            _modules.postLoading()
            val result = repo.getModulesForFarmer(farmer, emptyList(), excludeSurveyType8 = true)
            _modules.postSuccess(result)
        } catch (exception: Exception) {
            _modules.postError(exception.message)
            Timber.e("Error loading farmer modules: ${exception.message}")
        }
    }


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


    fun loadModule(moduleId: Long) = viewModelScope.launch {
        _questions.value = repo.getUIQuestionsForModule(moduleId)
    }


    // Answers keyed by questionId
    private val _answers = MutableLiveData<Map<Long, Any?>>(emptyMap())
    val answers: LiveData<Map<Long, Any?>> = _answers


    // Combined UI list with answers injected
    private val _uiQuestions = MediatorLiveData<List<UIQuestionUi>>().apply {
        addSource(_questions) { combine() }
        addSource(_answers) { combine() }
    }
    val uiQuestions: LiveData<List<UIQuestionUi>> = _uiQuestions

    private fun combine() {
        val qs = _questions.value.orEmpty()
        val ans = _answers.value.orEmpty()
        _uiQuestions.value = qs.map { q ->
            UIQuestionUi(
                base = q,
                answer = ans[q.question.questionId]
            )
        }
    }

    fun saveAnswer(questionId: Long, answer: Any?) {
        val current = _answers.value.orEmpty()
        val updated = current.toMutableMap().apply { this[questionId] = answer }
        _answers.value = updated
    }

    // Optionally: get all answers for submit
    fun getAllAnswers(): Map<Long, Any?> = _answers.value.orEmpty()
}
