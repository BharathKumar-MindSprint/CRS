package com.olam.digital.crs.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable
@Keep
data class PhotoImageDownloadRequestBody(
    @SerializedName("rootobject")
    var rootobject: String? = null,
    @SerializedName("filepath")
    var filepath: String? = null
): Serializable