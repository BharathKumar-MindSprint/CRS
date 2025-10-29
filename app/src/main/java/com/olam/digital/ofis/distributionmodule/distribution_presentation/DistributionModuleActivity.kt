package com.olam.digital.ofis.distributionmodule.distribution_presentation

import android.os.Bundle
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.olam.digital.ofis.BaseListActivity
import com.olam.digital.ofis.R
import com.olam.digital.ofis.databinding.ActivityDistributionModuleBinding
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.FarmerAndStakeHolderDto
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DistributionModuleActivity : BaseListActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityDistributionModuleBinding
    private lateinit var viewModel: DistributionModuleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDistributionModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        viewModel = ViewModelProvider(this)[DistributionModuleViewModel::class.java]

        val targetFragment = intent.getStringExtra("fromSync")
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (targetFragment != null) {
            val farmerId = intent.getStringExtra("farmerId")
            val stakeHolderId = intent.getStringExtra("stakeHolderId")
            val name = intent.getStringExtra("name")
            val olamFarmerId = intent.getStringExtra("olamFarmerId")
            val tempId = intent.getStringExtra("tempId");
            viewModel.isFromSyncScreen.value = true
            viewModel.onEvent(DistributionModuleUiEvent.setFarmerAndStakeHolderData(
                FarmerAndStakeHolderDto(name = name ?: "", id = farmerId ?: "", displayId = olamFarmerId ?: "", stakeHolderId = stakeHolderId),
                tempId
            ))
            navController.navigate(R.id.inputOrFinanceDistribution)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val navDestination = navController!!.currentDestination
            if (navDestination != null) {
                return when (navDestination.id) {
                    R.id.findFarmerAndStakeHolder -> {
                        onBackPressed()
                        true
                    }
                    R.id.distributionSummaryFragment -> {
                        navController!!.popBackStack()
                        true
                    }
                    R.id.inputOrFinanceDistribution -> {
                        false
                    }
                    else -> {
                        super.onOptionsItemSelected(item)
                    }
                }
            } else {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}