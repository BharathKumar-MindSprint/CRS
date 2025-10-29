package com.olam.digital.ofis.distributionmodule.distribution_domain.use_case

import com.olam.digital.ofis.distributionmodule.distribution_domain.repository.DistributionModuleRepository
import com.olam.digital.ofis.model.SpinnerDataDto

class GetSpinnerData(
    private val repository: DistributionModuleRepository
) {

    suspend operator fun invoke() : Result<List<List<SpinnerDataDto>>> {
        return repository.getSpinnerData()
    }

    suspend fun getDistributionItems(id : String) : Result<List<SpinnerDataDto>> {
        return repository.getDistributionItems(id)
    }

    suspend fun getDistributionGroups(id : Int) : Result<List<SpinnerDataDto>> {
        return repository.getDistributionGroups(id)
    }

    suspend fun getCategory() : Result<List<SpinnerDataDto>> {
        return repository.getCategory()
    }

    suspend fun getUnitsData(): Result<List<List<SpinnerDataDto>>> {
        return repository.getUnitsData();
    }
}