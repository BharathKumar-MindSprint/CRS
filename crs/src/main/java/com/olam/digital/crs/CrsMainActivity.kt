package com.olam.digital.crs

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.olam.digital.crs.databinding.ActivityCrsMainBinding
import com.olam.digital.crs.fragment.CrsSuccessFragment
import com.olam.digital.crs.fragment.SurveyHomeFragment
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CrsMainActivity : AppCompatActivity() {
    lateinit var binding: ActivityCrsMainBinding
    private val crsViewModel: CrsQuestionaryViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set statusbar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        binding = ActivityCrsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        binding.apply {
            setUi()
            setObserver()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val navHost = supportFragmentManager
                    .findFragmentById(R.id.fragmentContainerView) as? NavHostFragment
                    ?: return finish()

                val navController = navHost.navController

                val current = navController.currentDestination?.id
                val start = navController.graph.startDestinationId // surveyHomeFragment

                if (current == start) {
                    finish()  // Only Home screen exits the Activity
                } else {
                    navController.popBackStack()  // Always go back in stack
                }
            }
        })
    }


    companion object {
        @JvmStatic
        fun createIntent(context: Context, farmerId: String): Intent {
            return Intent(context, CrsMainActivity::class.java)
        }
    }


    private fun setObserver() {
        binding.apply {
            crsViewModel.apply {
                farmersCrs.observe(this@CrsMainActivity) { farmersList ->
                    when (farmersList.state) {
                        ResourceState.SUCCESS -> {
                            Timber.d("Farmers fetched CRS: ${farmersList.data?.size}")
                        }

                        ResourceState.ERROR -> {

                        }

                        ResourceState.LOADING -> {

                        }

                        ResourceState.NO_DATA -> {

                        }
                    }
                }
            }
        }
    }

    private fun setUi() {
        binding.apply {
            //getAllFarmers()

           /* binding.root.viewTreeObserver.addOnGlobalLayoutListener {
                val rect = Rect()
                binding.root.getWindowVisibleDisplayFrame(rect)
                val screenHeight = binding.root.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) {
                    // ✅ Keyboard is open
                    val focusedView = binding.root.findFocus()
                    if (focusedView != null) {
                        binding.rvChildQuestions.post {
                            binding.rvChildQuestions.smoothScrollToPosition(
                                binding.rvChildQuestions.getChildAdapterPosition(focusedView)
                            )
                        }
                    }
                }
            }
*/


        }
    }
}