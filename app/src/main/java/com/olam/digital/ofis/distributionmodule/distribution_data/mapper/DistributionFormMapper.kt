package com.olam.digital.ofis.distributionmodule.distribution_data.mapper

import com.olam.digital.ofis.distributionmodule.distribution_data.local.entity.DistributionFormEntity
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.DistributionFormDto

fun DistributionFormDto.toDistributionFormEntity() : DistributionFormEntity {
    return DistributionFormEntity(
        id = id,
        distributionId = tempId,
        distributionGroupId = distributionGroupId,
        distributionCategoryId = distributionCategoryId,
        distributionItemId = distributionItemId,
        transactionValue = transactionValue,
        localUnitId = localUnitId,
        currencyId = currencyId,
        price = price,
        totalAmount = totalAmount,
        evidenceUrl = evidenceUrl,
        signatureUrl = signatureUrl,
        farmerId = farmerId,
        stakeHolderId = stakeHolderId
    )
}