package com.olam.digital.crs.dynamicAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.olam.digital.crs.adapter.CheckboxVH
import com.olam.digital.crs.databinding.ItemQCheckboxBinding
import com.olam.digital.crs.databinding.ItemQDatepickerBinding
import com.olam.digital.crs.databinding.ItemQDropdownBinding
import com.olam.digital.crs.databinding.ItemQEdittextBinding
import com.olam.digital.crs.databinding.ItemQRadioBinding
import com.olam.digital.crs.databinding.ItemsQLocationCaptureBinding
import com.olam.digital.crs.databinding.LayoutDecimalOnlyChildBinding
import com.olam.digital.crs.databinding.LayoutNumbericOnlyChildBinding
import com.olam.digital.crs.model.AnswerKey
import com.olam.digital.crs.model.QuestionType
import com.olam.digital.crs.model.UIQuestionUi
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel

class RepeaterChildContainer(
    private val context: Context,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val onAnswerLive: (AnswerKey, Any?) -> Unit,
    private val onPickPhoto: (AnswerKey) -> Unit,
    private val onPickFile: (AnswerKey) -> Unit,
    private val onCaptureLocation: (AnswerKey) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val viewModel: CrsQuestionaryViewModel
    ) {

    fun populateChildren(
        container: LinearLayout,
        children: List<UIQuestionUi>
    ) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)

        children.forEachIndexed { index, child ->
            when (child.base.type) {
                QuestionType.CHECKBOX -> {
                    val binding = ItemQCheckboxBinding.inflate(inflater, container, false)
                    CheckboxVH(binding, onAnswer, null).bind(child, index)
                    container.addView(binding.root)
                }
                QuestionType.DATEPICKER -> {
                    val binding = ItemQDatepickerBinding.inflate(inflater, container, false)
                    DatePickerVH(binding, onAnswer, null).bind(child, index)
                    container.addView(binding.root)
                }
                QuestionType.DROPDOWN -> {
                    val binding = ItemQDropdownBinding.inflate(inflater, container, false)
                    DropdownVH(binding, onAnswer, null).bind(child, index)
                    container.addView(binding.root)
                }
                QuestionType.LOCATION -> {
                    val binding = ItemsQLocationCaptureBinding.inflate(inflater, container, false)
                    LocationVH(binding, onCaptureLocation, null).bind(child, index)
                    container.addView(binding.root)
                }
                QuestionType.RADIO -> {
                    val binding = ItemQRadioBinding.inflate(inflater, container, false)
                    val repeaterChildrenMap: MutableMap<Long, List<UIQuestionUi>> = mutableMapOf()
                    RadioVH(binding, onAnswer, null)
                        .bind(child, index)
                    container.addView(binding.root)
                }
                QuestionType.EDITTEXT -> {
                    val binding = ItemQEdittextBinding.inflate(inflater, container, false)
                    TextboxVH(binding, onAnswer, onAnswerLive,signatureCache, null).bind(child, index, false)
                    container.addView(binding.root)
                }
                QuestionType.NUMBERIC -> {
                    val binding = LayoutNumbericOnlyChildBinding.inflate(inflater, container, false)
                    TextboxNumericVH(binding, onAnswer, onAnswerLive,signatureCache, null).bind(child, index, false)
                    container.addView(binding.root)
                }
                QuestionType.DECIMAL -> {
                    val binding = LayoutDecimalOnlyChildBinding.inflate(inflater, container, false)
                    TextboxDecimalVH(binding, onAnswer, onAnswerLive,signatureCache, null).bind(child, index, false)
                    container.addView(binding.root)
                }
                else -> {
                    val binding = ItemQEdittextBinding.inflate(inflater, container, false)
                    TextboxVH(binding, onAnswer, onAnswerLive,signatureCache, null).bind(child, index, false)
                    container.addView(binding.root)
                }
            }
        }
    }

}