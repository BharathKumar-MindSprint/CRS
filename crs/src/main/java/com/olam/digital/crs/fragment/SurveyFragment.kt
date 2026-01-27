// fragment/SurveyFragment.kt
package com.olam.digital.crs.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.olam.digital.crs.R
import com.olam.digital.crs.adapter.CrsDynamicQustionsAdapter
import com.olam.digital.crs.databinding.FragmentSurveyCrsBinding
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SurveyFragment : Fragment() {

    private var _binding: FragmentSurveyCrsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CrsQuestionaryViewModel by activityViewModels()
    private lateinit var adapter: CrsDynamicQustionsAdapter

    // Pending ids for actions
    private var pendingPhotoQuestionId: Long? = null
    private var pendingFileQuestionId: Long? = null
    private var pendingLocationQuestionId: Long? = null

    // OpenDocument launchers
    private val openImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val qId = pendingPhotoQuestionId ?: return@registerForActivityResult
            uri?.let {
                persistReadPermission(it)
                viewModel.saveAnswer(qId, it.toString())
            }
            pendingPhotoQuestionId = null
        }

    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val qId = pendingFileQuestionId ?: return@registerForActivityResult
            uri?.let {
                persistReadPermission(it)
                viewModel.saveAnswer(qId, it.toString())
            }
            pendingFileQuestionId = null
        }

    // Location
    private lateinit var fusedLocation: FusedLocationProviderClient
    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) captureLocationNow() else
                Toast.makeText(requireContext(),
                    getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSurveyCrsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupAdapter()
        setupObservers()
        // viewModel.loadModule(arguments?.getLong("moduleId") ?: 1L)
    }

    private fun setupAdapter() {
        adapter = CrsDynamicQustionsAdapter(
            onAnswer = { qId, value -> viewModel.saveAnswer(qId, value) },
            onPickPhoto = { qId ->
                pendingPhotoQuestionId = qId
                openImageLauncher.launch(arrayOf("image/*"))
            },
            onPickFile = { qId ->
                pendingFileQuestionId = qId
                openFileLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "text/plain",
                        "*/*"
                    )
                )
            },
            onCaptureLocation = { qId ->
                pendingLocationQuestionId = qId
                requestOrCaptureLocation()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SurveyFragment.adapter
        }
    }

    private fun setupObservers() {
        // Observe the reactive UI list
        viewModel.uiQuestions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        // Existing module state (show blocking overlay)
        viewModel.selectedModule.observe(viewLifecycleOwner) { res ->
            when (res.state) {
                ResourceState.LOADING -> setBlockingLoading(true)
                ResourceState.SUCCESS -> {
                    setBlockingLoading(false)
                    Timber.d("Module loaded: ${res.data}")
                    // viewModel.loadModule(res.data?.moduleId ?: 1) // if needed
                }
                ResourceState.ERROR -> {
                    setBlockingLoading(false)
                    Toast.makeText(requireContext(), "Failed to load module", Toast.LENGTH_SHORT).show()
                }
                ResourceState.NO_DATA -> {
                    setBlockingLoading(false)
                    Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // If questions load asynchronously separately, you can expose a Resource for questions
        // and toggle the same overlay here.
    }

    private fun setBlockingLoading(show: Boolean) {
        binding.mProgress.visibility = if (show) View.VISIBLE else View.GONE
        val act = activity ?: return
        if (show) {
            act.window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            act.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    /* --------------- Location --------------- */

    private fun requestOrCaptureLocation() {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            captureLocationNow()
        }
    }

    @SuppressLint("MissingPermission")
    private fun captureLocationNow() {
        val qId = pendingLocationQuestionId ?: return

        fusedLocation.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                Timber.d("Captured location: ${loc.latitude}, ${loc.longitude}")
                viewModel.saveAnswer(qId, "${loc.latitude},${loc.longitude}")
                pendingLocationQuestionId = null
            } else {
                fusedLocation.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    null
                ).addOnSuccessListener { current ->
                    if (current != null) {
                        viewModel.saveAnswer(qId, "${current.latitude},${current.longitude}")
                    } else {
                        Toast.makeText(requireContext(),
                            getString(R.string.unable_to_capture_location), Toast.LENGTH_SHORT).show()
                    }
                    pendingLocationQuestionId = null
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), getString(R.string.unable_to_capture_location), Toast.LENGTH_SHORT).show()
                    pendingLocationQuestionId = null
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), getString(R.string.unable_to_capture_location), Toast.LENGTH_SHORT).show()
            pendingLocationQuestionId = null
        }
    }

    private fun persistReadPermission(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (permission: SecurityException) {
            // ignore if provider doesn't support persistable perms
            Timber.w("Persistable permission not supported for uri: ${permission.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}