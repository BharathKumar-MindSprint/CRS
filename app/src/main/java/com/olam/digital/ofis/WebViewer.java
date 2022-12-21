package com.olam.digital.ofis;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.github.barteksc.pdfviewer.PDFView;
import com.olam.digital.ofis.downloadsurvey.dto.ModuleDTO;
import com.olam.digital.ofis.model.DocumentList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebViewer extends BaseActivity {
    PDFView pdfView;
    Integer Docid;
    String Title;
    int  intValue;
    RelativeLayout Progressbar;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_viewer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            DocumentList.data document = ( DocumentList.data) bundle.getSerializable("DocumentList");
            Title=document.getFileName();
            intValue=document.gettDocsLanId();
        }
        if(Title!=null){
            setTitle(Title);
        }else{
            setTitle(getString(R.string.document_viewer));
        }
        Docid=intValue;
        pdfView = findViewById(R.id.idPDFView);
        Progressbar=findViewById(R.id.Progressbar);
        Progressbar.setVisibility(View.VISIBLE);
        if(intValue>0){
           ManageRecords Bytes=RetrofitClient.getRetrofit().create(ManageRecords.class);
           Call<ResponseBody> call=Bytes.getBytes(intValue);
           call.enqueue(new Callback<ResponseBody>() {
               @Override
               public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                   if(response.isSuccessful()){
                       Progressbar.setVisibility(View.INVISIBLE);
                       pdfView.fromStream(response.body().byteStream())
                               .enableAnnotationRendering(true)
                               .enableDoubletap(true)
                               .spacing(10)// in dp
                               .load();
                   }else{
                       Progressbar.setVisibility(View.INVISIBLE);
                       showAlert(getString(R.string.failed_to_load_document));
                   }
               }

               @Override
               public void onFailure(Call<ResponseBody> call, Throwable t) {
                   t.printStackTrace();

               }
           });
       }
    }
    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle(getString(R.string.Alert_txt));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) (dialog, which) -> {
            Intent intent = new Intent(context, ManagingTrainingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}


