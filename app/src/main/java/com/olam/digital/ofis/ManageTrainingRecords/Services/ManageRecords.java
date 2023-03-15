package com.olam.digital.ofis.ManageTrainingRecords.Services;

import com.olam.digital.ofis.ManageTrainingRecords.model.Assets;
import com.olam.digital.ofis.ManageTrainingRecords.model.Course;
import com.olam.digital.ofis.ManageTrainingRecords.model.DocumentList;
import com.olam.digital.ofis.ManageTrainingRecords.model.Languages;
import com.olam.digital.ofis.ManageTrainingRecords.model.Module;
import com.olam.digital.ofis.ManageTrainingRecords.model.ViewFile;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
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
                                       ,@Query("coursesLanguageId") int coursesLanguageId);

    @GET("ofistraining/ofis/mobile/course/master/assetType")
    Call<Assets> getAllAsset(@Query("courseLanguageId") int courseLanguageId);

    @GET("ofistraining/file/stream/{courseLanguageDocId}")
    Call<ViewFile> getBytes(@Path("courseLanguageDocId") int courseLanguageDocId);

}
