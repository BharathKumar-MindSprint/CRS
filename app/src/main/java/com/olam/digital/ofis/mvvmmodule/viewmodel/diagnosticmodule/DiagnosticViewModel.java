package com.olam.digital.ofis.mvvmmodule.viewmodel.diagnosticmodule;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ResponseModel;

public class DiagnosticViewModel extends AndroidViewModel {
    private DiagnosticModuleRepository diagnosticModuleRepository;
    private LiveData<ResponseModel> responseLiveData;


    public DiagnosticViewModel(@NonNull Application application) {
        super(application);

        diagnosticModuleRepository = new DiagnosticModuleRepository(application);
        this.responseLiveData = diagnosticModuleRepository.getAllDiagnosticModuleData(1);
    }

    public LiveData<ResponseModel> responseLiveData() {
        return responseLiveData;
    }
}
