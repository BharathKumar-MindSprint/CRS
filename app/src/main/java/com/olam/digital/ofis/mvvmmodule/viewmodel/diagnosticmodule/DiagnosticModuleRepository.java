package com.olam.digital.ofis.mvvmmodule.viewmodel.diagnosticmodule;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.Response;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseModel;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;

public class DiagnosticModuleRepository {
    private static final String TAG = DiagnosticModuleRepository.class.getSimpleName();
    private ManageRecords manageRecords;
    public DiagnosticModuleRepository(Application application) {
        manageRecords = RetrofitClient.getRetrofit().create(ManageRecords.class);
    }

    public LiveData<ResponseModel> getAllDiagnosticModuleData(Integer id) {
        final MutableLiveData<ResponseModel> data = new MutableLiveData<>();
        Call<ResponseModel> getAllData = manageRecords.getAllDiagnosticModuleData(1);

               getAllData.enqueue(new Callback<ResponseModel>() {

                    @Override
                    public void onResponse(Call<ResponseModel> call, retrofit2.Response<ResponseModel> response) {
                        Log.d(TAG, "onResponse response:: " + response);

                        if (response.body() != null) {
                            data.setValue(response.body());

                            Log.d(TAG, "articles total result:: " + response.body().getResponse());
                           // Log.d(TAG, "articles size:: " + response.body().getArticles().size());
                           // Log.d(TAG, "articles title pos 0:: " + response.body().getArticles().get(0).getTitle());
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
        return data;
    }
}
