// adapter/CrsDynamicQustionsAdapter.kt
package com.olam.digital.crs.adapter

import android.app.DatePickerDialog
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
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
import java.util.Calendar

class CrsDynamicQustionsAdapter(
    private val onAnswer: (Long, Any?) -> Unit,
    private val onPickPhoto: (Long) -> Unit,
    private val onPickFile: (Long) -> Unit,
    private val onCaptureLocation: (Long) -> Unit
) : ListAdapter<UIQuestionUi, RecyclerView.ViewHolder>(Diff) {

    init { setHasStableIds(true) }

    override fun getItemId(position: Int) = getItem(position).base.question.questionId

    override fun getItemViewType(position: Int) = when (getItem(position).base.type) {
        QuestionType.CHECKBOX      -> ViewType.CHECKBOX.ordinal
        QuestionType.DATEPICKER    -> ViewType.DATE_PICKER.ordinal
        QuestionType.DROPDOWN      -> ViewType.DROPDOWN.ordinal
        QuestionType.LOCATION      -> ViewType.LOCATION.ordinal
        QuestionType.PHOTO         -> ViewType.PHOTO_UPLOAD.ordinal
        QuestionType.PHOTO_CHECKBOX-> ViewType.PHOTO_CHECKBOX.ordinal
        QuestionType.RADIO         -> ViewType.RADIO.ordinal
        QuestionType.EDITTEXT      -> ViewType.TEXTBOX.ordinal
        QuestionType.FILE_UPLOAD   -> ViewType.FILE_UPLOAD.ordinal
        QuestionType.SIGNATURE     -> ViewType.SIGNATURE.ordinal
        else                       -> ViewType.TEXTBOX.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (ViewType.entries[viewType]) {
            ViewType.CHECKBOX -> CheckboxVH(ItemQCheckboxBinding.inflate(inf, parent, false), onAnswer)
            ViewType.DATE_PICKER -> DatePickerVH(ItemQDatepickerBinding.inflate(inf, parent, false), onAnswer)
            ViewType.DROPDOWN -> DropdownVH(ItemQDropdownBinding.inflate(inf, parent, false), onAnswer)
            ViewType.LOCATION -> LocationVH(ItemsQLocationCaptureBinding.inflate(inf, parent, false), onCaptureLocation)
            ViewType.PHOTO_CHECKBOX -> PhotoCheckboxVH(ItemQPhotoCheckboxMainBinding.inflate(inf, parent, false), onAnswer)
            ViewType.PHOTO_UPLOAD -> PhotoUploadVH(ItemQPhotoUploadBinding.inflate(inf, parent, false), onPickPhoto)
            ViewType.RADIO -> RadioVH(ItemQRadioBinding.inflate(inf, parent, false), onAnswer)
            ViewType.TEXTBOX -> TextboxVH(ItemQEdittextBinding.inflate(inf, parent, false), onAnswer)
            ViewType.FILE_UPLOAD -> FileUploadVH(ItemQFileUploadBinding.inflate(inf, parent, false), onPickFile)
            ViewType.SIGNATURE -> SignatureVH(ItemQSignatureCheckbocBinding.inflate(inf, parent, false), onAnswer)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is CheckboxVH     -> holder.bind(item, position)
            is DatePickerVH   -> holder.bind(item, position)
            is DropdownVH     -> holder.bind(item, position)
            is LocationVH     -> holder.bind(item, position)
            is PhotoCheckboxVH-> holder.bind(item, position)
            is PhotoUploadVH  -> holder.bind(item, position)
            is RadioVH        -> holder.bind(item, position)
            is TextboxVH      -> holder.bind(item, position)
            is FileUploadVH   -> holder.bind(item, position)
            is SignatureVH    -> holder.bind(item, position)
        }
    }

    object Diff : DiffUtil.ItemCallback<UIQuestionUi>() {
        override fun areItemsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi) =
            oldItem.base.question.questionId == newItem.base.question.questionId

        override fun areContentsTheSame(oldItem: UIQuestionUi, newItem: UIQuestionUi): Boolean {
            // Compare only fields that affect UI
            return oldItem.base.type == newItem.base.type &&
                    oldItem.base.question == newItem.base.question &&
                    oldItem.base.options == newItem.base.options &&
                    answersEqual(oldItem.answer, newItem.answer)
        }

        private fun answersEqual(a: Any?, b: Any?) =
            if (a is List<*> && b is List<*>) a == b else a == b
    }
}

/* ------------------------ VHs ------------------------ */

private class FileUploadVH(
    private val binding: ItemQFileUploadBinding,
    private val onPickFile: (Long) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        txtQuestion.text = "${position1 + 1}), ${q.question.questionText}"

        val uriStr = item.answer as? String
        layFilePreview.isVisible = !uriStr.isNullOrEmpty()
        txtFileName.text = if (uriStr.isNullOrEmpty()) "No file" else "File Selected"
        btnUpload.setOnClickListener { onPickFile(q.question.questionId) }
    }
}

private class SignatureVH(
    private val binding: ItemQSignatureCheckbocBinding,
    private val onAnswer: (Long, Any?) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        txtQuestion.text = "${position1 + 1}), ${q.question.questionText}"
        txtClear.setOnClickListener {
            signaturePad.clear()
            onAnswer(q.question.questionId, null)
        }

        // Optionally restore a preview from item.answer if you store bitmap/base64
        signaturePad.setOnSignedListener(object :
            com.github.gcacace.signaturepad.views.SignaturePad.OnSignedListener {
            override fun onStartSigning() {}
            override fun onSigned() {
                onAnswer(q.question.questionId, signaturePad.signatureBitmap)
            }
            override fun onClear() {
                //onAnswer(q.question.questionId, null)
            }
        })
    }
}

private class TextboxVH(
    private val binding: ItemQEdittextBinding,
    private val onAnswer: (Long, Any?) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var internalChange = false

    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${position1 + 1}), ${q.question.questionText}"
        etAnswer.hint = q.question.helpText ?: ""

        val text = item.answer as? String ?: ""
        if (etAnswer.text?.toString() != text) {
            internalChange = true
            etAnswer.setText(text)
            etAnswer.setSelection(text.length)
            internalChange = false
        }

        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !internalChange) {
                onAnswer(q.question.questionId, etAnswer.text?.toString())
            }
        }
        // If you want instant updates on each char, call onAnswer in a TextWatcher with a guard.
    }
}

private class RadioVH(
    private val binding: ItemQRadioBinding,
    private val onAnswer: (Long, Any?) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    // Reuse one listener instance to avoid allocation each bind
    private var currentListener: RadioGroup.OnCheckedChangeListener? = null

    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${position1 + 1}), ${q.question.questionText}"

        // 1) Build or rebuild radio buttons with stable, unique IDs
        //    (We rebuild to simplify keeping code deterministic.)
        rgOptions.setOnCheckedChangeListener(null) // detach any old listener
        rgOptions.removeAllViews()

        // map: optionId -> buttonId (stable per bind)
        val idMap = mutableMapOf<Long, Int>()
        q.options.forEach { opt ->
            val btnId = stableViewIdForOption(opt.optionId)
            idMap[opt.optionId] = btnId

            val rb = RadioButton(itemView.context).apply {
                id = btnId
                text = opt.label
                tag = opt.optionId     // keep optionId for easy lookup later
            }
            rgOptions.addView(rb)
        }

        // 2) Restore selection from answer without firing listener
        val selectedOptionId = item.answer as? Long
        if (selectedOptionId != null) {
            val btnId = idMap[selectedOptionId]
            if (btnId != null && rgOptions.checkedRadioButtonId != btnId) {
                // Do NOT call listener during restoration
                rgOptions.check(btnId)
            }
        } else {
            rgOptions.clearCheck()
        }

        // 3) Attach listener AFTER state restoration
        val newListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
            // Ignore if user cleared selection (checkedId == -1)
            if (checkedId == View.NO_ID) {
                onAnswer(q.question.questionId, null)
                return@OnCheckedChangeListener
            }
            val selected = group.findViewById<RadioButton>(checkedId)
            val optionId = selected?.tag as? Long
            // Only push if changed (avoid redundant VM writes)
            if (optionId != null && optionId != selectedOptionId) {
                onAnswer(q.question.questionId, optionId)
            }
        }
        currentListener = newListener
        rgOptions.setOnCheckedChangeListener(newListener)
    }

    /** Deterministic stable View ID from optionId */
    private fun stableViewIdForOption(optionId: Long): Int {
        // You can use generateViewId(), but deterministic mapping helps across binds.
        // Hash into positive Int but keep collisions unlikely.
        val hash = (optionId xor (optionId ushr 32)).toInt()
        return 0x7f000000 or (hash and 0x00ffffff)
    }
}

private class PhotoCheckboxVH(
    private val binding: ItemQPhotoCheckboxMainBinding,
    private val onAnswer: (Long, Any?) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        txtQuestion.text = "${position1 + 1}), ${q.question.questionText}"

        // Selected options from answer
        val selected = ((item.answer as? List<*>)?.filterIsInstance<Long>() ?: emptyList()).toMutableSet()

        // Uses your setUpAdapter extension
        mPhotosRecyclerView.setUpAdapter(
            q.options.toMutableList(),
            com.olam.digital.crs.R.layout.item_q_photo_checkbox,
            ItemQPhotoCheckboxBinding::inflate,
        { opt: CrsOptionEntity, _, bin ->
            bin.optionCheckbox.text = opt.label
            bin.optionCheckbox.setOnCheckedChangeListener(null)
            bin.optionCheckbox.isChecked = selected.contains(opt.optionId)
            bin.optionCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selected += opt.optionId else selected -= opt.optionId
                onAnswer(q.question.questionId, selected.toList())
            }

            // IMPORTANT: Bind the image from the *correct* field.
            val imageUri = imageUriOf(opt) // implement below to map to your entity
            if (!imageUri.isNullOrEmpty()) {
                bin.optionImage.setImageURI(Uri.parse(imageUri))
                bin.optionImage.isVisible = true
            } else {
                bin.optionImage.isVisible = false
            }
        })
    }

    private fun imageUriOf(opt: CrsOptionEntity): String? {
        // TODO: Map to your entity's field that holds the image URI.
        // Example candidates: opt.imageUri, opt.value, opt.mediaUrl, etc.
        // return opt.imageUri ?: opt.value
        return null
    }
}

private class PhotoUploadVH(
    private val binding: ItemQPhotoUploadBinding,
    private val onPickPhoto: (Long) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${position1 + 1}), ${q.question.questionText}"

        val uriStr = item.answer as? String
        ivPreview.isVisible = !uriStr.isNullOrEmpty()
        if (!uriStr.isNullOrEmpty()) ivPreview.setImageURI(Uri.parse(uriStr))

        btnUpload.setOnClickListener { onPickPhoto(q.question.questionId) }
    }
}

private class LocationVH(
    private val binding: ItemsQLocationCaptureBinding,
    private val onCaptureLocation: (Long) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${position1 + 1}), ${q.question.questionText}"

        val locText = item.answer as? String
        tvLocationStatus.text = locText ?: "Tap to capture location"
        tvLocationStatus.isVisible = !locText.isNullOrEmpty()

        itemView.setOnClickListener { onCaptureLocation(q.question.questionId) }
    }
}

private class DropdownVH(
    private val binding: ItemQDropdownBinding,
    private val onAnswer: (Long, Any?) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${position1 + 1}), ${q.question.questionText}"

        val labels = q.options.map { it.label ?: "" }
        val ids = q.options.map { it.optionId }

        val adapter = ArrayAdapter(
            itemView.context,
            android.R.layout.simple_spinner_item,
            labels
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter

        val selectedId = item.answer as? Long
        val initPos = if (selectedId != null) ids.indexOf(selectedId) else 0
        if (initPos in ids.indices) spinner.setSelection(initPos, false)

        var userChange = false
        spinner.setOnTouchListener { _, _ -> userChange = true; false }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, v: View?, pos: Int, id: Long) {
                if (userChange) onAnswer(q.question.questionId, ids[pos])
                userChange = false
            }
            override fun onNothingSelected(p0: AdapterView<*>) {}
        }
    }
}

private class DatePickerVH(
    private val binding: ItemQDatepickerBinding,
    private val onAnswer: (Long, Any?) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${position1 + 1}), ${q.question.questionText}"

        val current = item.answer as? String
        tvSelectedDate.text = current ?: ""
        tvSelectedDate.isVisible = !current.isNullOrEmpty()

        btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                itemView.context,
                { _, y, m, d ->
                    val date = "%04d-%02d-%02d".format(y, m + 1, d)
                    tvSelectedDate.text = date
                    tvSelectedDate.isVisible = true
                    onAnswer(q.question.questionId, date)
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}

private class CheckboxVH(
    private val binding: ItemQCheckboxBinding,
    private val onAnswer: (Long, Any?) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UIQuestionUi, position1: Int) = with(binding) {
        val q = item.base
        tvTitle.text = "${position1 + 1}), ${q.question.questionText}"
        llOptionsContainer.removeAllViews()

        val selected = ((item.answer as? List<*>)?.filterIsInstance<Long>() ?: emptyList()).toMutableSet()

        q.options.forEach { opt ->
            val cb = CheckBox(itemView.context).apply {
                text = opt.label
                tag = opt.optionId
                isChecked = selected.contains(opt.optionId)
            }
            cb.setOnCheckedChangeListener { _, checked ->
                if (checked) selected += opt.optionId else selected -= opt.optionId
                onAnswer(q.question.questionId, selected.toList())
            }
            llOptionsContainer.addView(cb)
        }
    }
}