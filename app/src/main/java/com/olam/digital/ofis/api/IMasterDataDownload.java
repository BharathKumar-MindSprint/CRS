package com.olam.digital.ofis.api;

import com.olam.digital.ofis.Common.Constants.IAPIConstants;
import com.olam.digital.ofis.mvvmmodule.model.master.MasterDataResponse;
import com.olam.digital.ofis.mvvmmodule.model.master.MasterResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface IMasterDataDownload {

    @GET(IAPIConstants.COUNTRY_SPINNERS)
    Call<MasterResponse> getMasterData( @Header("App-Id") int appId);

}
