// adapter/CrsDynamicQustionsAdapter.kt
package com.olam.digital.crs.dynamicAdapter

import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.provider.OpenableColumns
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.olam.digital.crs.databinding.ItemQCheckboxBinding
import com.olam.digital.crs.databinding.ItemQDatepickerBinding
import com.olam.digital.crs.databinding.ItemQDropdownBinding
import com.olam.digital.crs.databinding.ItemQEdittextBinding
import com.olam.digital.crs.databinding.ItemQFileUploadBinding
import com.olam.digital.crs.databinding.ItemQPhotoCheckboxBinding
import com.olam.digital.crs.databinding.ItemQPhotoCheckboxMainBinding
import com.olam.digital.crs.databinding.ItemQPhotoUploadBinding
import com.olam.digital.crs.databinding.ItemQRadioBinding
import com.olam.digital.crs.databinding.ItemQSignatureCheckbocBinding
import com.olam.digital.crs.databinding.ItemsQLocationCaptureBinding
import com.olam.digital.crs.extension.setUpAdapter
import com.olam.digital.crs.model.QuestionType
import com.olam.digital.crs.model.UIQuestionUi
import com.olam.digital.crs.model.ViewType
import com.olam.digital.crs.room.CrsOptionEntity
import timber.log.Timber
import java.io.File
import java.util.Calendar
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.LayoutDecimalOnlyChildBinding
import com.olam.digital.crs.databinding.LayoutNumbericOnlyChildBinding
import com.olam.digital.crs.extension.base64ToBitmap
import com.olam.digital.crs.extension.bitmapToBase64
import com.olam.digital.crs.extension.displayNumbering
import com.olam.digital.crs.extension.localizedLabel
import com.olam.digital.crs.extension.localizedText
import com.olam.digital.crs.extension.setQuestionWithAsterisk
import com.olam.digital.crs.extension.showOrHide
import com.olam.digital.crs.model.AnswerKey
import com.olam.digital.crs.model.UIQuestion
import com.olam.digital.crs.viewmodel.api.CrsApi
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel

class CrsDynamicQustionsAdapter(
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val onAnswerLive: (AnswerKey, Any?) -> Unit,
    private val onPickPhoto: (AnswerKey) -> Unit,
    private val onPickFile: (AnswerKey) -> Unit,
    private val onCaptureLocation: (AnswerKey) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private var childRecyclerView: RecyclerView? = null,
    private val viewModel: CrsQuestionaryViewModel
) : ListAdapter<UIQuestionUi, RecyclerView.ViewHolder>(Diff) {

    init {
        setHasStableIds(true)
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        this.childRecyclerView = recyclerView
    }

    fun getRecyclerView(): RecyclerView? = childRecyclerView

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        val qId = item.base.question.questionId
        val repeatIndex = item.repeatIndex ?: 0
        val repeaterId = item.repeaterId ?: 0
        val childIndex = item.childIndex ?: 0
        return "$qId-$repeaterId-$repeatIndex-$childIndex".hashCode().toLong()
    }

    override fun getItemViewType(position: Int) = when (getItem(position).base.type) {
        QuestionType.CHECKBOX -> ViewType.CHECKBOX.ordinal
        QuestionType.DATEPICKER -> ViewType.DATE_PICKER.ordinal
        QuestionType.DROPDOWN -> ViewType.DROPDOWN.ordinal
        QuestionType.LOCATION -> ViewType.LOCATION.ordinal
        QuestionType.PHOTO -> ViewType.PHOTO_UPLOAD.ordinal
        QuestionType.PHOTO_CHECKBOX -> ViewType.PHOTO_CHECKBOX.ordinal
        QuestionType.RADIO -> ViewType.RADIO.ordinal
        QuestionType.EDITTEXT -> ViewType.TEXTBOX.ordinal
        QuestionType.FILE_UPLOAD -> ViewType.FILE_UPLOAD.ordinal
        QuestionType.SIGNATURE -> ViewType.SIGNATURE.ordinal
        QuestionType.DECIMAL -> ViewType.DECIMAL.ordinal
        QuestionType.NUMBERIC -> ViewType.NUMBERIC.ordinal
        QuestionType.REPEATER -> ViewType.REPEATER.ordinal
        else -> ViewType.TEXTBOX.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (ViewType.entries[viewType]) {
            ViewType.CHECKBOX -> CheckboxVH(
                ItemQCheckboxBinding.inflate(inf, parent, false),
                onAnswer,
                childRecyclerView
            )

            ViewType.DATE_PICKER -> DatePickerVH(
                ItemQDatepickerBinding.inflate(inf, parent, false),
                onAnswer,
                childRecyclerView
            )

            ViewType.DROPDOWN -> DropdownVH(
                ItemQDropdownBinding.inflate(inf, parent, false),
                onAnswer,
                childRecyclerView
            )

            ViewType.LOCATION -> LocationVH(
                ItemsQLocationCaptureBinding.inflate(
                    inf,
                    parent,
                    false
                ), onCaptureLocation, childRecyclerView
            )

            ViewType.PHOTO_CHECKBOX -> PhotoCheckboxVH(
                ItemQPhotoCheckboxMainBinding.inflate(
                    inf,
                    parent,
                    false
                ), onAnswer, childRecyclerView
            )

            ViewType.PHOTO_UPLOAD -> PhotoUploadVH(
                ItemQPhotoUploadBinding.inflate(
                    inf,
                    parent,
                    false
                ), onPickPhoto, onAnswer, childRecyclerView
            )

            ViewType.RADIO -> RadioVH(
                ItemQRadioBinding.inflate(inf, parent, false),
                onAnswer,
                childRecyclerView
            )

            ViewType.FILE_UPLOAD -> FileUploadVH(
                ItemQFileUploadBinding.inflate(inf, parent, false),
                onPickFile,
                onAnswer,
                childRecyclerView
            )

            ViewType.SIGNATURE -> SignatureVH(
                ItemQSignatureCheckbocBinding.inflate(
                    inf,
                    parent,
                    false
                ), onAnswer,signatureCache, childRecyclerView
            )

            ViewType.TEXTBOX -> TextboxVH(
                ItemQEdittextBinding.inflate(inf, parent, false),
                onAnswer,
                onAnswerLive,
                signatureCache,
                childRecyclerView
            )

            ViewType.DECIMAL -> TextboxDecimalVH(
                LayoutDecimalOnlyChildBinding.inflate(
                    inf,
                    parent,
                    false
                ), onAnswer, onAnswerLive,signatureCache, childRecyclerView
            )

            ViewType.NUMBERIC -> TextboxNumericVH(
                LayoutNumbericOnlyChildBinding.inflate(
                    inf,
                    parent,
                    false
                ), onAnswer,onAnswerLive, signatureCache, childRecyclerView
            )

            ViewType.REPEATER -> TextboxRepeaterNumericVH(
                ItemQEdittextBinding.inflate(inf, parent, false),
                onAnswer,
                signatureCache,
                {
                    CrsDynamicQustionsAdapter(
                        onAnswer,
                        onAnswerLive,
                        onPickPhoto,
                        onPickFile,
                        onCaptureLocation,
                        signatureCache,
                        viewModel = viewModel
                    )
                },
                repeaterChildrenMap,
                RepeaterChildContainer(
                    parent.context,
                    onAnswer,
                    onAnswerLive,
                    onPickPhoto,
                    onPickFile,
                    onCaptureLocation,
                    signatureCache,
                    viewModel
                ),
                viewModel
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val isLast = position == currentList.lastIndex
        when (holder) {
            is CheckboxVH -> holder.bind(item, position)
            is DatePickerVH -> holder.bind(item, position)
            is DropdownVH -> holder.bind(item, position)
            is LocationVH -> holder.bind(item, position)
            is PhotoCheckboxVH -> holder.bind(item, position)
            is PhotoUploadVH -> holder.bind(item, position)
            is RadioVH -> holder.bind(item, position)
            is TextboxVH -> holder.bind(item, position, isLast)
            is FileUploadVH -> holder.bind(item, position)
            is SignatureVH -> holder.bind(item, position)
            is TextboxRepeaterNumericVH -> holder.bind(item, position, isLast)
            is TextboxNumericVH -> holder.bind(item, position, isLast)
            is TextboxDecimalVH -> holder.bind(item, position, isLast)
        }
    }

    // Supplied by the Fragment from CrsSkipLogicEvaluator. Returns:
//   true  -> condition satisfied, show children
//   false -> not satisfied, hide
//   null  -> evaluator not wired, fall back to legacy "Yes"
   /* private var conditionSatisfied: ((Long, Any?) -> Boolean?)? = null
    fun setConditionEvaluator(decider: (Long, Any?) -> Boolean?) {
        conditionSatisfied = decider
    }

    private val conditionalChildrenMap = mutableMapOf<Long, List<UIQuestionUi>>()
    fun setConditionalChildrenMap(map: Map<Long, List<UIQuestionUi>>) {
        conditionalChildrenMap.clear()
        conditionalChildrenMap.putAll(map)
    }*/

    private val repeaterChildrenMap = mutableMapOf<Long, List<UIQuestionUi>>()
    fun setRepeaterChildrenMap(map: Map<Long, List<UIQuestionUi>>) {
        repeaterChildrenMap.clear()
        repeaterChildrenMap.putAll(map)
    }

   /* object Diff : DiffUtil.ItemCallback<UIQuestionUi>() {
        override fun areItemsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi) =
            oldItem.base.question.questionId == newItem.base.question.questionId &&
                    oldItem.repeaterId == newItem.repeaterId &&
                    oldItem.repeatIndex == newItem.repeatIndex &&
                    oldItem.childIndex == newItem.childIndex

        override fun areContentsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi) =
            oldItem.base.type == newItem.base.type &&
                    oldItem.childMainQuestionPosition == newItem.childMainQuestionPosition &&
                    answersEqual(oldItem.answer, newItem.answer)

        private fun answersEqual(a: Any?, b: Any?) =
            if (a is List<*> && b is List<*>) a == b else a == b
    }*/
   object Diff : DiffUtil.ItemCallback<UIQuestionUi>() {
       override fun areItemsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi) =
           oldItem.base.question.questionId == newItem.base.question.questionId &&
                   oldItem.repeaterId == newItem.repeaterId &&
                   oldItem.repeatIndex == newItem.repeatIndex &&
                   oldItem.childIndex == newItem.childIndex

       override fun areContentsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi): Boolean {
           if (oldItem.base.type != newItem.base.type) return false
           if (oldItem.childMainQuestionPosition != newItem.childMainQuestionPosition) return false

           // ✅ Signature manages its own pad state internally. Treat any answer change as
           //    "same content" so DiffUtil doesn't trigger a rebind that would redraw the pad
           //    and cause a visible flicker after onSigned commits the sentinel.
           if (newItem.base.type == QuestionType.SIGNATURE) return true

           return answersEqual(oldItem.answer, newItem.answer)
       }

       private fun answersEqual(a: Any?, b: Any?) =
           if (a is List<*> && b is List<*>) a == b else a == b
   }
}

/* ─────────────────────────── ViewHolders ─────────────────────────── */

class TextboxVH(
    private val binding: ItemQEdittextBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val onAnswerLive: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {

    private var internalChange = false
    private var textWatcher: TextWatcher? = null
    private var commitRunnable: Runnable? = null
    fun bind(item: UIQuestionUi, position1: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        etAnswer.hint = q.question.helpText ?: ""

        val text = item.answer as? String ?: ""
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.text?.let { etAnswer.setSelection(text.length.coerceAtMost(it.length)) }
            internalChange = false
        }
        textWatcher?.let { etAnswer.removeTextChangedListener(it) }
        textWatcher = etAnswer.addTextChangedListener {
            if (internalChange) return@addTextChangedListener
            val value = it?.toString()
            val emitted = if (value.isNullOrEmpty()) null else value
            onAnswerLive(
                AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                emitted
            )
            if (isLast) {
                signatureCache[AnswerKey(q.question.questionId)] = value.orEmpty()
            }
        }

        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            etAnswer.isCursorVisible = hasFocus
            if (!hasFocus) {
                val finalText = etAnswer.text?.toString()
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    if (finalText.isNullOrEmpty()) null else finalText
                )
                if (finalText.isNullOrEmpty()) {
                    signatureCache.remove(AnswerKey(q.question.questionId))
                }
            }
            if (hasFocus) {
                (itemView.parent as? RecyclerView)?.let { rv ->
                    for (i in 0 until rv.childCount) {
                        val child = rv.getChildAt(i)
                        if (child !== itemView) {
                            child.findViewById<EditText>(R.id.etAnswer)?.clearFocus()
                        }
                    }
                }
            }
        }
    }
}

class TextboxNumericVH(
    private val binding: LayoutNumbericOnlyChildBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val onAnswerLive: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {

    private var internalChange = false
    private var textWatcher: TextWatcher? = null
    private var commitRunnable: Runnable? = null

    init {
        binding.etAnswer.isFocusableInTouchMode = true
        binding.etAnswer.keyListener = DigitsKeyListener.getInstance("0123456789-")
        binding.etAnswer.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        binding.etAnswer.imeOptions = EditorInfo.IME_ACTION_NEXT
    }

    fun bind(item: UIQuestionUi, position1: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        etAnswer.hint = q.question.helpText ?: ""


        // ✅ 3. Apply SIMPLE numeric filter ONLY
        etAnswer.filters = arrayOf(SimpleNumericInputFilter())

        val text = item.answer as? String ?: ""
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.text?.let { etAnswer.setSelection(text.length.coerceAtMost(it.length)) }
            internalChange = false
        }
        /*      etAnswer.isVisible = true
              etAnswer.isVisible = etAnswer.isFocused*/
        /* textWatcher?.let { etAnswer.removeTextChangedListener(it) }
         textWatcher = etAnswer.addTextChangedListener {
             Timber.d("NUMERIC_WATCHER: text='${it.toString()}'")
             if (internalChange) return@addTextChangedListener
             if (isLast) signatureCache[AnswerKey(q.question.questionId)] = it.toString()
         }*/

        textWatcher?.let { etAnswer.removeTextChangedListener(it) }
        textWatcher = etAnswer.addTextChangedListener {
            if (internalChange) return@addTextChangedListener
            val value = it?.toString()
            val emitted = if (value.isNullOrEmpty()) null else value
            onAnswerLive(
                AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                emitted
            )
            if (isLast) {
                signatureCache[AnswerKey(q.question.questionId)] = value.orEmpty()
            }
        }

        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            etAnswer.isCursorVisible = hasFocus
            if (!hasFocus) {
                val finalText = etAnswer.text?.toString()
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    if (finalText.isNullOrEmpty()) null else finalText
                )
                if (finalText.isNullOrEmpty()) {
                    signatureCache.remove(AnswerKey(q.question.questionId))
                }
            }
            if (hasFocus) {
                (itemView.parent as? RecyclerView)?.let { rv ->
                    for (i in 0 until rv.childCount) {
                        val child = rv.getChildAt(i)
                        if (child !== itemView) {
                            child.findViewById<EditText>(R.id.etAnswer)?.clearFocus()
                        }
                    }
                }
            }
        }

        // ✅ Handle IME "Next" button WITH DELAY to prevent focus jumping
        etAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                // Save answer
                if (!etAnswer.text.isNullOrEmpty()) {
                    onAnswer(
                        AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                        etAnswer.text.toString()
                    )
                }

                // ✅ Just move focus - keyboard follows automatically
                etAnswer.post {
                    val nextView = etAnswer.focusSearch(View.FOCUS_DOWN)
                    if (nextView != null && nextView !== etAnswer) {
                        nextView.requestFocus()
                    }
                }
                true
            } else false
        }
    }
}

// ✅ FIXED DecimalDigitsInputFilter
/**
 * ✅ BULLETPROOF decimal filter - accepts digits + dots
 */
class DecimalDigitsInputFilter(
    private val maxDigitsBeforeDecimal: Int,
    private val maxDigitsAfterDecimal: Int
) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // Step 1: Allow delete/backspace
        if (source == null || source.isEmpty()) {
            Timber.d("DECIMAL: delete/backspace allowed")
            return null
        }

        val sourceStr = source.subSequence(start, end).toString()
        val destStr = dest?.toString() ?: ""

        // Step 2: Build new text
        val newText = destStr.substring(0, dstart) + sourceStr + destStr.substring(dend)
        Timber.d("DECIMAL: source='$sourceStr', newText='$newText', max=$maxDigitsBeforeDecimal.$maxDigitsAfterDecimal")

        // Step 3: Allow empty
        if (newText.isEmpty()) {
            Timber.d("DECIMAL: ACCEPTED empty")
            return null
        }

        // Step 4: Check for invalid characters (only digits and dots)
        for (char in newText) {
            if (char != '.' && !char.isDigit()) {
                Timber.d("DECIMAL: REJECTED - invalid char '$char'")
                return ""
            }
        }

        // Step 5: Check dot count (max 1 dot)
        val dotCount = newText.count { it == '.' }
        if (dotCount > 1) {
            Timber.d("DECIMAL: REJECTED - multiple dots ($dotCount)")
            return ""
        }

        // Step 6: Allow trailing dot (user typing "5.")
        if (newText.endsWith(".")) {
            Timber.d("DECIMAL: ACCEPTED trailing dot '$newText'")
            return null
        }

        // Step 7: If has decimal, validate both parts
        if (newText.contains(".")) {
            val parts = newText.split(".")
            val before = parts[0]
            val after = parts.getOrNull(1) ?: ""

            Timber.d("DECIMAL: has dot - before='$before' after='$after'")

            // Check before decimal (can be empty like ".5")
            if (before.isNotEmpty() && before.length > maxDigitsBeforeDecimal) {
                Timber.d("DECIMAL: REJECTED - too many digits before: '${before.length}' > $maxDigitsBeforeDecimal")
                return ""
            }

            // Check after decimal
            if (after.length > maxDigitsAfterDecimal) {
                Timber.d("DECIMAL: REJECTED - too many digits after: '${after.length}' > $maxDigitsAfterDecimal")
                return ""
            }

            Timber.d("DECIMAL: ACCEPTED decimal '$newText'")
            return null
        }

        // Step 8: No decimal - just integer
        if (newText.length > maxDigitsBeforeDecimal) {
            Timber.d("DECIMAL: REJECTED - integer too long: '${newText.length}' > $maxDigitsBeforeDecimal")
            return ""
        }

        Timber.d("DECIMAL: ACCEPTED integer '$newText'")
        return null // accept
    }
}

class TextboxDecimalVH(
    private val binding: LayoutDecimalOnlyChildBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val onAnswerLive: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {

    private var internalChange = false
    private var textWatcher: TextWatcher? = null
    private var commitRunnable: Runnable? = null

    init {
        binding.etAnswer.isFocusableInTouchMode = true
        binding.etAnswer.keyListener = DigitsKeyListener.getInstance("0123456789.,-")
        binding.etAnswer.inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL or
                InputType.TYPE_NUMBER_FLAG_SIGNED
        binding.etAnswer.imeOptions = EditorInfo.IME_ACTION_NEXT
    }

    fun bind(item: UIQuestionUi, position1: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        etAnswer.hint = q.question.helpText ?: ""

        // ✅ 1. Set inputType first
        /*  etAnswer.inputType = InputType.TYPE_CLASS_NUMBER or
                  InputType.TYPE_NUMBER_FLAG_DECIMAL or
                  InputType.TYPE_NUMBER_FLAG_SIGNED

          // ✅ 2. Set imeOptions immediately
          etAnswer.imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_NEXT*/

        // ✅ 3. Apply decimal filter
        val maxBefore = q.question.characterLimit?.toInt() ?: 10
        val maxAfter = q.question.decimalLimit?.toInt() ?: 2
        etAnswer.filters = arrayOf(DecimalDigitsInputFilter(maxBefore, maxAfter))

        Timber.d("DECIMAL: max=$maxBefore.$maxAfter")
        binding.etAnswer.setOnKeyListener { _, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_TAB // ignore Tab
        }


        val text = item.answer as? String ?: ""
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.text?.let { etAnswer.setSelection(text.length.coerceAtMost(it.length)) }
            internalChange = false
        }


        textWatcher?.let { etAnswer.removeTextChangedListener(it) }
        textWatcher = etAnswer.addTextChangedListener {
            if (internalChange) return@addTextChangedListener
            val value = it?.toString()
            val emitted = if (value.isNullOrEmpty()) null else value
            onAnswerLive(
                AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                emitted
            )
            if (isLast) {
                signatureCache[AnswerKey(q.question.questionId)] = value.orEmpty()
            }
        }

        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            etAnswer.isCursorVisible = hasFocus
            if (!hasFocus) {
                val finalText = etAnswer.text?.toString()
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    if (finalText.isNullOrEmpty()) null else finalText
                )
                if (finalText.isNullOrEmpty()) {
                    signatureCache.remove(AnswerKey(q.question.questionId))
                }
            }
            if (hasFocus) {
                (itemView.parent as? RecyclerView)?.let { rv ->
                    for (i in 0 until rv.childCount) {
                        val child = rv.getChildAt(i)
                        if (child !== itemView) {
                            child.findViewById<EditText>(R.id.etAnswer)?.clearFocus()
                        }
                    }
                }
            }
        }

       // ✅ Handle IME "Next" button WITH DELAY to prevent focus jumping
      /*  etAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                // Save answer
                if (!etAnswer.text.isNullOrEmpty()) {
                    onAnswer(
                        AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                        etAnswer.text.toString()
                    )
                }
                // ✅ Just move focus - keyboard follows automatically
                etAnswer.post {
                    val nextView = etAnswer.focusSearch(View.FOCUS_DOWN)
                    if (nextView != null && nextView !== etAnswer) {
                        nextView.requestFocus()
                    }
                }
                true
            } else false
        }*/
    }
}


private class TextboxRepeaterNumericVH(
    private val binding: ItemQEdittextBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childAdapterFactory: () -> CrsDynamicQustionsAdapter,
    private val repeaterChildrenMap: MutableMap<Long, List<UIQuestionUi>>,
    private val repeaterChildContainer: RepeaterChildContainer,
    private val viewModel: CrsQuestionaryViewModel
) : RecyclerView.ViewHolder(binding.root) {

    private val childAdapter = childAdapterFactory()
    private var internalChange = false
    private var textWatcher: TextWatcher? = null
    private var commitRunnable: Runnable? = null
    private var lastRepeatCount = -1   // guards redundant submitChildren

    init {
        binding.rvChildQuestions.apply {
            adapter = childAdapter
            isNestedScrollingEnabled = false
            // layoutManager set via app:layoutManager in XML
        }
        //repeaterChildContainer = RepeaterChildContainer(binding.rvChildQuestions.context, onAnswer, onPickPhoto = {}, onPickFile = {}, onCaptureLocation = {}, signatureCache)
        childAdapter.setRecyclerView(binding.rvChildQuestions)
        childAdapter.submitList(emptyList())

        // Keep parent focusable first; children focus only on explicit tap
        (binding.root as? ViewGroup)?.descendantFocusability =
            ViewGroup.FOCUS_AFTER_DESCENDANTS

        // When ANY child field gains focus, drop the parent count field's cursor
        binding.rvChildQuestions.viewTreeObserver.addOnGlobalFocusChangeListener { _, newFocus ->
            if (newFocus != null && newFocus !== binding.etAnswer && binding.etAnswer.isFocused) {
                binding.etAnswer.clearFocus()
            }
        }
    }


    fun bind(item: UIQuestionUi, position: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        val key = AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex)

        // Reset recycled state
        lastRepeatCount = -1
        commitRunnable?.let { etAnswer.removeCallbacks(it) }
        commitRunnable = null
        if (etAnswer.isFocused) etAnswer.clearFocus()

        // Title + mandatory marker
        tvTitle.text =
            "${item.displayNumbering(position + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)

        // Input type BEFORE setting text
        etAnswer.inputType = InputType.TYPE_CLASS_NUMBER

        // Remove stale watcher before programmatic setText
        textWatcher?.let { etAnswer.removeTextChangedListener(it) }

        // Hydrate existing answer
        val text = when (val ans = item.answer) {
            is String -> ans.toDoubleOrNull()?.let {
                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
            } ?: ans   // not numeric, keep raw
            is Int -> ans.toString()
            is Long -> ans.toString()
            is Double -> if (ans == ans.toLong().toDouble()) ans.toLong()
                .toString() else ans.toString()

            else -> ""
        }
        Timber.d("Text changed answers is $text")
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.setSelection(text.length)
            internalChange = false
        }

        // Restore children if saved repeat count exists
        /* val savedRepeatCount = text.toDoubleOrNull()?.toInt() ?: 0
         Timber.d("Saved Repeater Count One is  $savedRepeatCount")
         if (savedRepeatCount > 0) {
             Timber.d("Inside the Repeater count is ......")
             etAnswer.isCursorVisible = false
             populateChildren(q, savedRepeatCount, position + 1, focusClear = false)
         }*/
        // ✅ Always sync children on bind — even when count is 0, so cleared state actually clears.
        val savedRepeatCount = text.toDoubleOrNull()?.toInt() ?: 0
        Timber.d("Saved Repeater Count is $savedRepeatCount")
        etAnswer.isCursorVisible = savedRepeatCount == 0 && etAnswer.isFocused
        populateChildren(q, savedRepeatCount, position + 1, focusClear = false)


        // Parent focus listener
        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            etAnswer.isCursorVisible = hasFocus
            if (hasFocus) {
                containerChildQuestions.clearFocus()
            }
        }

        // TextWatcher for repeat count
        textWatcher = etAnswer.addTextChangedListener { editable ->
            if (internalChange) return@addTextChangedListener
            commitRunnable?.let { etAnswer.removeCallbacks(it) }

            val maxAllowed = item.base.question.repeaterTimes ?: 0
            if (editable.isNullOrEmpty()) {
                // Clear UI immediately for responsiveness
                binding.containerChildQuestions.removeAllViews()
                binding.containerChildQuestions.visibility = View.GONE
                commitRunnable = Runnable {
                    onAnswer(key, null)
                    clearStaleAnswers(q, 0, maxAllowed)
                    populateChildren(q, 0, position + 1, focusClear = false)
                }
            } else {
                // Non-empty → validate and submit
                val repeatCount = editable.toString().toIntOrNull() ?: 0
                if (repeatCount > maxAllowed) {
                    etAnswer.error =
                        etAnswer.context.getString(R.string.maximum_allowed_is, maxAllowed)
                    return@addTextChangedListener
                }
                commitRunnable = Runnable {
                    onAnswer(key, repeatCount)
                    clearStaleAnswers(q, repeatCount, maxAllowed)
                    populateChildren(q, repeatCount, position + 1, focusClear = true)
                }
            }

            // Debounce commit
            etAnswer.postDelayed(commitRunnable!!, 200)
        }
    }


    private fun populateChildren(
        q: UIQuestion,
        repeatCount: Int,
        childPosition: Int,
        focusClear: Boolean
    ) {
        binding.containerChildQuestions.showOrHide(repeatCount > 0)
        lastRepeatCount = repeatCount

        val children = if (repeatCount == 0) {
            emptyList()
        } else {
            (1..repeatCount).flatMap { repeatIndex ->
                repeaterChildrenMap[q.question.questionId].orEmpty().mapIndexed { childIndex, child ->
                    val childKey = AnswerKey(
                        child.base.question.questionId,
                        q.question.questionId,
                        repeatIndex
                    )
                    child.copy(
                        repeatIndex = repeatIndex,
                        repeaterId = q.question.questionId,
                        childIndex = childIndex + 1,
                        // ✅ Only per-index keyed answers. NO fallback to child.answer — the
                        //    template may carry a leaked value from another repeat instance.
                        answer = viewModel.answers.value?.get(childKey),
                        childMainQuestionPosition = childPosition
                    )
                }
            }
        }

        repeaterChildContainer.populateChildren(binding.containerChildQuestions, children)
        binding.containerChildQuestions.visibility = if (children.isNotEmpty()) View.VISIBLE else View.GONE
    }


    private fun clearStaleAnswers(q: UIQuestion, newCount: Int, maxCount: Int) {
        repeaterChildrenMap[q.question.questionId].orEmpty().forEach { child ->
            for (staleIndex in (newCount + 1)..maxCount) {
                val staleKey =
                    AnswerKey(child.base.question.questionId, q.question.questionId, staleIndex)
                signatureCache.remove(staleKey)
                onAnswer(staleKey, null)
            }
        }
    }


    private fun submitChildren(
        q: UIQuestion,
        repeatCount: Int,
        childPosition: Int,
        focusClear: Boolean
    ) {
        // Always honor an explicit clear (0); only short-circuit non-zero no-ops
        if (repeatCount != 0 && repeatCount == lastRepeatCount) return
        lastRepeatCount = repeatCount

        val children = (1..repeatCount).flatMap { repeatIndex ->
            repeaterChildrenMap[q.question.questionId].orEmpty().mapIndexed { childIndex, child ->
                val childKey = AnswerKey(
                    questionId = child.base.question.questionId,
                    repeaterId = q.question.questionId,
                    repeatIndex = repeatIndex
                )
                child.copy(
                    repeatIndex = repeatIndex,
                    repeaterId = q.question.questionId,
                    childIndex = childIndex + 1,
                    answer = signatureCache[childKey] ?: child.answer,
                    childMainQuestionPosition = childPosition
                )
            }
        }

        binding.rvChildQuestions.stopScroll()

        if (focusClear) {
            binding.etAnswer.clearFocus()
            val imm = binding.etAnswer.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etAnswer.windowToken, 0)
        }

        childAdapter.submitList(children)   // empty list when repeatCount == 0

        val targetVisibility = if (children.isNotEmpty()) View.VISIBLE else View.GONE
        if (binding.rvChildQuestions.visibility != targetVisibility) {
            binding.rvChildQuestions.visibility = targetVisibility
        }
    }
}

/* ─── Remaining VHs unchanged ─── */

private class FileUploadVH(
    private val binding: ItemQFileUploadBinding,
    private val onPickFile: (AnswerKey) -> Unit,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        txtQuestion.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(txtQuestion.context)}"
        txtQuestion.setQuestionWithAsterisk(q.question.isMandatory == true)
        val uriStr = item.answer as? String
        layFilePreview.isVisible = !uriStr.isNullOrEmpty()
        txtFileName.text = if (uriStr.isNullOrEmpty()) "No file"
        else getDisplayName(itemView.context.contentResolver, uriStr) ?: "File Selected"
        imgRemove.setOnClickListener {
            childRecyclerView?.stopScroll()
            onAnswer(AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex), null)
        }
        btnUpload.setOnClickListener {
            childRecyclerView?.stopScroll()
            onPickFile(AnswerKey(q.question.questionId))
        }
    }
}

private class SignatureVH(
    private val binding: ItemQSignatureCheckbocBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {

    private var signedListener: com.github.gcacace.signaturepad.views.SignaturePad.OnSignedListener? = null
    private var renderedSignatureKey: String? = null
    private var suppressListener = false   // ✅ NEW

    fun bind(item: UIQuestionUi, position: Int) = with(binding) {
        Timber.d("SIG_BIND fired qId=${item.base.question.questionId} sentinel=${item.answer}")
        signaturePad.isSaveEnabled = false
        signaturePad.isSaveFromParentEnabled = false
        val q = item.base
        txtQuestion.text = "${item.displayNumbering(position + 1)}), ${q.question.localizedText(txtQuestion.context)}"
        txtQuestion.setQuestionWithAsterisk(q.question.isMandatory == true)
        signedListener?.let { signaturePad.setOnSignedListener(null) }

        val savedBase64 = signatureCache[AnswerKey(q.question.questionId)]
            ?: (item.answer as? String)?.takeIf { it.isNotBlank() && it != "signed" }

        if (savedBase64 != renderedSignatureKey) {
            if (savedBase64 != null) {
                val bmp = savedBase64.base64ToBitmap()
                if (bmp != null) {
                    suppressListener = true                  // ✅ NEW: ignore library re-fires
                    signaturePad.post {
                        try {
                            signaturePad.signatureBitmap = bmp
                            signaturePad.setBackgroundResource(R.drawable.ic_signature_bg)
                        } catch (e: Exception) {
                            signaturePad.clear()
                        } finally {
                            signaturePad.post { suppressListener = false }  // ✅ release after layout settles
                        }
                    }
                } else signaturePad.clear()
            } else {
                signaturePad.clear()
            }
            renderedSignatureKey = savedBase64
        }

        txtClear.setOnClickListener {
            suppressListener = true                          // ✅ swallow the onClear re-entry
            signaturePad.clear()
            signaturePad.post { suppressListener = false }
            childRecyclerView?.stopScroll()
            renderedSignatureKey = null
            val key = AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex)
            signatureCache.remove(AnswerKey(q.question.questionId))
            onAnswer(key, null)
        }

        signedListener = object : com.github.gcacace.signaturepad.views.SignaturePad.OnSignedListener {
            override fun onStartSigning() {}
            override fun onSigned() {
                if (suppressListener) {
                    Timber.d("SIG_FLOW: onSigned SUPPRESSED (programmatic load)")
                    return
                }
                val b64 = signaturePad.context.bitmapToBase64(signaturePad.signatureBitmap)
                signatureCache[AnswerKey(q.question.questionId)] = b64
                renderedSignatureKey = b64
                val key = AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex)
                Timber.d("SIG_FLOW: onSigned firing qId=${q.question.questionId} key=$key")
                onAnswer(key, "signed")
                childRecyclerView?.stopScroll()
            }

            override fun onClear() {
                if (suppressListener) {
                    Timber.d("SIG_FLOW: onClear SUPPRESSED (programmatic)")
                    return
                }
                childRecyclerView?.stopScroll()
                renderedSignatureKey = null
                val key = AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex)
                signatureCache.remove(AnswerKey(q.question.questionId))
                onAnswer(key, null)
            }
        }
        signaturePad.setOnSignedListener(signedListener)
    }
}







class RadioVH(
    private val binding: ItemQRadioBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {

    private fun asLong(any: Any?): Long? = when (any) {
        is Long -> any
        is Int -> any.toLong()
        is String -> any.toLongOrNull()
        else -> null
    }

    init {
        // No nested children anymore — keep the child RV permanently hidden.
        binding.rvChildQuestions.visibility = View.GONE
    }

    fun bind(item: UIQuestionUi, position: Int) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)

        rgOptions.setOnCheckedChangeListener(null)
        rgOptions.removeAllViews()

        val selectedOptionId = asLong(item.answer)
        val idMap = mutableMapOf<Long, Int>()

        q.options.forEach { opt ->
            val btnId = View.generateViewId()
            idMap[opt.optionId] = btnId
            rgOptions.addView(RadioButton(itemView.context).apply {
                id = btnId
                text = opt.localizedLabel(tvTitle.context)
                tag = opt.optionId
            })
        }

        if (selectedOptionId != null) idMap[selectedOptionId]?.let { rgOptions.check(it) }
        else rgOptions.clearCheck()

        rgOptions.setOnCheckedChangeListener { group, checkedId ->
            childRecyclerView?.stopScroll()
            val key = AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex)

            if (checkedId == View.NO_ID) {
                onAnswer(key, null)
                return@setOnCheckedChangeListener
            }

            val optId = asLong(group.findViewById<RadioButton>(checkedId)?.tag)
                ?: return@setOnCheckedChangeListener
            onAnswer(key, optId)
        }
    }
}




/*class RadioVH(
    private val binding: ItemQRadioBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val onAnswerLive: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView? = null,
    private val viewModel: CrsQuestionaryViewModel
) : RecyclerView.ViewHolder(binding.root) {

    private val childAdapter =
        CrsDynamicQustionsAdapter(onAnswer, onAnswerLive = onAnswerLive, {}, {}, {},mutableMapOf(), viewModel = viewModel)

    init {
        // (binding.root as? ViewGroup)?.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        binding.rvChildQuestions.layoutManager = LinearLayoutManager(itemView.context)
        binding.rvChildQuestions.adapter = childAdapter
        binding.rvChildQuestions.isNestedScrollingEnabled = false
    }

    private fun asLong(any: Any?): Long? = when (any) {
        is Long -> any; is Int -> any.toLong(); is String -> any.toLongOrNull(); else -> null
    }

    *//** Evaluator decides; if not wired (null), fall back to legacy "Yes" reveal. *//*
    private fun shouldShowChildren(q: UIQuestion, answer: Any?): Boolean {
        conditionSatisfied(q.question.questionId, answer)?.let { return it }
        val yesId = q.options.firstOrNull { it.label.equals("Yes", ignoreCase = true) }?.optionId
        return asLong(answer) == yesId
    }

    private fun showChildren(children: List<UIQuestionUi>) {
        binding.rvChildQuestions.visibility = View.VISIBLE
        childAdapter.submitList(children)
    }

    private fun hideChildren(
        children: List<UIQuestionUi>,
        item: UIQuestionUi,
        clearAnswers: Boolean
    ) {
        binding.rvChildQuestions.visibility = View.GONE
        childAdapter.submitList(emptyList())
        if (clearAnswers) {
            // ✅ clear each CHILD's answer (the old code cleared the parent key repeatedly — a bug)
            children.forEach { child ->
                onAnswer(
                    AnswerKey(child.base.question.questionId, item.repeaterId, item.repeatIndex),
                    null
                )
            }
        }
    }

    fun bind(item: UIQuestionUi, position: Int) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        rgOptions.setOnCheckedChangeListener(null)
        rgOptions.removeAllViews()

        val selectedOptionId = asLong(item.answer)
        val idMap = mutableMapOf<Long, Int>()
        q.options.forEach { opt ->
            val btnId = View.generateViewId()
            idMap[opt.optionId] = btnId
            rgOptions.addView(RadioButton(itemView.context).apply {
                id = btnId
                text = opt.localizedLabel(tvTitle.context)
                tag = opt.optionId
            })
        }

        if (selectedOptionId != null) idMap[selectedOptionId]?.let { rgOptions.check(it) }
        else rgOptions.clearCheck()

        val children = conditionalChildrenMap[q.question.questionId] ?: emptyList()

        // Initial render — don't clear answers on (re)bind
        if (shouldShowChildren(q, selectedOptionId)) showChildren(children)
        else hideChildren(children, item, clearAnswers = false)

        rgOptions.setOnCheckedChangeListener { group, checkedId ->
            childRecyclerView?.stopScroll()
            if (checkedId == View.NO_ID) {
                onAnswer(AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex), null)
                hideChildren(children, item, clearAnswers = true)
                return@setOnCheckedChangeListener
            }
            val optId = asLong(group.findViewById<RadioButton>(checkedId)?.tag)
                ?: return@setOnCheckedChangeListener
            onAnswer(AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex), optId)

            // User actively changed selection → clear children when hiding
            if (shouldShowChildren(q, optId)) showChildren(children)
            else hideChildren(children, item, clearAnswers = true)
        }
    }
}*/

private class PhotoCheckboxVH(
    private val binding: ItemQPhotoCheckboxMainBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        txtQuestion.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(txtQuestion.context)}"
        txtQuestion.setQuestionWithAsterisk(q.question.isMandatory == true)
        val selected =
            ((item.answer as? List<*>)?.filterIsInstance<Long>() ?: emptyList()).toMutableSet()
        mPhotosRecyclerView.setUpAdapter(
            q.options.toMutableList(),
            R.layout.item_q_photo_checkbox,
            ItemQPhotoCheckboxBinding::inflate,
            { opt: CrsOptionEntity, _, bin ->
                bin.optionCheckbox.setOnCheckedChangeListener(null)
                bin.optionCheckbox.isChecked = selected.contains(opt.optionId)
                bin.optionCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    childRecyclerView?.stopScroll()
                    if (isChecked) selected += opt.optionId else selected -= opt.optionId
                    onAnswer(
                        AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                        selected.toList()
                    )
                }
                val imageUri = if (CrsApi.isImageFile(opt.label)) opt.label else null
                if (!imageUri.isNullOrEmpty()) {
                    Glide.with(bin.optionImage.context).load(imageUri).into(bin.optionImage)
                    bin.optionImage.isVisible = true
                    bin.optionCheckbox.text = ""
                } else {
                    bin.optionImage.isVisible = false
                    bin.optionCheckbox.text = opt.label
                }
            })
    }
}

private class PhotoUploadVH(
    private val binding: ItemQPhotoUploadBinding,
    private val onPickPhoto: (AnswerKey) -> Unit,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        val uriStr = item.answer as? String
        ivPreview.isVisible = !uriStr.isNullOrEmpty()
        btnClearPhoto.isVisible = !uriStr.isNullOrEmpty()
        btnClearPhoto.setOnClickListener {
            childRecyclerView?.stopScroll()
            onAnswer(AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex), null)
        }
        if (!uriStr.isNullOrEmpty()) Glide.with(ivPreview.context).load(uriStr).into(ivPreview)
        btnUpload.setOnClickListener {
            childRecyclerView?.stopScroll()
            onPickPhoto(AnswerKey(q.question.questionId))
        }
    }
}

class LocationVH(
    private val binding: ItemsQLocationCaptureBinding,
    private val onCaptureLocation: (AnswerKey) -> Unit,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        val locText = item.answer as? String
        tvLocationStatus.text = locText ?: "Tap to capture location"
        tvLocationStatus.isVisible = !locText.isNullOrEmpty()
        btnCaptureLocation.setOnClickListener {
            childRecyclerView?.stopScroll()
            onCaptureLocation(AnswerKey(q.question.questionId))
        }
    }
}

class DropdownVH(
    private val binding: ItemQDropdownBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        val optionLabels = q.options.map { it.localizedLabel(tvTitle.context) ?: "" }
        val optionIds = q.options.map { it.optionId }
        val labels = listOf("---Select---") + optionLabels
        val adapter = object :
            ArrayAdapter<String>(itemView.context, android.R.layout.simple_spinner_item, labels) {
            override fun isEnabled(position: Int) = true
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val v = super.getDropDownView(position, convertView, parent) as TextView
                v.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (position == 0) android.R.color.darker_gray else android.R.color.black
                    )
                )
                return v
            }
        }.also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinner.adapter = adapter

        val selectedId = item.answer as? Long
        val initPos = if (selectedId != null) {
            val idx = optionIds.indexOf(selectedId)
            if (idx >= 0) idx + 1 else 0
        } else 0
        if (initPos in labels.indices) spinner.setSelection(initPos, false)

        var userChange = false
        spinner.setOnTouchListener { _, _ -> userChange = true; false }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                if (!userChange) return
                childRecyclerView?.stopScroll()
                val chosenId = if (pos == 0) null else optionIds[pos - 1]
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    chosenId
                )
                userChange = false
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}

class DatePickerVH(
    private val binding: ItemQDatepickerBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        val current = item.answer as? String
        tvSelectedDate.text = current ?: ""
        tvSelectedDate.isVisible = !current.isNullOrEmpty()
        btnPickDate.setOnClickListener {
            childRecyclerView?.stopScroll()
            val c = Calendar.getInstance()
            DatePickerDialog(itemView.context, { _, y, m, d ->
                val date = "%04d-%02d-%02d".format(y, m + 1, d)
                tvSelectedDate.text = date
                tvSelectedDate.isVisible = true
                onAnswer(AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex), date)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}

private class CheckboxVH(
    private val binding: ItemQCheckboxBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text =
            "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        llOptionsContainer.removeAllViews()
        val selected =
            ((item.answer as? List<*>)?.filterIsInstance<Long>() ?: emptyList()).toMutableSet()
        q.options.forEach { opt ->
            val cb = CheckBox(itemView.context).apply {
                text = opt.localizedLabel(tvTitle.context)
                tag = opt.optionId
                isChecked = selected.contains(opt.optionId)
            }
            cb.setOnCheckedChangeListener { _, checked ->
                childRecyclerView?.stopScroll()
                if (checked) selected += opt.optionId else selected -= opt.optionId
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    selected.toList()
                )
            }
            llOptionsContainer.addView(cb)
        }
    }
}

fun getDisplayName(resolver: ContentResolver, uriString: String): String? {
    return try {
        val uri = uriString.toUri()
        when (uri.scheme?.lowercase()) {
            "content" -> resolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { c ->
                if (c.moveToFirst()) c.getString(
                    c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }
                        ?: return null)
                else null
            }

            "file" -> File(uri.path ?: "").name
            else -> uri.lastPathSegment ?: uriString
        }
    } catch (e: Exception) {
        Timber.e("Error getting file name: ${e.message}")
        null
    }
}