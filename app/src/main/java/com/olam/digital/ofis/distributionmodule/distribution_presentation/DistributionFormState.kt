package com.olam.digital.ofis.distributionmodule.distribution_presentation

import android.graphics.Bitmap
import android.net.Uri

data class DistributionFormState(
    var id : Int? = null,
    var tempId : String? = null,
    var distributionCategory : CategoryEnum = CategoryEnum.INPUT,
    var distributionGroupName : String = "",
    var distributionItemName : String = "",
    var distributionGroupId : String = "",
    var distributionItemId : String = "",
    var categoryId : Int = 1,
    var transactionValue : String = "",
    var pricePerUnit : String = "",
    var totalAmount : String = "",
    var localUnitId : Int? = null,
    var currencyId : Int? = null,
    var uomName : String = "",
    var evidenceUri : Uri? = null,
    var evidenceName : String = "",
    var signatureFileName : String = "",
    var signatureBitmap: Bitmap? = null,
    var signatureUrl : String = ""
)
