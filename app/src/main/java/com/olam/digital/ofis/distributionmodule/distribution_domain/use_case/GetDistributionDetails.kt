package com.olam.digital.ofis.distributionmodule.distribution_domain.use_case

import com.olam.digital.ofis.distributionmodule.distribution_domain.model.DistributionFormDto
import com.olam.digital.ofis.distributionmodule.distribution_domain.repository.DistributionModuleRepository

class GetDistributionDetails(
    private val repository: DistributionModuleRepository
) {

    suspend operator fun invoke(distributionId : String?) : Result<DistributionFormDto> {
        return repository.getDistributionDetails(distributionId)
    }
}