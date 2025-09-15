package com.olam.digital.ofis.distributionmodule.distribution_domain.use_case

import android.content.ContentValues
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.DistributionFormDto
import com.olam.digital.ofis.distributionmodule.distribution_domain.repository.DistributionModuleRepository

class SaveDistributionForm(
    private val repository: DistributionModuleRepository
) {

    suspend operator fun invoke(
        distributionFormDto: DistributionFormDto
    ) {
        repository.saveDistributionForm(distributionFormDto)
    }

    suspend fun insertNetworkQueue(
        values : ContentValues
    ) : Long {
        return repository.insertNetworkQueueEntry(values)
    }
}