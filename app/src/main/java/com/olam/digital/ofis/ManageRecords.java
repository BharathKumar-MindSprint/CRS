package com.olam.digital.ofis;

import com.olam.digital.ofis.model.Assets;
import com.olam.digital.ofis.model.Course;
import com.olam.digital.ofis.model.DocumentList;
import com.olam.digital.ofis.model.Languages;
import com.olam.digital.ofis.model.Module;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ManageRecords {

    @GET("ofistraining/ofis/mobile/course/master/allcourses")
    Call<Course> getAllCourses();

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
    Call<ResponseBody> getBytes(@Path("courseLanguageDocId") int courseLanguageDocId);

}
