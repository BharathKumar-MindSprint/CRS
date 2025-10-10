package com.olam.digital.ofis.distributionmodule.distribution_domain.use_case

import com.olam.digital.ofis.distributionmodule.distribution_domain.model.FarmerAndStakeHolderDto
import com.olam.digital.ofis.distributionmodule.distribution_domain.repository.DistributionModuleRepository
import com.olam.digital.ofis.distributionmodule.distribution_presentation.EntityEnum

class SearchFarmerOrStakeHolder(
    private val repository: DistributionModuleRepository
) {
    suspend operator fun invoke(
        query: String,
        farmerGroupId : List<Int>,
        sectionId : List<Int>,
        entityName : EntityEnum,
        roleId : Int

    ) : Result<List<FarmerAndStakeHolderDto>> {
        return repository.searchFarmerOrStakeHolder(query, farmerGroupId, sectionId, entityName, roleId)
    }

}