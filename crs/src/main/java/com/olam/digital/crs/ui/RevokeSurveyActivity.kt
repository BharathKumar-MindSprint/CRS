package com.olam.digital.crs.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.olam.digital.crs.CrsMainActivity
import com.olam.digital.crs.databinding.LayoutRevokeSurveyBinding
import com.olam.digital.crs.model.ResourceState
import com.olam.digital.crs.viewmodel.CrsQuestionaryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RevokeSurveyActivity : AppCompatActivity() {
    lateinit var binding: LayoutRevokeSurveyBinding
    private val viewModel: CrsQuestionaryViewModel by viewModels()

    companion object {
        @JvmStatic
        fun createIntent(context: Context, farmerId: String): Intent {
            return Intent(context, RevokeSurveyActivity::class.java)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, com.olam.digital.crs.R.color.colorPrimary)
        binding = LayoutRevokeSurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            setUi()
            setObserver()
        }
    }

    private fun setUi() {
        binding.apply {
            viewModel.apply {
                getAllFarmersCrs()
            }
        }
    }

    private fun setObserver() {
        binding.apply {
            viewModel.apply {
                farmersCrs.observe(this@RevokeSurveyActivity)
                {
                    when (it.state) {
                        ResourceState.ERROR -> {}
                        ResourceState.LOADING -> {}
                        ResourceState.NO_DATA -> {}
                        ResourceState.SUCCESS -> {
                        //add Farmer to spinner adapter
                        }
                    }
                }





            }
        }
    }


}