package com.olam.digital.crs.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.olam.digital.crs.R
import com.olam.digital.crs.databinding.LayoutFragmentAddCommunitySurveyBinding
import com.olam.digital.crs.extension.setPhoneLengthLimit
import com.olam.digital.crs.extension.showPopup
import com.olam.digital.crs.extension.toArrayList
import com.olam.digital.crs.extension.toast
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.room.CountryEntity
import com.olam.digital.crs.room.CrsCommunityRegionEntity
import com.olam.digital.crs.room.CrsProfileEntity
import com.olam.digital.crs.room.DistrictEntity
import com.olam.digital.crs.room.PlaceEntity
import com.olam.digital.crs.room.ProductEntity
import com.olam.digital.crs.room.ProfileWithMapping
import com.olam.digital.crs.room.RespondentEntity
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.Calendar

@AndroidEntryPoint
class CrsCommunityRegisterFragment : Fragment() {

    lateinit var binding: LayoutFragmentAddCommunitySurveyBinding
    private val viewModel: CrsQuestionaryViewModel by activityViewModels()
    private var pendingPhotoQuestionId: Long? = null

    private var listOfUserProfiles: List<ProfileWithMapping>? = null
    private var listOfUserCountryProfiles: List<CountryEntity>? = null
    private var listOfUserProductProfiles: List<ProductEntity>? = null
    private var listOfUserRegionProfiles: List<CrsCommunityRegionEntity>? = null
    private var listOfUserDistrictProfiles: List<DistrictEntity>? = null
    private var listOfUserPlaceProfiles: List<PlaceEntity>? = null

    private val openImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val qId = pendingPhotoQuestionId ?: return@registerForActivityResult
            uri?.let {
                val fileName = "img_${qId}_${System.currentTimeMillis()}.jpg"
                val internalPath = copyUriToInternalStorage(it, fileName)
                Timber.d("Image copied to internal storage: $internalPath")
                viewModel.updateProfileImage(internalPath ?: "")
            }
            pendingPhotoQuestionId = null
        }

    private fun copyUriToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().filesDir, fileName)
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Error copying file")
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutFragmentAddCommunitySurveyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
        setObservers()
    }

    // --- Utilities ---

    private fun getFilterArray(nationalIdLimit: Int): Array<out InputFilter>? {
        return arrayOf<InputFilter>(InputFilter.LengthFilter(nationalIdLimit))
    }

    private fun getPoint(anchor: View): Point {
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        return Point(location[0], location[1])
    }

    private fun View.safePopupWidth(): Int =
        this.let { v -> v.width.takeIf { it > 0 } ?: v.measuredWidth }
            .takeIf { it > 0 }
            ?: (this.resources.displayMetrics.widthPixels / 2)

    private fun getDialCodeUsingLib(): String {
        val country = getUserCountry()
        val phoneUtil = PhoneNumberUtil.getInstance()
        val code = phoneUtil.getCountryCodeForRegion(country)
        return "+$code"
    }

    // Country detection (SIM → Network → Locale)
    private fun getUserCountry(): String {
        val tm = context?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

        tm?.simCountryIso?.takeIf { it.isNotBlank() }?.let { return it.uppercase() }
        tm?.networkCountryIso?.takeIf { it.isNotBlank() }?.let { return it.uppercase() }

        val cfg = context?.resources?.configuration
        val country = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cfg?.locales?.get(0)?.country
            } else {
                @Suppress("DEPRECATION")
                cfg?.locale?.country
            }
        } catch (_: Exception) {
            null
        }
        return (country ?: "IN").uppercase()
    }

    // --- UI ---

    //profile id -> country id  -> display two country

    private fun setUi() = with(binding) {
        // Load all data
            viewModel.getProfilesWithCommunityMapping()
            viewModel.getAllProductNameFromMapping()
            viewModel.getAllCountryNameFromMapping()
            viewModel.getAllRegionIdsFromMapping()
            viewModel.getAllDistrictNameFromMapping()
            viewModel.getAllPlaceNameFromMapping()

        // Top bar
        imgBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Image picker
        imgCamera.setOnClickListener {
            pendingPhotoQuestionId = 1
            openImageLauncher.launch("image/*")
        }

        // Phone code & length
        etCountryCode.setText(getDialCodeUsingLib())
        etMobileNumber.setPhoneLengthLimit(etCountryCode.text.toString())

        // Date picker (no post)
        etdateofbirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -18)
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val dob = "%04d-%02d-%02d".format(y, m + 1, d)
                    etdateofbirth.setText(dob)
                },
                year,
                month,
                day
            )
            datePicker.datePicker.maxDate = calendar.timeInMillis
            datePicker.show()
        }

        // Make selector-style fields act as pickers (defensive, complements XML)
        makeSelector(etproduct)
        makeSelector(etmartialstatus)
        makeSelector(etBusineesUnit)
        makeSelector(etRegionMain)
        makeSelector(etDistrict)
        makeSelector(etPlace)
        makeSelector(etgender)
        makeSelector(etdateofbirth) // clickable-only control

        // First-tap popup (no post {})
       showNormalPopUp(1,viewModel.selectedCrsProfileData.value?.data)
        etmartialstatus.setOnClickListener { showNormalPopUp(
            2
        ) }
        etBusineesUnit.setOnClickListener { showNormalPopUp(
            3
        ) }
        etRegionMain.setOnClickListener { showNormalPopUp(
            4
        ) }
        etDistrict.setOnClickListener { showNormalPopUp(
            5
        ) }
        etPlace.setOnClickListener { showNormalPopUp(6) }
        etgender.setOnClickListener { showNormalPopUp(7) }

        // Submit
        layFilter.setOnClickListener {
            // Photo validation
            val photoPath = imgfarmer.tag as? String
            if (photoPath.isNullOrEmpty()) {
                imgfarmer.context.toast(getString(R.string.please_upload_image))
                return@setOnClickListener
            }

            if (etFarmerName.text.toString().isEmpty()) {
                etFarmerName.error = getString(R.string.please_enter_farmer_name)
                return@setOnClickListener
            }

            if (etgender.text.toString().isEmpty()) {
                etgender.error = getString(R.string.please_select_gender)
                return@setOnClickListener
            }

            if (etMobileNumber.text.toString().isEmpty()) {
                etMobileNumber.error = getString(R.string.please_enter_mobile_number)
                return@setOnClickListener
            }

            if (etdateofbirth.text.toString().isEmpty()) {
                etdateofbirth.error = getString(R.string.please_select_date_of_birth)
                return@setOnClickListener
            }

            if (etproduct.text.toString().isEmpty()) {
                etproduct.error = getString(R.string.please_select_profile)
                return@setOnClickListener
            }

            if (etmartialstatus.text.toString().isEmpty()) {
                etmartialstatus.error = getString(R.string.please_select_country)
                return@setOnClickListener
            }

            if (etBusineesUnit.text.toString().isEmpty()) {
                etBusineesUnit.error = getString(R.string.please_select_product) // fixed target
                return@setOnClickListener
            }

            if (etRegionMain.text.toString().isEmpty()) {
                etRegionMain.error = getString(R.string.please_select_region)
                return@setOnClickListener
            }

            if (etDistrict.text.toString().isEmpty()) {
                etDistrict.error = getString(R.string.please_select_district)
                return@setOnClickListener
            }

            if (etPlace.text.toString().isEmpty()) {
                etPlace.error = getString(R.string.please_select_place)
                return@setOnClickListener
            }

            // Save
            viewModel.saveCrsProfile(
                RespondentEntity(
                    id = 0,
                    name = etFarmerName.text.toString(),
                    countryId = etmartialstatus.tag.toString().toInt(),
                    productId = etBusineesUnit.tag.toString().toInt(),
                    profileId = etproduct.tag.toString().toInt(),
                    regionId = etRegionMain.tag.toString().toInt(),
                    districtId = etDistrict.tag.toString().toInt(),
                    placeId = etPlace.tag.toString().toInt(),
                    emailId = etEmailId.text.toString(),
                    phoneNumber = etMobileNumber.text.toString(),
                    countryCode = etCountryCode.text.toString(),
                    respondentCode = System.currentTimeMillis().toString(),
                    dateOfBirth = etdateofbirth.text.toString(),
                    isSynced = 1,
                    gender = etgender.text.toString(),
                    photo = photoPath
                )
            )
        }
    }

    private fun makeSelector(view: View) {
        view.isFocusable = false
        view.isFocusableInTouchMode = false
        view.isClickable = true
        if (view is android.widget.TextView) {
            view.isCursorVisible = false
            // inputType none is already set in XML; keep it here as a safeguard:
            view.setOnLongClickListener { true } // disable text selection popup
        }
    }

    private fun getArrayNameList(
        stringArray: java.util.ArrayList<ProfileWithMapping>,
        countryId: String
    ): Array<String?> {
        val localArrayData = arrayOfNulls<String>(stringArray.size)
        var index = 0
        stringArray.forEach { lData ->
           if (lData.mapping.countryId.toString().equals(countryId,true))
           {
               localArrayData[index++] = lData.profile.profileName
           }
        }
        return localArrayData
    }

    private fun getArrayProductNameList(stringArray: java.util.ArrayList<ProductEntity>): Array<String?> {
        val localArrayData = arrayOfNulls<String>(stringArray.size)
        var index = 0
        stringArray.forEach { lData ->
            localArrayData[index++] = lData.name
        }
        return localArrayData
    }

    private fun getCountryRegionNameList(
        stringArray: java.util.ArrayList<CrsCommunityRegionEntity>,
        selectedCountryId: String
    ): Array<String?> {
        val localArrayData = arrayOfNulls<String>(stringArray.size)
        var index = 0
        stringArray.forEach { lData ->
            Timber.d("Selected Country ID: $selectedCountryId, Region's Country ID: ${lData.countryId}")
            if (lData.countryId.equals(selectedCountryId,true)) {
                localArrayData[index++] = lData.name
            }
        }
        return localArrayData.copyOf(index)
    }

    private fun getCountryDistrictNameList(
        stringArray: java.util.ArrayList<DistrictEntity>
    ): Array<String?> {
        val localArrayData = arrayOfNulls<String>(stringArray.size)
        var index = 0
        stringArray.forEach { lData ->
            localArrayData[index++] = lData.name
        }
        return localArrayData
    }

    private fun getCountryPlaceNameList(
        stringArray: java.util.ArrayList<PlaceEntity>
    ): Array<String?> {
        val localArrayData = arrayOfNulls<String>(stringArray.size)
        var index = 0
        stringArray.forEach { lData ->
            localArrayData[index++] = lData.name
        }
        return localArrayData
    }

    private fun getCountryNameList(
        stringArray: java.util.ArrayList<CountryEntity>
    ): Array<String?> {
        val localArrayData = arrayOfNulls<String>(stringArray.size)
        var index = 0
        stringArray.forEach { lData ->
            localArrayData[index++] = lData.name
        }
        return localArrayData
    }

    private fun getArrayList(stringArray: Array<String?>): java.util.ArrayList<String> {
        val localArrayData = java.util.ArrayList<String>()
        stringArray.forEach { lData ->
            localArrayData.add(lData ?: "")
        }
        return localArrayData
    }

    private fun showNormalPopUp(intentData: Int, lData: CrsProfileEntity?=null) = with(binding) {
        when (intentData) {
            1 -> { // Profile
                etproduct.setText(lData?.profileName)
                etproduct.tag = lData?.id
            }

            2 -> { // Country

                //filter country based on profile id
                listOfUserCountryProfiles?.let { countryList ->

                    // 1. Filter mapping list by selected profile ID and extract countryIds
                    val selectedProfileId = etproduct.tag?.toString()?.toIntOrNull()
                    Timber.d("Selected Profile ID: $selectedProfileId")
                    Timber.d("Full Mapping List: ${listOfUserProfiles?.joinToString { "ProfileID: ${it.profile.id}, CountryID: ${it.mapping.countryId}" }}")
                    val filterCountries: Set<Int> =
                        listOfUserProfiles
                            ?.filter { it.profile.id == selectedProfileId }
                            ?.mapNotNull { it.mapping.countryId }
                            ?.toSet()
                            ?: emptySet()
                    Timber.d("Filtered Country IDs for Profile $selectedProfileId: $filterCountries")

                    // 2. Filter countries by those allowed for this profile
                    val filteredCountryList = countryList.filter { filterCountries.contains(it.id) }

                    Timber.d("Filtered Country List: ${filteredCountryList.joinToString { "ID: ${it.id}, Name: ${it.name}" }}")

                    // 3. Show popup
                    requireContext().showPopup(
                        getPoint(etmartialstatus),
                        etmartialstatus.safePopupWidth(),
                        getCountryNameList(filteredCountryList.toArrayList()),
                        ArrayList<Any>(filteredCountryList)
                    ) { selectedItem ->

                        val selectedCountry = selectedItem as CountryEntity
                        etmartialstatus.setText(selectedCountry.name)
                        etmartialstatus.tag = selectedCountry.id

                        // RESET downstream selections
                        etBusineesUnit.setText(""); etBusineesUnit.tag = null
                        etRegionMain.setText("");   etRegionMain.tag = null
                        etDistrict.setText("");     etDistrict.tag = null
                        etPlace.setText("");        etPlace.tag = null
                    }
                }
            }

            3 -> { // Business Unit
                if (etmartialstatus.tag == null) return
                listOfUserProductProfiles?.toArrayList()?.let {
                    requireContext().showPopup(
                        getPoint(etBusineesUnit),
                        etBusineesUnit.safePopupWidth(),
                        getArrayProductNameList(it.toArrayList()),
                        it as ArrayList<Any>
                    ) { returnData ->
                        returnData as ProductEntity
                        etBusineesUnit.setText(returnData.name)
                        etBusineesUnit.tag = returnData.id

                        // Reset downstream
                        etRegionMain.setText(""); etRegionMain.tag = null
                        etDistrict.setText("");   etDistrict.tag = null
                        etPlace.setText("");      etPlace.tag = null
                    }
                }
            }

            4 -> { // Region
                if (etBusineesUnit.tag == null) return
                listOfUserRegionProfiles?.let {
                    requireContext().showPopup(
                        getPoint(etRegionMain),
                        etRegionMain.safePopupWidth(),
                        getCountryRegionNameList(it.toArrayList(), etmartialstatus.tag.toString()),
                        it.filter { it-> it.countryId.equals(etmartialstatus.tag.toString(),true) }  as ArrayList<Any>
                    ) { ldata ->
                        ldata as CrsCommunityRegionEntity
                        etRegionMain.setText(ldata.name)
                        etRegionMain.tag = ldata.id

                        // Reset downstream
                        etDistrict.setText(""); etDistrict.tag = null
                        etPlace.setText("");    etPlace.tag = null
                    }
                }
            }

            5 -> { // District
                if (etRegionMain.tag == null) return
                listOfUserDistrictProfiles?.let {
                    val selectedDistrict = it.filter { district ->
                        district.regionId == etRegionMain.tag.toString().toInt()
                    }
                    requireContext().showPopup(
                        getPoint(etDistrict),
                        etDistrict.safePopupWidth(),
                        getCountryDistrictNameList(selectedDistrict.toArrayList()),
                        selectedDistrict as ArrayList<Any>
                    ) { ldata ->
                        ldata as DistrictEntity
                        etDistrict.setText(ldata.name)
                        etDistrict.tag = ldata.id

                        // Reset downstream
                        etPlace.setText(""); etPlace.tag = null
                    }
                }
            }

            6 -> { // Place
                if (etDistrict.tag == null) return
                listOfUserPlaceProfiles?.let {
                    val selectedPlace = it.filter { place ->
                        place.district_id == etDistrict.tag.toString().toInt()
                    }
                    requireContext().showPopup(
                        getPoint(etPlace),
                        etPlace.safePopupWidth(),
                        getCountryPlaceNameList(selectedPlace.toArrayList()),
                        selectedPlace as ArrayList<Any>
                    ) { ldata ->
                        ldata as PlaceEntity
                        etPlace.setText(ldata.name)
                        etPlace.tag = ldata.id
                    }
                }
            }

            7 -> { // Gender
                requireContext().showPopup(
                    getPoint(etgender),
                    etgender.safePopupWidth(),
                    resources.getStringArray(R.array.genderArray),
                    getArrayList(resources.getStringArray(R.array.genderArray)) as ArrayList<Any>
                ) {
                    etgender.setText(it.toString())
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.clearSaveRespondent()
    }

    private fun setObservers() = with(binding) {
        viewModel.apply {
            farmersCrsProfileWithMapping.observe(viewLifecycleOwner) {
                when (it.state) {
                    ResourceState.SUCCESS -> listOfUserProfiles = it.data ?: emptyList()
                    else -> Unit
                }
            }
            farmersCrsProfileCountryName.observe(viewLifecycleOwner) {
                when (it.state) {
                    ResourceState.SUCCESS -> listOfUserCountryProfiles = it.data ?: emptyList()
                    else -> Unit
                }
            }
            farmersCrsProfileProductName.observe(viewLifecycleOwner) {
                when (it.state) {
                    ResourceState.SUCCESS -> listOfUserProductProfiles = it.data ?: emptyList()
                    else -> Unit
                }
            }
            farmersCrsProfileRegionName.observe(viewLifecycleOwner) {
                when (it.state) {
                    ResourceState.SUCCESS -> listOfUserRegionProfiles = it.data ?: emptyList()
                    else -> Unit
                }
            }
            farmersCrsProfileDistrictName.observe(viewLifecycleOwner) {
                when (it.state) {
                    ResourceState.SUCCESS -> listOfUserDistrictProfiles = it.data ?: emptyList()
                    else -> Unit
                }
            }
            farmersCrsProfilePlaceName.observe(viewLifecycleOwner) {
                when (it.state) {
                    ResourceState.SUCCESS -> listOfUserPlaceProfiles = it.data ?: emptyList()
                    else -> Unit
                }
            }
            crsProfileSaved.observe(viewLifecycleOwner) {
                when (it.state) {
                    ResourceState.SUCCESS -> requireActivity().onBackPressedDispatcher.onBackPressed()
                    else -> Unit
                }
            }
            selectResponsentImage.observe(viewLifecycleOwner) { path ->
                if (!path.isNullOrEmpty()) {
                    Glide.with(imgfarmer.context).load(path).into(imgfarmer)
                    imgfarmer.tag = path
                }
            }
        }
    }
}