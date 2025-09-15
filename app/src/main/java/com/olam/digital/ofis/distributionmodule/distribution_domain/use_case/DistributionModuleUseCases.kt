package com.olam.digital.ofis.distributionmodule.distribution_domain.use_case

data class DistributionModuleUseCases(
    val searchFarmerOrStakeHolder: SearchFarmerOrStakeHolder,
    val saveDistributionForm: SaveDistributionForm,
    val getSpinnerData: GetSpinnerData,
    val getDistributionDetails : GetDistributionDetails
)
