package com.olam.digital.ofis.distributionmodule.distribution_domain.di

import android.content.Context
import com.olam.digital.ofis.distributionmodule.distribution_domain.repository.DistributionModuleRepository
import com.olam.digital.ofis.distributionmodule.distribution_domain.use_case.DistributionModuleUseCases
import com.olam.digital.ofis.distributionmodule.distribution_domain.use_case.GetDistributionDetails
import com.olam.digital.ofis.distributionmodule.distribution_domain.use_case.GetSpinnerData
import com.olam.digital.ofis.distributionmodule.distribution_domain.use_case.SaveDistributionForm
import com.olam.digital.ofis.distributionmodule.distribution_domain.use_case.SearchFarmerOrStakeHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object DistributionDomainModule {

    @ViewModelScoped
    @Provides
    fun provideDistributionModuleUseCases(
        repository: DistributionModuleRepository
    ) : DistributionModuleUseCases {
        return DistributionModuleUseCases(
            SearchFarmerOrStakeHolder(repository),
            SaveDistributionForm(repository),
            GetSpinnerData(repository),
            GetDistributionDetails(repository)
        )
    }
}