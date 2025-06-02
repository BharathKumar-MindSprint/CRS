package com.olam.digital.ofis.RetrofitServices;

import com.olam.digital.ofis.Common.Constants.IAPIConstants;
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
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.APICall.LocalUnitResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DMSurveyResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.followup.FollowUpResponseModel;
import com.olam.digital.ofis.mvvmmodule.model.photocheckbox.ResponseModel.PhotoCheckBoxResponseModel;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.APICall.PolygonHistoryResponseModel;

import okhttp3.RequestBody;
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

    @GET(IAPIConstants.GET_ALL_COURSES)
    Call<Course> getAllCourses();

    @GET(IAPIConstants.GET_ALL_MODULES)
    Call<Module> getAllModules(@Query("id") int id);

    @GET(IAPIConstants.GET_COURSE_LANGUAGE)
    Call<Languages> getAllLanguages(@Query("coursesTrainingModuleId") int courseTrainingModuleId);

    @GET(IAPIConstants.GET_ALL_DOCUMENTS)
    Call<DocumentList> getAllDocuments(@Query("coursesAssetTypeId") int coursesAssetTypeId
            , @Query("coursesLanguageId") int coursesLanguageId);

    @GET(IAPIConstants.GET_ALL_COURSE_ASSET)
    Call<Assets> getAllAsset(@Query("courseLanguageId") int courseLanguageId);

    @GET(IAPIConstants.GET_COURSE_FILE)
    Call<ViewFile> getBytes(@Path("courseLanguageDocId") int courseLanguageDocId);

    @POST(IAPIConstants.EXTERNAL_LOGIN)
    Call<AccessTokenResp> getAccessToken(@Body AccessTokenReqBody accessTokenReqBody);


    @GET(IAPIConstants.GET_FARM_PLAN)
    Call<DownloadFormList> getAllFarmFormList(@Header("App-Id") int AppId
            , @Query("userId") String userId
            , @Query("enumeratorId") Long enumeratorId
            , @Query("odAccessToken") String odAccessToken);


    @Headers({"Content-Type: application/json"})
    @POST(IAPIConstants.STORAGE_CONNECTOR_DOWNLOAD)
    Call<ResponseBody> getDownloadUrl(@Body DownloadUrlRequestBody downloadUrlRequestBody);

    //Diagnostic Module
    @GET(IAPIConstants.GET_ALL_DIAGNOSTIC_MODULE_DATA)
    Call<ResponseModel> getAllDiagnosticModuleData();

    @POST(IAPIConstants.SUBMIT_DM_SURVEY)
    Call<DMSurveyResponseModel> postAllDiagnosticModuleSurveyData(@Header("App-Id") int AppId,
                                                                  @Body String diagnosticModuleSurveyRequestBody);
    @GET(IAPIConstants.GET_ALL_DM_FOLLOWUP_DATA)
    Call<FollowUpResponseModel> getAllFollowUpDMData();

    @GET(IAPIConstants.GET_LOCAL_UNIT_MASTER)
    Call<LocalUnitResponseModel> getAllLocalUnitResponseModel();

    @GET(IAPIConstants.GET_POLYGON_DATA)
    Call<PolygonHistoryResponseModel> getAllPolygonHistoryResponse(@Path("farmer_id") Integer farmerId, @Header("App-Id") int appId);

    @POST(IAPIConstants.GET_ALL_PHOTOCHECKBOX_IMAGES)
    Call<PhotoCheckBoxResponseModel> getAllPhotoCheckBoxData(@Header("App-Id") int appId, @Query(value = "syncDate") String syncDate, @Body RequestBody photoModuleList);


}
