package com.olam.digital.crs.extension

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.olam.digital.crs.R
import com.olam.digital.crs.adapter.PopUpRecylerAdapter
import com.olam.digital.crs.databinding.LayoutPopupListBinding
import com.olam.digital.crs.model.Resource
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.model.UIQuestionUi
import com.olam.digital.crs.room.CRSQuestionEntity
import com.olam.digital.crs.room.CrsOptionEntity
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun View.show() {
    this.visibility = android.view.View.VISIBLE
}

fun View.hide() {
    this.visibility = android.view.View.GONE
}

fun View.invisible() {
    this.visibility = android.view.View.INVISIBLE
}

fun View.showOrHide(show: Boolean) {
    this.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
}

fun View.showOrInvisible(show: Boolean) {
    this.visibility = if (show) android.view.View.VISIBLE else android.view.View.INVISIBLE
}

fun View.isVisible(): Boolean {
    return this.visibility == android.view.View.VISIBLE
}

fun View.isGone(): Boolean {
    return this.visibility == android.view.View.GONE
}

fun View.isInvisible(): Boolean {
    return this.visibility == android.view.View.INVISIBLE
}

fun View.toggleVisibility() {
    this.visibility = if (this.visibility == android.view.View.VISIBLE) {
        android.view.View.GONE
    } else {
        android.view.View.VISIBLE
    }
}


fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


fun Context.longToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}



fun <T> MutableLiveData<Resource<T>>.setSuccess(data: T? = null) {
    value = Resource(ResourceState.SUCCESS, data)
}

fun <T> MutableLiveData<Resource<T>>.postSuccess(data: T? = null) {
    postValue(Resource(ResourceState.SUCCESS, data))
}

fun <T> MutableLiveData<Resource<T>>.postNoData(data: T? = null) {
    postValue(Resource(ResourceState.NO_DATA, data))
}

fun <T> MutableLiveData<Resource<T>>.setLoading() {
    value = Resource(ResourceState.LOADING, value?.data)
}

fun <T> MutableLiveData<Resource<T>>.postLoading() {
    postValue(Resource(ResourceState.LOADING, value?.data))
}

fun <T> MutableLiveData<Resource<T>>.setError(message: String? = null) {
    value = Resource(ResourceState.ERROR, value?.data, message)
}

fun <T> MutableLiveData<Resource<T>>.postError(message: String? = null) {
    postValue(Resource(ResourceState.ERROR, value?.data, message))
}

fun Long.millisToLocalTime(): String {
    val date = Date(this)  // Convert millis to Date object
    val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm:ss a", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()  // Local timezone
    return sdf.format(date)
}



fun Context.getSecureAndroidId(): String {
    // Returns null only in extremely rare situations; handle defensively.
    return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        ?: "unknown"
}

/** Encode a Bitmap to Base64 string (PNG by default to preserve transparency) */
fun Context.bitmapToBase64(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(format, quality, baos)
    val bytes = baos.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP) // NO_WRAP keeps it compact
}

/** Decode a Base64 string back to Bitmap */


fun String.base64ToBitmap(): Bitmap? {
    // Strip optional data URI prefix
    val pure = this.substringAfter("base64,", this)
        .replace("\n", "")
        .replace("\r", "")

    return try {
        val bytes = Base64.decode(pure, Base64.DEFAULT)
        val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        // Force mutable ARGB_8888
        raw?.copy(Bitmap.Config.ARGB_8888, /*mutable=*/true)
    } catch (e: Exception) {
        Timber.e(e, "Failed to decode base64 → bitmap")
        null
    }
}


fun EditText.setDecimalWithCharacterLimit(maxBefore: Int, maxAfter: Int) {
    val filter = InputFilter { source, start, end, dest, dstart, dend ->
        val newValue = dest.subSequence(0, dstart).toString() +
                source.subSequence(start, end).toString() +
                dest.subSequence(dend, dest.length)

        val dotIndex = newValue.indexOf(".")

        val beforeDigits = if (dotIndex == -1) newValue.count { it.isDigit() }
        else newValue.substring(0, dotIndex).count { it.isDigit() }
        if (beforeDigits > maxBefore) return@InputFilter ""

        if (dotIndex != -1) {
            val afterDigits = newValue.substring(dotIndex + 1).count { it.isDigit() }
            if (afterDigits > maxAfter) return@InputFilter ""
        }

        // Only one decimal point allowed
        if (newValue.count { it == '.' } > 1 && source.toString() == ".") return@InputFilter ""

        null
    }
    this.filters = arrayOf(filter)
    this.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
}


 fun String.normalizeToken(): String =
    trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "") // remove spaces, hyphens, underscores, etc.






fun Activity.setColorStatusBarColor(color: Int) {
    val window: Window = window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    when (color) {
        1 -> {
            window.statusBarColor =
                ContextCompat.getColor(this, R.color.colorPrimary)
        }

        2 -> {
            window.statusBarColor =
                ContextCompat.getColor(this, R.color.topBarBrown)
        }

        3 -> {
            window.statusBarColor =
                ContextCompat.getColor(this, R.color.darkGray)
        }

        4 -> {
            window.statusBarColor =
                ContextCompat.getColor(this, R.color.colorOrangeBack)
        }

        else -> {

        }

    }

    when (color) {
        1, 4, 3 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {

                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }

        2 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.getAgeInYears(): Int = ChronoUnit.YEARS.between(this, LocalDate.now()).toInt()

@RequiresApi(Build.VERSION_CODES.O)
fun Context.getAge(year: Int, month: Int, dayOfMonth: Int): Int {
    return Period.between(
        LocalDate.of(year, month, dayOfMonth),
        LocalDate.now()
    ).years
}

fun Context.getCurrentAge(ageYear: Int, currentYear: Int): Int {
    return (currentYear - ageYear)
}


fun EditText.setPhoneLengthLimit(dialCode: String) {
    val phoneUtil = PhoneNumberUtil.getInstance()

    val region = phoneUtil.getRegionCodeForCountryCode(
        dialCode.replace("+", "").toInt()
    )

    val example = phoneUtil.getExampleNumber(region)
    val maxLength = phoneUtil.getNationalSignificantNumber(example).length

    this.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
}




fun Context.showPopup(
    p: Point,
    width: Int,
    mList: Array<String?>,
    returnList: ArrayList<Any>?,
    clickedReturn: ((userData: Any) -> Unit)
) {
    val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val binding = LayoutPopupListBinding.inflate(layoutInflater)
    val popUp = PopupWindow(this)
    popUp.contentView = binding.root
    popUp.width = width + width
    popUp.height = LinearLayout.LayoutParams.WRAP_CONTENT
    popUp.isFocusable = true

    val x = LinearLayout.LayoutParams.WRAP_CONTENT
    val y = 60
    popUp.setBackgroundDrawable(ColorDrawable())
    popUp.animationStyle = R.style.popup_window_animation
    binding.apply {
        Timber.d("Array list data ${Gson().toJson(mList)}")
        Timber.d("return list data ${Gson().toJson(returnList)}")
        val adapter = PopUpRecylerAdapter(mList, returnList) {
            it?.let { clickedReturn?.invoke(it) }
            popUp.dismiss()
        }
        mRecyler.adapter = adapter
    }
    popUp.showAtLocation(binding.root, Gravity.NO_GRAVITY, p.x + x, p.y + y)
}


fun Context.showPopupBottomSheet(
    p: Point,
    width: Int,
    mList: Array<String?>,
    view: View,
    returnList: ArrayList<Any>?,
    clickedReturn: ((userData: Any) -> Unit)
) {
    val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val binding = LayoutPopupListBinding.inflate(layoutInflater)
    val popUp = PopupWindow(this)
    popUp.contentView = binding.root
    popUp.width = width + width
    popUp.height = LinearLayout.LayoutParams.WRAP_CONTENT
    popUp.isFocusable = true

    val x = LinearLayout.LayoutParams.WRAP_CONTENT
    val y = 60
    popUp.setBackgroundDrawable(ColorDrawable())
    popUp.animationStyle = R.style.popup_window_animation
    binding.apply {
        val adapter = PopUpRecylerAdapter(mList, returnList) {
            it?.let { clickedReturn?.invoke(it) }
            popUp.dismiss()
        }
        mRecyler.adapter = adapter
    }
    popUp.showAsDropDown(view, Gravity.NO_GRAVITY, p.x + x, p.y + y)
}

inline fun <reified T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}


fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun Fragment.hideKeyboard() {
    context?.apply {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this@hideKeyboard.requireView().windowToken, 0)
    }
}


fun Context.loadImage(path: Any, imageView: ImageView) {
    Glide.with(this).load(path).into(imageView)
}

fun Context.setArrayAdapter(autoEditText: AutoCompleteTextView, array: Array<String?>) {
    val arrayAdapter =
        ArrayAdapter(autoEditText.context, R.layout.dropdownedittext, array)
    autoEditText.setAdapter(arrayAdapter)
}


fun Context.showCustomAlertDialog(
    msg: String?,
    heading: String?,
    alertDialogType: String,
    clickedReturn: ((userData: Any) -> Unit)
) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.alert_delete_popup_layout)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    msg?.let {
        val text = dialog.findViewById<View>(R.id.txtAlertHeading) as TextView

        text.text = heading
    }
    heading?.let {
        val textMessage = dialog.findViewById<View>(R.id.txtAlertDesc) as TextView
        textMessage.text = msg
    }
    val dialogBtn_cancel =
        dialog.findViewById<View>(R.id.layCancel) as RelativeLayout
    dialogBtn_cancel.setOnClickListener { //
        clickedReturn.invoke(0)
        dialog.dismiss()
    }
    val dialogBtn_okay =
        dialog.findViewById<View>(R.id.layBtnPositive) as RelativeLayout
    dialogBtn_okay.setOnClickListener {
        clickedReturn.invoke(1)
        dialog.cancel()
    }
    val textDelete = dialog.findViewById<View>(R.id.txtDelete) as TextView
    val textCancelDelete = dialog.findViewById<View>(R.id.txtCancel) as TextView

    when (alertDialogType) {
        AlertDialogUiState.CANCEL_CONFIRM.name -> {
            dialogBtn_okay.background =
                ContextCompat.getDrawable(dialogBtn_okay.context, R.drawable.bg_selector_black)
            textDelete.text = dialogBtn_cancel.context.getString(R.string.yes_cancel)
            textCancelDelete.text = dialogBtn_cancel.context.getString(R.string.not_now)
        }

        AlertDialogUiState.DELETE_CONFIRM.name -> {
            dialogBtn_okay.background =
                ContextCompat.getDrawable(dialogBtn_okay.context, R.drawable.bg_selector_error)
            textDelete.text = dialogBtn_cancel.context.getString(R.string.delete)
            textCancelDelete.text = dialogBtn_cancel.context.getString(R.string.cancel)
        }
        AlertDialogUiState.HOUSEHOLD_INFO.name->
        {
            dialogBtn_okay.background =
                ContextCompat.getDrawable(dialogBtn_okay.context, R.drawable.bg_selector_error)
            textDelete.text = dialogBtn_cancel.context.getString(R.string.yes)
            textCancelDelete.text = dialogBtn_cancel.context.getString(R.string.no)
        }

        else -> {
            dialogBtn_okay.background =
                ContextCompat.getDrawable(dialogBtn_okay.context, R.drawable.bg_selector_error)
            textDelete.text = dialogBtn_cancel.context.getString(R.string.yes)
            textCancelDelete.text = dialogBtn_cancel.context.getString(R.string.cancel)
        }
    }
    dialog.show()
}


@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Intent.getSerializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getSerializableExtra(key, T::class.java)
    else
        this.getSerializableExtra(key) as? T
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getSerializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getSerializable(key, T::class.java)
    else
        this.getSerializable(key) as? T
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelable(key: String): T? {
    Timber.d("intentextras parce")
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(key, T::class.java)
    } else {
        this.getParcelableExtra(key)
    }
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableArrayList(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableArrayListExtra(key, T::class.java)
    } else {
        this.getParcelableArrayListExtra(key)
    }
}

fun Context.getNormalTimeFormatOnlyDate(timeinMillious: Long): String {
    val date = Date(timeinMillious)

    val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    return outputFormat.format(date)
}

fun Context.getNormalTimeFormatOnlyTime(timeinMillious: Long): String {
    val date = Date(timeinMillious)

    val outputFormat = SimpleDateFormat("hh:mm", Locale.ENGLISH)
    return outputFormat.format(date)
}


fun Context.getNormalTimeFormat(timeinMillious: Long): String {
    val date = Date(timeinMillious)

    val outputFormat = SimpleDateFormat("dd-MM-yyyy, hh:mma", Locale.ENGLISH)
    return outputFormat.format(date)
}


fun String.convertToTimestamp(): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    val date: Date = dateFormat.parse(this)
    return date.time
}

fun RecyclerView.ensureFocusedViewAboveKeyboard(root: View, insets: WindowInsetsCompat) {
    val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
    if (!imeVisible) return

    val focusedView = root.findFocus() ?: return

    // Find the nearest RecyclerView ancestor of the focused view
    var parent: View? = focusedView.parent as? View
    var recycler: RecyclerView? = null
    while (parent != null) {
        if (parent is RecyclerView) {
            recycler = parent
            break
        }
        parent = parent.parent as? View
    }

    recycler?.post {
        val vh = recycler.findContainingViewHolder(focusedView)
        val pos = vh?.adapterPosition
        if (pos != null && pos != RecyclerView.NO_POSITION) {
            recycler.smoothScrollToPosition(pos)
        }

        // Calculate overlap with keyboard
        val rect = Rect()
        focusedView.getGlobalVisibleRect(rect)
        val keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        val screenHeight = recycler.resources.displayMetrics.heightPixels

        val overlap = rect.bottom + keyboardHeight - screenHeight
        if (overlap > 0) {
            recycler.smoothScrollBy(0, overlap)
        }
    }
}




fun CRSQuestionEntity.localizedText(context: Context): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val lang = prefs.getString("app_language", Locale.getDefault().language) ?: "en"
    return when (lang) {
        "fr" -> questionText_fr
        "es" -> questionText_es
        "id" -> questionText_id
        "pt" -> questionText_pt
        "tr" -> questionText_tr
        "lo" -> questionText_lo
        "vi" -> questionText_vi
        "th" -> questionText_th
        "mr" -> questionText_mr
        else -> null
    }?.takeIf { !it.isNullOrBlank() }
        ?: questionText.orEmpty()
}


// Extension on UIQuestionUi
fun UIQuestionUi.displayNumbering(position: Int): String {
    Timber.d("Repeater Id : $repeaterId, Repeat Index: $repeatIndex, Child Index: $childIndex, Child Main Question Position: $childMainQuestionPosition")
    return if (repeaterId != null && repeatIndex != null && childIndex != null) {
        // Child inside a repeater → parentId.repeatIndex.childIndex
        val parentId = repeaterId
        val repeatIdx = repeatIndex
        val childIdx = childIndex ?: 1 // pass via mapIndexed when building children
        "$childMainQuestionPosition.$repeatIdx.$childIdx"
    } else {
        // Normal question → just position
        "${position}"
    }
}


fun View.clearFocusAndHideKeyboard() {
    // 1. Clear focus from the view
    clearFocus()

    // 2. Hide the soft keyboard
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}


fun TextView.setQuestionWithAsterisk(isManitory:Boolean) {
    if (isManitory)
    {
        val spannable  = SpannableString(this.text.toString() + " *")
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this.context, R.color.colorRed)),
            spannable.length - 1,
            spannable.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        this.text = spannable
    }
    else{
        this.text = this.text.toString()
    }
}


fun CrsOptionEntity.localizedLabel(context: Context): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val lang = prefs.getString("app_language", Locale.getDefault().language) ?: "en"
    return when (lang) {
        "fr" -> options_fr
        "es" -> options_es
        "id" -> options_id
        "pt" -> options_pt
        "tr" -> options_tr
        "lo" -> options_lo
        "vi" -> options_vi
        "th" -> options_th
        "mr" -> options_mr
        else -> null
    }?.takeIf { !it.isNullOrBlank() }
        ?: label.orEmpty()
}



enum class AlertDialogUiState{
    CANCEL_CONFIRM,
    DELETE_CONFIRM,
    DISPATCH_INFO,
    HOUSEHOLD_INFO
}
