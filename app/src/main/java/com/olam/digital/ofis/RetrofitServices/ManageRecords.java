package com.olam.digital.ofis.RetrofitServices;

import com.olam.digital.ofis.ManageTrainingRecords.model.Assets;
import com.olam.digital.ofis.ManageTrainingRecords.model.Course;
import com.olam.digital.ofis.ManageTrainingRecords.model.DocumentList;
import com.olam.digital.ofis.ManageTrainingRecords.model.Languages;
import com.olam.digital.ofis.ManageTrainingRecords.model.Module;
import com.olam.digital.ofis.ManageTrainingRecords.model.ViewFile;
import com.olam.digital.ofis.farmplandownload.model.AccessTokenReqBody;
import com.olam.digital.ofis.farmplandownload.model.AccessTokenResp;
import com.olam.digital.ofis.farmplandownload.model.DownloadFormList;
import com.olam.digital.ofis.farmplandownload.model.DownloadUrlRequestBody;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DMSurveyResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.followup.FollowUpResponseModel;

import org.json.JSONArray;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ManageRecords {

    @GET("ofistraining/ofis/mobile/course/master/allcourses")
    Call<Course> getAllCourses(@Query("enumeratorLoginId") String enumeratorLoginId);

    @GET("ofistraining/ofis/mobile/course/module")
    Call<Module> getAllModules(@Query("id") int id);

    @GET("ofistraining/ofis/mobile/course/master/courselanguage")
    Call<Languages> getAllLanguages(@Query("coursesTrainingModuleId") int courseTrainingModuleId);

    @GET("ofistraining/ofis/mobile/course/module/assettype/language/files")
    Call<DocumentList> getAllDocuments(@Query("coursesAssetTypeId") int coursesAssetTypeId
            , @Query("coursesLanguageId") int coursesLanguageId);

    @GET("ofistraining/ofis/mobile/course/master/assetType")
    Call<Assets> getAllAsset(@Query("courseLanguageId") int courseLanguageId);

    @GET("ofistraining/file/stream/{courseLanguageDocId}")
    Call<ViewFile> getBytes(@Path("courseLanguageDocId") int courseLanguageDocId);

    @POST("ofismaster/master/externalLogin")
    Call<AccessTokenResp> getAccessToken(@Body AccessTokenReqBody accessTokenReqBody);


    @GET("ofisreports/reports/farmplan")
    Call<DownloadFormList> getAllFarmFormList(@Header("App-Id") int AppId
            , @Query("userId") String userId
            , @Query("enumeratorId") Integer enumeratorId
            , @Query("odAccessToken") String odAccessToken);


    @Headers({"Content-Type: application/json"})
    @POST("storageconnect/api/v1/common")
    Call<ResponseBody> getDownloadUrl(@Body DownloadUrlRequestBody downloadUrlRequestBody);

    //Diagnostic Module
    @GET("ofissurvey/survey/diagnostic/getAllDiagnosticModuleData/{enumerator}")
    Call<ResponseModel> getAllDiagnosticModuleData(@Path("enumerator") Integer enumeratorId);

    @POST("ofissurvey/survey/diagnosticmodule/submitSurvey")
    Call<DMSurveyResponseModel> postAllDiagnosticModuleSurveyData(@Header("App-Id") int AppId,
                                                                  @Body String diagnosticModuleSurveyRequestBody);

    @GET("ofissurvey/survey/diagnosticmodule/getAllDiagnosticModuleData/{enum_id}")
    Call<FollowUpResponseModel> getAllFollowUpDMData(@Path("enum_id") Integer enumeratorId);

}
