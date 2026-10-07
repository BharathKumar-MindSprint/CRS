// adapter/CrsDynamicQustionsAdapterOld.kt
package com.olam.digital.crs.adapter

import android.app.DatePickerDialog
import android.content.ContentResolver
import android.provider.OpenableColumns
import android.text.InputFilter
import android.text.InputType  //fullquestuion -> 4 question types not question
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
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
import com.olam.digital.crs.extension.base64ToBitmap
import com.olam.digital.crs.extension.bitmapToBase64
import com.olam.digital.crs.extension.clearFocusAndHideKeyboard
import com.olam.digital.crs.extension.displayNumbering
import com.olam.digital.crs.extension.localizedLabel
import com.olam.digital.crs.extension.localizedText
import com.olam.digital.crs.extension.setDecimalWithCharacterLimit
import com.olam.digital.crs.extension.setQuestionWithAsterisk
import com.olam.digital.crs.model.AnswerKey
import com.olam.digital.crs.model.UIQuestion
import com.olam.digital.crs.viewmodel.api.CrsApi

class CrsDynamicQustionsAdapterOld(
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val onPickPhoto: (AnswerKey) -> Unit,
    private val onPickFile: (AnswerKey) -> Unit,
    private val onCaptureLocation: (AnswerKey) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private var childRecyclerView: RecyclerView?=null
) : ListAdapter<UIQuestionUi, RecyclerView.ViewHolder>(Diff) {

    init {
        setHasStableIds(true)
        childRecyclerView = childRecyclerView ?: null
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        this.childRecyclerView = recyclerView
    }

    fun getRecyclerView(): RecyclerView? {
        return childRecyclerView
    }

    /*override fun getItemId(position: Int): Long {
        val item = getItem(position)
        val qId = item.base.question.questionId
        val repeatIndex = item.repeatIndex ?: 0
        // Combine into a unique long
        return qId * 1000L + repeatIndex
    }*/

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        val qId = item.base.question.questionId
        val repeatIndex = item.repeatIndex ?: 0
        val repeaterId = item.repeaterId ?: 0
        val childIndex = item.childIndex ?: 0

        // ✅ Combine everything into a unique string, then hash
        return "$qId-$repeaterId-$repeatIndex-$childIndex".hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
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
        Timber.d("ViewType for viewType=$viewType is ${ViewType.entries[viewType]}")
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
                ), onCaptureLocation,
                childRecyclerView
            )

            ViewType.PHOTO_CHECKBOX -> PhotoCheckboxVH(
                ItemQPhotoCheckboxMainBinding.inflate(
                    inf,
                    parent,
                    false
                ), onAnswer,
                childRecyclerView
            )

            ViewType.PHOTO_UPLOAD -> PhotoUploadVH(
                ItemQPhotoUploadBinding.inflate(
                    inf,
                    parent,
                    false
                ), onPickPhoto,
                onAnswer,
                childRecyclerView
            )

            ViewType.RADIO -> RadioVH(
                ItemQRadioBinding.inflate(inf, parent, false),
                onAnswer,
                conditionalChildrenMap,
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
                ),
                signatureCache,
                childRecyclerView
            )
            ViewType.TEXTBOX -> TextboxVH(
                ItemQEdittextBinding.inflate(inf, parent, false),
                onAnswer,
                signatureCache,
                childRecyclerView
            )

            ViewType.DECIMAL ->
                TextboxDecimalVH(
                    ItemQEdittextBinding.inflate(inf, parent, false),
                    onAnswer,
                    signatureCache,
                    childRecyclerView
                )

            ViewType.NUMBERIC -> {
                TextboxNumericVH(
                    ItemQEdittextBinding.inflate(inf, parent, false),
                    onAnswer,
                    signatureCache,
                    childRecyclerView
                )
            }

            ViewType.REPEATER ->
                TextboxRepeaterNumericVH(
                    ItemQEdittextBinding.inflate(inf, parent, false),
                    onAnswer,
                    signatureCache,
                    {
                        CrsDynamicQustionsAdapterOld(
                            onAnswer,
                            onPickPhoto,
                            onPickFile,
                            onCaptureLocation,
                            signatureCache
                        )
                    },
                    repeaterChildrenMap
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

    private val conditionalChildrenMap = mutableMapOf<Long, List<UIQuestionUi>>()

    fun setConditionalChildrenMap(map: Map<Long, List<UIQuestionUi>>) {
        conditionalChildrenMap.clear()
        conditionalChildrenMap.putAll(map)
    }


    private val repeaterChildrenMap = mutableMapOf<Long, List<UIQuestionUi>>()

    fun setRepeaterChildrenMap(map: Map<Long, List<UIQuestionUi>>) {
        repeaterChildrenMap.clear()
        repeaterChildrenMap.putAll(map)
    }

    object Diff : DiffUtil.ItemCallback<UIQuestionUi>() {
        override fun areItemsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi): Boolean {
            return oldItem.base.question.questionId == newItem.base.question.questionId &&
                    oldItem.repeaterId == newItem.repeaterId &&
                    oldItem.repeatIndex == newItem.repeatIndex &&
                    oldItem.childIndex == newItem.childIndex
        }

        override fun areContentsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi): Boolean {
            // ❌ AVOID: oldItem.base.question == newItem.base.question
            // This fails if the underlying model reference shifts during your map functions.

            return oldItem.base.type == newItem.base.type &&
                    oldItem.childMainQuestionPosition == newItem.childMainQuestionPosition &&
                    answersEqual(oldItem.answer, newItem.answer)
        }

        private fun answersEqual(a: Any?, b: Any?): Boolean {
            return if (a is List<*> && b is List<*>) {
                a == b
            } else {
                a == b
            }
        }
    }

    /*object Diff : DiffUtil.ItemCallback<UIQuestionUi>() {
        override fun areItemsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi): Boolean {
            return oldItem.base.question.questionId == newItem.base.question.questionId &&
                    oldItem.repeaterId == newItem.repeaterId &&
                    oldItem.repeatIndex == newItem.repeatIndex &&
                    oldItem.childIndex == newItem.childIndex
        }
        override fun areContentsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi): Boolean {
            // Compare only fields that affect UI
            return oldItem.base.type == newItem.base.type &&
                    oldItem.base.question == newItem.base.question &&
                    oldItem.base.options == newItem.base.options &&
                    answersEqual(oldItem.answer, newItem.answer)
        }

        private fun answersEqual(a: Any?, b: Any?) =
            if (a is List<*> && b is List<*>) a == b else a == b
    }*/
}

/* ------------------------ VHs ------------------------ */

private class FileUploadVH(
    private val binding: ItemQFileUploadBinding,
    private val onPickFile: (AnswerKey) -> Unit,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base

        txtQuestion.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(txtQuestion.context)}"

        txtQuestion.setQuestionWithAsterisk(q.question.isMandatory == true)

        val uriStr = item.answer as? String
        layFilePreview.isVisible = !uriStr.isNullOrEmpty()
        txtFileName.text = when {
            (uriStr.isNullOrEmpty()) -> {
                "No file"
            }

            else -> {
                val name = getDisplayName(itemView.context.contentResolver, uriStr)
                name ?: "File Selected"

            }
        }
        imgRemove.setOnClickListener {
            childRecyclerView?.stopScroll()
            //remove file and save answer as null
            onAnswer(AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex), null)
        }
        btnUpload.setOnClickListener {
            childRecyclerView?.stopScroll()
            onPickFile(AnswerKey(q.question.questionId)) }
    }
}


private class SignatureVH(
    private val binding: ItemQSignatureCheckbocBinding,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {

    private var signedListener: com.github.gcacace.signaturepad.views.SignaturePad.OnSignedListener? =
        null

    fun bind(item: UIQuestionUi, position: Int) = with(binding) {
        signaturePad.isSaveEnabled = false
        signaturePad.isSaveFromParentEnabled = false
        val q = item.base
        txtQuestion.text = "${position + 1}), ${q.question.localizedText(txtQuestion.context)}"
        txtQuestion.setQuestionWithAsterisk(q.question.isMandatory == true)
        // Detach old listener to avoid stacking across recycled holders
        signedListener?.let { signaturePad.setOnSignedListener(null) }

        val savedBase64 = signatureCache[AnswerKey(q.question.questionId)]
            ?: (item.answer as? String)?.takeIf { it.isNotBlank() }
        Timber.d("Saved base64 len=${savedBase64?.length}, preview=${savedBase64?.take(30)}...")

        if (savedBase64 != null) {
            val bmp = savedBase64.base64ToBitmap()
            if (bmp != null) {
                // Post ensures pad has width/height
                signaturePad.post {
                    try {
                        signaturePad.setSignatureBitmap(bmp)
                        // Optional: if you use a specific background
                        signaturePad.setBackgroundResource(R.drawable.ic_signature_bg)
                        Timber.d("Signature restored: ${bmp.width}x${bmp.height}")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to set signature bitmap")
                        signaturePad.clear()
                    }
                }
            } else {
                signaturePad.clear()
            }
        } else {
            signaturePad.clear()
        }

        txtClear.setOnClickListener {
            signaturePad.clear()
            childRecyclerView?.stopScroll()
            signatureCache.remove(AnswerKey(q.question.questionId))
        }

        // Single listener instance per bind
        signedListener =
            object : com.github.gcacace.signaturepad.views.SignaturePad.OnSignedListener {
                override fun onStartSigning() {}

                override fun onSigned() {
                    val bmp = signaturePad.signatureBitmap
                    val b64 = signaturePad.context.bitmapToBase64(bmp)
                    signatureCache[AnswerKey(q.question.questionId)] = b64
                    childRecyclerView?.stopScroll()
                    Timber.d("Cached signature for ${q.question.questionId}, len=${b64.length}")
                }

                override fun onClear() {
                    childRecyclerView?.stopScroll()
                    signatureCache.remove(AnswerKey(q.question.questionId))
                }
            }
        signaturePad.setOnSignedListener(signedListener)
    }
}

class DecimalDigitsInputFilter(
    private val digitsBeforeZero: Int,
    private val digitsAfterZero: Int
) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val newText = dest?.replaceRange(dstart, dend, source?.subSequence(start, end) ?: "")

        // Count only digits, ignore '.' in character count
        val digitCount = newText?.count { it.isDigit() } ?: 0
        if (digitCount > digitsBeforeZero + digitsAfterZero) return ""

        // Allow empty string or trailing decimal point
        if (newText.isNullOrEmpty() || newText.endsWith(".")) return null

        // Validate format: digits before and after decimal
        val pattern = Regex("^[0-9]{0,$digitsBeforeZero}(\\.[0-9]{0,$digitsAfterZero})?$")
        return if (pattern.matches(newText)) null else ""
    }
}


private class TextboxVH(
    private val binding: ItemQEdittextBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {

    private var internalChange = false

    fun bind(item: UIQuestionUi, position1: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        etAnswer.hint = q.question.helpText ?: ""
        Timber.d("Binding Textbox: questionId=${q.question.questionId}, answer=${item.answer}, isLast=$isLast")
        val text = item.answer as? String ?: ""
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.text?.let { editable ->
                val safeIndex = text.length.coerceAtMost(editable.length)
                etAnswer.setSelection(safeIndex)
            }
            internalChange = false
        }
        Timber.d("Is it Last item? $isLast")

        //setting character limit
        /*val filters = mutableListOf<InputFilter>()
        q.question.characterLimit?.let { filters.add(InputFilter.LengthFilter(it.toInt())) }
        q.question.decimalLimit?.let { filters.add(DecimalDigitsInputFilter(10, it.toInt())) }
        etAnswer.filters = filters.toTypedArray()*/


        etAnswer.addTextChangedListener {
            Timber.d("Input changed in Textbox: ${it.toString()}")
            if (isLast) {
                signatureCache[AnswerKey(q.question.questionId)] = it.toString()
            }
        }
        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            Timber.d("Focus changed: hasFocus=$hasFocus, text=${etAnswer.text}")
            if (!hasFocus && !etAnswer.text.toString().isNullOrEmpty()) {
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    etAnswer.text.toString()
                )
                //remove from signature cache when focus is lost and answer is empty
                signatureCache.remove(AnswerKey(q.question.questionId))
            }

            if (hasFocus && childRecyclerView!=null)
            {
                childRecyclerView.stopScroll()
            }

        }
    }
}


//numberic
private class TextboxRepeaterNumericVH(
    private val binding: ItemQEdittextBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childAdapterFactory: () -> CrsDynamicQustionsAdapterOld,
    private val repeaterChildrenMap: MutableMap<Long, List<UIQuestionUi>>
) : RecyclerView.ViewHolder(binding.root) {

    private val childAdapter = childAdapterFactory()
    private var internalChange = false

    init {
        binding.rvChildQuestions.apply {
            adapter = childAdapter
            isNestedScrollingEnabled = false
            // layoutManager set via app:layoutManager in XML
        }
        childAdapter.setRecyclerView(binding.rvChildQuestions)
        childAdapter.submitList(emptyList())
        (binding.root as? ViewGroup)?.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
    }

    fun bind(item: UIQuestionUi, position: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        val key = AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex)

        tvTitle.text = "${item.displayNumbering(position + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)

        val text = when (val ans = item.answer) {
            is String -> ans
            is Int -> ans.toString()
            else -> ""
        }

        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.setSelection(text.length)
            internalChange = false
        }

        etAnswer.inputType = InputType.TYPE_CLASS_NUMBER

        // Hydrate children on reopen if saved repeat count exists
        val savedRepeatCount = text.toIntOrNull() ?: 0
        if (savedRepeatCount > 0 && childAdapter.currentList.isEmpty()) {
            submitChildren(q, savedRepeatCount, position + 1, focusClear = false)
        }

        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && rvChildQuestions.isVisible) {
                rvChildQuestions.stopScroll()
            }
        }

        // Remove any previously added listener to avoid stacking on rebind
        etAnswer.addTextChangedListener { editable ->
            if (internalChange) return@addTextChangedListener

            if (editable.isNullOrEmpty()) {
                onAnswer(key, null)
                submitChildren(q, 0, position + 1, focusClear = false)
                return@addTextChangedListener
            }

            val repeatCount = editable.toString().toIntOrNull() ?: 0
            val maxAllowed = item.base.question.repeaterTimes ?: 0

            if (repeatCount > maxAllowed) {
                etAnswer.error = etAnswer.context.getString(R.string.maximum_allowed_is, maxAllowed)
                return@addTextChangedListener
            }

            onAnswer(key, repeatCount)
            clearStaleAnswers(q, repeatCount, maxAllowed)
            // ✅ focusClear = true only when the parent repeater count field itself is being typed
            submitChildren(q, repeatCount, position + 1, focusClear = true)
        }
    }

    private fun clearStaleAnswers(q: UIQuestion, newCount: Int, maxCount: Int) {
        repeaterChildrenMap[q.question.questionId].orEmpty().forEach { child ->
            for (staleIndex in (newCount + 1)..maxCount) {
                val staleKey = AnswerKey(child.base.question.questionId, q.question.questionId, staleIndex)
                signatureCache.remove(staleKey)
                onAnswer(staleKey, null)
            }
        }
    }

    private fun submitChildren(
        q: UIQuestion,
        repeatCount: Int,
        childPosition: Int,
        focusClear: Boolean   // ✅ only clear focus when parent field triggered this
    ) {
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

        // ✅ Only clear focus when the parent repeater count field triggered submitChildren.
        //    Never clear focus when restoring saved state (reopen) — that would steal
        //    focus away from child EditTexts the user is actively typing in.
        if (focusClear) {
            binding.etAnswer.clearFocusAndHideKeyboard()
        }

        childAdapter.submitList(children)
        binding.rvChildQuestions.visibility = if (children.isNotEmpty()) View.VISIBLE else View.GONE
    }
}

/*private class TextboxNumericVH(
    private val binding: ItemQEdittextBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {

    private var internalChange = false

    fun bind(item: UIQuestionUi, position1: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        etAnswer.hint = q.question.helpText ?: ""

        val text = item.answer as? String ?: ""
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.text?.let { editable ->
                val safeIndex = text.length.coerceAtMost(editable.length)
                etAnswer.setSelection(safeIndex)
            }
            internalChange = false
        }
        Timber.d("Is it Last item? $isLast")

        Timber.d("Character limit ${q.question.characterLimit}")
        etAnswer.inputType = InputType.TYPE_CLASS_NUMBER

        val filters = mutableListOf<InputFilter>()
        q.question.characterLimit?.let { filters.add(InputFilter.LengthFilter(it.toInt())) }
        // q.question.decimalLimit?.let { filters.add(DecimalDigitsInputFilter(10, it.toInt())) }
        etAnswer.filters = filters.toTypedArray()

        etAnswer.addTextChangedListener {
            if (isLast) {
                signatureCache[AnswerKey(item.base.question.questionId)] = it.toString()
            }
        }


        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            Timber.d("Focus changed Numeric: hasFocus=$hasFocus, text=${etAnswer.text}")
            if (!hasFocus && !etAnswer.text.toString().isNullOrEmpty()) {
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    etAnswer.text.toString()
                )
                //remove from signature cache when focus is lost and answer is empty
                signatureCache.remove(AnswerKey(q.question.questionId))
            }

            if (hasFocus && childRecyclerView!=null)
            {
                childRecyclerView.stopScroll()
            }

        }
    }
}*/





//numberic
/*private class TextboxDecimalVH(
    private val binding: ItemQEdittextBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root)
{

    private var internalChange = false

    fun bind(item: UIQuestionUi, position1: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        etAnswer.hint = q.question.helpText ?: ""

        val text = item.answer as? String ?: ""
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.text?.let { editable ->
                val safeIndex = text.length.coerceAtMost(editable.length)
                etAnswer.setSelection(safeIndex)
            }
            internalChange = false
        }





        Timber.d("Is it Last item? $isLast")
        Timber.d("Character limit ${q.question.characterLimit}")
        etAnswer.inputType = InputType.TYPE_CLASS_NUMBER

        *//*  val filters = mutableListOf<InputFilter>()
          q.question.characterLimit?.let { filters.add(InputFilter.LengthFilter(it.toInt())) }
          q.question.decimalLimit?.let { filters.add(DecimalDigitsInputFilter( (q.question.characterLimit?:10).toInt(), it.toInt())) }
          etAnswer.filters = filters.toTypedArray()*//*
        etAnswer.setDecimalWithCharacterLimit(
            maxBefore = q.question.characterLimit?.toInt() ?: 5,
            maxAfter = q.question.decimalLimit?.toInt() ?: 2
        )

        etAnswer.addTextChangedListener {
            if (isLast) {
                signatureCache[AnswerKey(q.question.questionId)] = it.toString()
            }
        }


        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            Timber.d("Focus changed Decimal: hasFocus=$hasFocus, text=${etAnswer.text}")
            if (!hasFocus && !etAnswer.text.toString().isNullOrEmpty()) {
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    etAnswer.text.toString()
                )
                //remove from signature cache when focus is lost and answer is empty
                signatureCache.remove(AnswerKey(q.question.questionId))
            }

            if (hasFocus && childRecyclerView!=null)
            {
                childRecyclerView.stopScroll()
            }
        }
    }
}*/


private class TextboxNumericVH(
    private val binding: ItemQEdittextBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {

    private var internalChange = false
    private var textWatcher: android.text.TextWatcher? = null

    fun bind(item: UIQuestionUi, position1: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        etAnswer.hint = q.question.helpText ?: ""

        // ✅ 1. Set inputType FIRST — before setText — so the IME connection
        //       is established correctly and doesn't get reset after text is placed
        etAnswer.inputType = InputType.TYPE_CLASS_NUMBER

        // ✅ 2. Apply filters
        val filters = mutableListOf<InputFilter>()
        q.question.characterLimit?.let { filters.add(InputFilter.LengthFilter(it.toInt())) }
        etAnswer.filters = filters.toTypedArray()

        // ✅ 3. Now set text — IME is already configured, won't drop keystrokes
        val text = item.answer as? String ?: ""
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.text?.let { editable ->
                etAnswer.setSelection(text.length.coerceAtMost(editable.length))
            }
            internalChange = false
        }

        // ✅ 4. Remove stale watcher before attaching fresh one
        textWatcher?.let { etAnswer.removeTextChangedListener(it) }
        textWatcher = etAnswer.addTextChangedListener {
            if (internalChange) return@addTextChangedListener
            if (isLast) {
                signatureCache[AnswerKey(q.question.questionId)] = it.toString()
            }
        }
        etAnswer.setOnKeyListener { _, keyCode, event ->
            Timber.d("KEY EVENT: keyCode=$keyCode action=${event.action}")
            false // don't consume
        }

        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !etAnswer.text.isNullOrEmpty()) {
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    etAnswer.text.toString()
                )
                signatureCache.remove(AnswerKey(q.question.questionId))
            }
            if (hasFocus) {
                childRecyclerView?.stopScroll()
            }
        }
    }
}


// Same fix applies to TextboxDecimalVH
private class TextboxDecimalVH(
    private val binding: ItemQEdittextBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val signatureCache: MutableMap<AnswerKey, String>,
    private val childRecyclerView: RecyclerView? = null
) : RecyclerView.ViewHolder(binding.root) {

    private var internalChange = false
    private var textWatcher: android.text.TextWatcher? = null

    fun bind(item: UIQuestionUi, position1: Int, isLast: Boolean) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1 + 1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        etAnswer.hint = q.question.helpText ?: ""

        // ✅ 1. inputType and filters BEFORE setText
        etAnswer.inputType = InputType.TYPE_CLASS_NUMBER
        etAnswer.setDecimalWithCharacterLimit(
            maxBefore = q.question.characterLimit?.toInt() ?: 5,
            maxAfter = q.question.decimalLimit?.toInt() ?: 2
        )

        etAnswer.setOnKeyListener { _, keyCode, event ->
            Timber.d("KEY EVENT: keyCode=$keyCode action=${event.action}")
            false // don't consume
        }

        // ✅ 2. Then setText
        val text = item.answer as? String ?: ""
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.text?.let { editable ->
                etAnswer.setSelection(text.length.coerceAtMost(editable.length))
            }
            internalChange = false
        }

        textWatcher?.let { etAnswer.removeTextChangedListener(it) }
        textWatcher = etAnswer.addTextChangedListener {
            if (internalChange) return@addTextChangedListener
            if (isLast) {
                signatureCache[AnswerKey(q.question.questionId)] = it.toString()
            }
        }

        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !etAnswer.text.isNullOrEmpty()) {
                onAnswer(
                    AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                    etAnswer.text.toString()
                )
                signatureCache.remove(AnswerKey(q.question.questionId))
            }
            if (hasFocus) {
                childRecyclerView?.stopScroll()
            }
        }
    }
}




















private class RadioVH(
    private val binding: ItemQRadioBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val conditionalChildrenMap: MutableMap<Long, List<UIQuestionUi>>,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {

    private val childAdapter = CrsDynamicQustionsAdapterOld(onAnswer, {}, {}, {}, mutableMapOf())


    init {
        // Lock layout down so radio checking doesn't snap the layout view boundaries
        (binding.root as? ViewGroup)?.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        binding.rvChildQuestions.layoutManager = LinearLayoutManager(itemView.context)
        binding.rvChildQuestions.adapter = childAdapter
        binding.rvChildQuestions.isNestedScrollingEnabled = false
    }
    private fun asLong(any: Any?): Long? = when (any) {
        is Long -> any
        is Int -> any.toLong()
        is String -> any.toLongOrNull()
        else -> null
    }

    fun bind(item: UIQuestionUi, position: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position+1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        // Reset group before binding
        rgOptions.setOnCheckedChangeListener(null)
        rgOptions.removeAllViews()

        val selectedOptionId = asLong(item.answer)

        // Build radio buttons
        val idMap = mutableMapOf<Long, Int>()
        q.options.forEach { opt ->
            val btnId = View.generateViewId()
            idMap[opt.optionId] = btnId
            val rb = RadioButton(itemView.context).apply {
                id = btnId
                text = opt.localizedLabel(tvTitle.context)
                tag = opt.optionId
            }
            rgOptions.addView(rb)
        }

        // Restore checked state
        if (selectedOptionId != null) {
            idMap[selectedOptionId]?.let { rgOptions.check(it) }
        } else {
            rgOptions.clearCheck()
        }

        val yesID = q.options.firstOrNull {
            it.label.equals("Yes", ignoreCase = true)
        }?.optionId

        val children = conditionalChildrenMap[q.question.questionId] ?: emptyList()

        // Reset child visibility on bind
        if (selectedOptionId == yesID) {
            rvChildQuestions.visibility = View.VISIBLE
            childAdapter.submitList(children)
        } else {
            rvChildQuestions.visibility = View.GONE
            childAdapter.submitList(emptyList())
        }

        // Listener for changes
        rgOptions.setOnCheckedChangeListener { group, checkedId ->
            childRecyclerView?.stopScroll()
            if (checkedId == View.NO_ID) {
                onAnswer(AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex), null)
                rvChildQuestions.visibility = View.GONE
                childAdapter.submitList(emptyList())
                return@setOnCheckedChangeListener
            }

            val selected = group.findViewById<RadioButton>(checkedId)
            val optId = asLong(selected?.tag)

            if (optId != null) {
                onAnswer(AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex), optId)

                if (optId == yesID) {
                    rvChildQuestions.visibility = View.VISIBLE
                    childAdapter.submitList(children)
                } else {
                    rvChildQuestions.visibility = View.GONE
                    childAdapter.submitList(emptyList())
                    children.forEach { child ->
                        onAnswer(
                            AnswerKey(
                                q.question.questionId,
                                item.repeaterId,
                                item.repeatIndex
                            ), null
                        )
                    }
                }
            }
        }

        /*rvChildQuestions.layoutManager = LinearLayoutManager(itemView.context)
        rvChildQuestions.adapter = childAdapter
        rvChildQuestions.isNestedScrollingEnabled = false*/
    }
}


private class PhotoCheckboxVH(
    private val binding: ItemQPhotoCheckboxMainBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        txtQuestion.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(txtQuestion.context)}"
        txtQuestion.setQuestionWithAsterisk(q.question.isMandatory == true)
        // Selected options from answer
        val selected =
            ((item.answer as? List<*>)?.filterIsInstance<Long>() ?: emptyList()).toMutableSet()

        // Uses your setUpAdapter extension
        mPhotosRecyclerView.setUpAdapter(
            q.options.toMutableList(),
            com.olam.digital.crs.R.layout.item_q_photo_checkbox,
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

                // IMPORTANT: Bind the image from the *correct* field.
                val imageUri = imageUriOf(opt) // implement below to map to your entity
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

    private fun imageUriOf(opt: CrsOptionEntity): String? {
        // TODO: Map to your entity's field that holds the image URI.
        // Example candidates: opt.imageUri, opt.value, opt.mediaUrl, etc.
        // return opt.imageUri ?: opt.value
        return if (CrsApi.isImageFile(opt.label)) opt.label else null
    }
}

private class PhotoUploadVH(
    private val binding: ItemQPhotoUploadBinding,
    private val onPickPhoto: (AnswerKey) -> Unit,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(tvTitle.context)}"
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
            onPickPhoto(AnswerKey(q.question.questionId)) }
    }
}

private class LocationVH(
    private val binding: ItemsQLocationCaptureBinding,
    private val onCaptureLocation: (AnswerKey) -> Unit,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        val locText = item.answer as? String
        tvLocationStatus.text = locText ?: "Tap to capture location"
        tvLocationStatus.isVisible = !locText.isNullOrEmpty()
        //btnClearLocation.isVisible = !locText.isNullOrEmpty()
        btnCaptureLocation.setOnClickListener {
            childRecyclerView?.stopScroll()
            onCaptureLocation(AnswerKey(q.question.questionId)) }
    }
}

private class DropdownVH(
    private val binding: ItemQDropdownBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        // Original option data
        val optionLabels = q.options.map { it.localizedLabel(tvTitle.context) ?: "" }
        val optionIds = q.options.map { it.optionId } // List<Long>

        // Build lists with a placeholder at position 0
        val placeholderLabel = "---Select---"
        val labels = listOf(placeholderLabel) + optionLabels
        val ids = listOf<Long?>(null) + optionIds // keep index alignment; index 0 = placeholder

        // Adapter that disables the placeholder row in the dropdown
        val adapter = object : ArrayAdapter<String>(
            itemView.context,
            android.R.layout.simple_spinner_item,
            labels
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item (placeholder)
                return true
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val v = super.getDropDownView(position, convertView, parent) as TextView
                // Gray out the placeholder
                if (position == 0) {
                    v.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                } else {
                    v.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                }
                return v
            }
        }.also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter

        // Restore selection:
        val selectedId = item.answer as? Long
        val initPos = if (selectedId != null) {
            val idx = optionIds.indexOf(selectedId)
            if (idx >= 0) idx + 1 else 0   // +1 because of placeholder
        } else {
            0
        }
        if (initPos in labels.indices) spinner.setSelection(initPos, false)

        // Only fire on user selection (not initial binding)
        var userChange = false
        spinner.setOnTouchListener { _, _ ->
            userChange = true
            false
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                if (!userChange) return
                childRecyclerView?.stopScroll()
                // Ignore placeholder selection
                if (pos == 0) {
                    onAnswer(
                        AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                        null
                    )  // or skip calling if you prefer
                } else {
                    // Map Spinner position back to your real option ID
                    val chosenId = optionIds[pos - 1]
                    onAnswer(
                        AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                        chosenId
                    )
                }
                userChange = false
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}

fun getDisplayName(resolver: ContentResolver, uriString: String): String? {
    return try {
        val uri = uriString.toUri()
        when (uri.scheme?.lowercase()) {
            "content" -> {
                resolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null, null, null
                )?.use { c ->
                    if (c.moveToFirst()) {
                        val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) c.getString(idx) else null
                    } else null
                }
            }

            "file" -> File(uri.path ?: "").name
            else -> uri.lastPathSegment ?: uriString
        }
    } catch (exception: Exception) {
        Timber.e("Error getting file name: ${exception.message}")
        null
    }
}


private class DatePickerVH(
    private val binding: ItemQDatepickerBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(tvTitle.context)}"
        tvTitle.setQuestionWithAsterisk(q.question.isMandatory == true)
        val current = item.answer as? String
        tvSelectedDate.text = current ?: ""
        tvSelectedDate.isVisible = !current.isNullOrEmpty()

        btnPickDate.setOnClickListener {
            childRecyclerView?.stopScroll()
            val c = Calendar.getInstance()
            DatePickerDialog(
                itemView.context,
                { _, y, m, d ->
                    val date = "%04d-%02d-%02d".format(y, m + 1, d)
                    tvSelectedDate.text = date
                    tvSelectedDate.isVisible = true
                    onAnswer(
                        AnswerKey(q.question.questionId, item.repeaterId, item.repeatIndex),
                        date
                    )
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}

class CheckboxVH(
    private val binding: ItemQCheckboxBinding,
    private val onAnswer: (AnswerKey, Any?) -> Unit,
    private val childRecyclerView: RecyclerView?=null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${item.displayNumbering(position1+1)}), ${q.question.localizedText(tvTitle.context)}"
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