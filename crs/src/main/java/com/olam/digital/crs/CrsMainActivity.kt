package com.olam.digital.crs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.olam.digital.crs.databinding.ActivityCrsMainBinding
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        //set statusbar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        binding = ActivityCrsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            setUi()
            setObserver()
        }
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
                    when(farmersList.state)
                    {
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

        }
    }
}