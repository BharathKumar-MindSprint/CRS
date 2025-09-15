package com.olam.digital.ofis.distributionmodule.distribution_presentation

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.olam.digital.ofis.BaseListActivity
import com.olam.digital.ofis.R
import com.olam.digital.ofis.databinding.ActivityDistributionModuleBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DistributionModuleActivity : BaseListActivity() {
    private var navController: NavController? = null
    private lateinit var binding: ActivityDistributionModuleBinding
    private val viewModel : DistributionModuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDistributionModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
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
                    R.id.inputOrFinanceDistribution, R.id.distributionSummaryFragment -> {
                        navController!!.popBackStack()
                        true
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