package com.olam.digital.ofis.mvvmmodule.viewmodel.diagnosticmodule;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.olam.digital.ofis.RetrofitServices.ManageRecords;
import com.olam.digital.ofis.RetrofitServices.RetrofitClient;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiagnosticModuleRepository {
    private static final String TAG = DiagnosticModuleRepository.class.getSimpleName();
    private ManageRecords manageRecords;
    public DiagnosticModuleRepository(Application application) {
        manageRecords = RetrofitClient.getRetrofit().create(ManageRecords.class);
    }

    public LiveData<ResponseModel> getAllDiagnosticModuleData(Integer id) {
        final MutableLiveData<ResponseModel> data = new MutableLiveData<>();
        Call<ResponseModel> call = manageRecords.getAllDiagnosticModuleData();

        call.enqueue(new Callback<ResponseModel>() {

                    @Override
                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                        Log.d(TAG, "onResponse response:: " + response);

                        if (response.body() != null) {
                            data.setValue(response.body());

                            Log.d(TAG, "diagnostic total result:: " + response.body().getDiagnosticModules().toString());
//                            Log.d(TAG, "diagnostic values:: " + response.body().getDiagnosticModules().get(0).getAnswerTypes());

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
