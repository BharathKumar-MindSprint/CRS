package com.olam.digital.crs.di.endpoints

import com.olam.digital.crs.di.IAPIConstants
import com.olam.digital.crs.model.PhotoImageDownloadRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface WebEndPoints {
    @Streaming
    @POST(IAPIConstants.STORAGE_CONNECTOR_DOWNLOAD)
    @Headers("Accept: application/octet-stream")
    suspend fun downloadSurveyImage(
        @Header("Authorization") bearerToken: String,
        @Body body: PhotoImageDownloadRequestBody
    ): Response<ResponseBody>
}