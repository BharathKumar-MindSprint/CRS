package com.olam.digital.ofis.RetrofitServices;

import com.olam.digital.ofis.Common.Constants.IAPIConstants;
import com.olam.digital.ofis.farmplandownload.model.AccessTokenResp;
import com.olam.digital.ofis.model.checkVersion.CheckVersionRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Master {

    @POST(IAPIConstants.CHECK_APP_VERSION)
    Call<AccessTokenResp> checkAppVersion(@Body CheckVersionRequest checkVersionRequest);
}
