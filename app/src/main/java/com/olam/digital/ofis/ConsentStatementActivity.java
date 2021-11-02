package com.olam.digital.ofis;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

public class ConsentStatementActivity extends BaseActivity implements View.OnClickListener {
    LinearLayout  consentStatement1,consentStatement2,consentStatement3,consentStatement4,consentStatement5;
    AppCompatCheckBox cb_sc7,cb_sc1,cb_sc2,cb_sc3,cb_sc4,cb_sc5,cb_sc6;
    EditText childname,occupationET;
    TextView et_farmername;
    Button nextsavebtn,prevbtn;
    Boolean reachedbottom = false;
    Long farmerId;
    String farmername,olamfarmername, childrenname, displayFarmerId, farmerSign = "", staffSign = "",type;
    String occupation="";
    TextView farmer_name,currentdate,currentdate2,enum_name,farmer_signature,enum_signature;
    int page = 1;
    ScrollView scrollView;
    DataLayer dataLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consentstatement);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dataLayer = new DataLayer(context.getResources(), context);
           try {
               farmerId=getIntent().getExtras().getLong("FarmerId");
               displayFarmerId=getIntent().getExtras().getString("displayFarmerId");
               olamfarmername=getIntent().getExtras().getString("olamfarmername");
               type=getIntent().getExtras().getString("type");
           }catch (Exception e){}
        init();
    }

    private void init() {
        consentStatement1 = (LinearLayout) findViewById(R.id.consentStatement1);
        consentStatement2 = (LinearLayout) findViewById(R.id.consentStatement2);
        consentStatement3 = (LinearLayout) findViewById(R.id.consentStatement3);
        consentStatement4 = (LinearLayout) findViewById(R.id.consentStatement4);
        consentStatement5 = (LinearLayout) findViewById(R.id.consentStatement5);
        cb_sc1 = (AppCompatCheckBox) findViewById(R.id.cb_sc1);
        cb_sc2 = (AppCompatCheckBox) findViewById(R.id.cb_sc2);
        cb_sc3 = (AppCompatCheckBox) findViewById(R.id.cb_sc3);
        cb_sc4 = (AppCompatCheckBox) findViewById(R.id.cb_sc4);
        cb_sc5 = (AppCompatCheckBox) findViewById(R.id.cb_sc5);
        cb_sc6 = (AppCompatCheckBox) findViewById(R.id.cb_sc6);
        cb_sc7 = (AppCompatCheckBox) findViewById(R.id.cb_sc7);
        et_farmername = (TextView) findViewById(R.id.et_farmername);
        childname = (EditText) findViewById(R.id.childname);
        occupationET=(EditText) findViewById(R.id.occupationET);
        farmer_name = (TextView) findViewById(R.id.farmer_name);
        currentdate = (TextView) findViewById(R.id.currentdate);
        currentdate2 = (TextView) findViewById(R.id.currentdate2);
        enum_name = (TextView) findViewById(R.id.enum_name);
        farmer_signature = (TextView) findViewById(R.id.farmer_signature);
        enum_signature = (TextView) findViewById(R.id.enum_signature);
        nextsavebtn = (Button) findViewById(R.id.nextsavebtn);
        prevbtn = (Button) findViewById(R.id.prevbtn);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        nextsavebtn.setOnClickListener(this);
        prevbtn.setOnClickListener(this);
        farmer_signature.setOnClickListener(this);
        enum_signature.setOnClickListener(this);
        loadpage();
        nextsavebtn.setClickable(false);
        et_farmername.setText(olamfarmername);
       // nextsavebtn.setBackgroundResource(R.drawable.border);
        disableBtn(nextsavebtn);
        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                if(isVisible){
                    nextsavebtn.setClickable(true);
                  //  nextsavebtn.setBackgroundResource(R.color.ofis_action_button_background);
                    enableSaveBtn(nextsavebtn);
                }
            }
        });
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView.getChildAt(0).getBottom()
                        <= (scrollView.getHeight() + scrollView.getScrollY())) {
                    //scroll view is at bottom
                    nextsavebtn.setClickable(true);
                    reachedbottom = true;
                  //  nextsavebtn.setBackgroundResource(R.color.ofis_action_button_background);
                    enableSaveBtn(nextsavebtn);
                } else {
                    //scroll view is not at bottom
                    if(! reachedbottom && page <= 3) {
                        nextsavebtn.setClickable(false);
                       // nextsavebtn.setBackgroundResource(R.drawable.border);
                        disableBtn(nextsavebtn);
                    }else {
                        nextsavebtn.setClickable(true);
                       // nextsavebtn.setBackgroundResource(R.color.ofis_action_button_background);
                        enableSaveBtn(nextsavebtn);
                    }
                }
            }
        });
        addingGlobalLayoutListener();

    }

    private  void enableSaveBtn(Button btn){
        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btnColorPrimary));
        btn.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void disableBtn(Button btn){
        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_primary_disabled));
        btn.setTextColor(ContextCompat.getColor(this, R.color.btn_disabled_text));
    }
    private void addingGlobalLayoutListener() {
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int childHeight = ((LinearLayout) findViewById(R.id.bottomSection)).getHeight();
                boolean isScrollable = scrollView.getHeight() < childHeight + scrollView.getPaddingTop() + scrollView.getPaddingBottom();
                if (!isScrollable) {
                    nextsavebtn.setClickable(true);
                  //  nextsavebtn.setBackgroundResource(R.color.ofis_action_button_background);
                    enableSaveBtn(nextsavebtn);
                }
            }
        });
    }

    private void loadpage() {

        if(page == 1){
            prevbtn.setVisibility(View.GONE);
            consentStatement1.setVisibility(View.VISIBLE);
            consentStatement2.setVisibility(View.GONE);
            consentStatement3.setVisibility(View.GONE);
            consentStatement4.setVisibility(View.GONE);
            consentStatement5.setVisibility(View.GONE);
            nextsavebtn.setText(getResources().getString(R.string.ModuleNextButton));
        }else if(page == 2){
            prevbtn.setVisibility(View.VISIBLE);
            consentStatement1.setVisibility(View.GONE);
            consentStatement2.setVisibility(View.VISIBLE);
            consentStatement3.setVisibility(View.GONE);
            consentStatement4.setVisibility(View.GONE);
            consentStatement5.setVisibility(View.GONE);
            nextsavebtn.setText(getResources().getString(R.string.ModuleNextButton));
        }else if (page == 3){
            prevbtn.setVisibility(View.VISIBLE);
            consentStatement1.setVisibility(View.GONE);
            consentStatement2.setVisibility(View.GONE);
            consentStatement3.setVisibility(View.VISIBLE);
            consentStatement4.setVisibility(View.GONE);
            consentStatement5.setVisibility(View.GONE);
            nextsavebtn.setText(getResources().getString(R.string.ModuleNextButton));
        }else if (page == 4){
            prevbtn.setVisibility(View.VISIBLE);
            consentStatement1.setVisibility(View.GONE);
            consentStatement2.setVisibility(View.GONE);
            consentStatement3.setVisibility(View.GONE);
            consentStatement4.setVisibility(View.VISIBLE);
            consentStatement5.setVisibility(View.GONE);
            nextsavebtn.setText(getResources().getString(R.string.ModuleNextButton));
        }else if (page == 5){
            prevbtn.setVisibility(View.VISIBLE);
            consentStatement1.setVisibility(View.GONE);
            consentStatement2.setVisibility(View.GONE);
            consentStatement3.setVisibility(View.GONE);
            consentStatement4.setVisibility(View.GONE);
            consentStatement5.setVisibility(View.VISIBLE);
            nextsavebtn.setText(getResources().getString(R.string.confirm));
        }
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.nextsavebtn){
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            if(page <= 5 && page >= 1) {
                if (page != 5) {
                    if(page == 2){
                        if(!cb_sc1.isChecked() || !cb_sc3.isChecked()  || !cb_sc4.isChecked() ||
                                !cb_sc5.isChecked() || !cb_sc6.isChecked() || !cb_sc7.isChecked() ){
                            Toast.makeText(ConsentStatementActivity.this, "Please click all the appropriate checkboxes", Toast.LENGTH_LONG).show();
                            return;
                        }
                        nextsavebtn.setClickable(false);
                      //  nextsavebtn.setBackgroundResource(R.drawable.border);
                        disableBtn(nextsavebtn);
                        addingGlobalLayoutListener();
                    }
                    if(page == 3){
                        if(et_farmername.getText().toString().matches("")){
                            et_farmername.getBackground().setColorFilter(getResources().getColor(android.R.color.holo_red_light),
                                    PorterDuff.Mode.SRC_ATOP);
                            Toast.makeText(ConsentStatementActivity.this, getResources().getString(R.string.please_fill_farmer_name), Toast.LENGTH_LONG).show();
                            return;
                        }else {
//                            nextsavebtn.setClickable(false);
//                            nextsavebtn.setBackgroundResource(R.drawable.border);
                            farmername = et_farmername.getText().toString();
                            farmer_name.setText(farmername);
                            Date today = new Date();
                            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                            String date = format.format(today);
                            currentdate.setText(date);
                            currentdate2.setText(date);
                            enum_name.setText(Utilities.getEnumeratorNameSharedPrefs(context));
                            addingGlobalLayoutListener();
                        }
                    }
                    if(page == 4){
                        childrenname = childname.getText().toString();
                        occupation = occupationET.getText().toString();
                        if(farmerSign.matches("")){
                            Toast.makeText(ConsentStatementActivity.this, getResources().getString(R.string.farmer_sign_required), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(staffSign.matches("")){
                            Toast.makeText(ConsentStatementActivity.this, getResources().getString(R.string.staff_sign_required), Toast.LENGTH_LONG).show();
                            return;
                        }
//                        nextsavebtn.setClickable(false);
//                        nextsavebtn.setBackgroundResource(R.drawable.border);
                        addingGlobalLayoutListener();
                    }
                    page++;
                    reachedbottom = false;
                    loadpage();
                    System.out.println("page value:" + page);
                }


                if(page == 5){
                    page++;
                    return;
                }

            }else if(page == 6){
                nextsavebtn.setClickable(false);
                ContentValues contentValue=new ContentValues();
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER_ID,farmerId);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER,farmername);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_DISPLAYFARMER_ID,displayFarmerId);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CHILDNAME,childrenname);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_OCCUPATION,occupation);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER_SIGN,farmerSign);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_ENUM_SIGN,staffSign);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER_CONFIRMATION,true);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_OLAM_FARMER_NAME,olamfarmername);
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String registereddate = format.format(today);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CONFIRMED_DATE,registereddate);
                dataLayer.insertConsentStatementDetails(contentValue,olamfarmername,type);
                if(type.equalsIgnoreCase("update")){
                    dataLayer.updateConsentStatus(farmerId);
                }
                Intent intent=new Intent();
                intent.putExtra("STATUS",true);
                setResult(2,intent);
                finish();

            }
            addingGlobalLayoutListener();
        }else if(id == R.id.prevbtn){
            page--;
            loadpage();
            scrollView.scrollTo(0, scrollView.getBottom());
            addingGlobalLayoutListener();
            System.out.println("page value:" + page);
        }else if(id == R.id.farmer_signature){
            showSignatureDialog(getResources().getString(R.string.farmer));
        }else if(id == R.id.enum_signature){
            showSignatureDialog(getResources().getString(R.string.enum_name));
        }
    }


    boolean isSigned = false;
    public void showSignatureDialog(String signedby) {
        isSigned = false;
        final Dialog signatureDialog = new Dialog(this, android.R.style.Theme_Material_Dialog_Alert);
        signatureDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        signatureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        signatureDialog.setContentView(R.layout.dialog_signature);
        SignaturePad mSignaturePad = signatureDialog.findViewById(R.id.signature_pad);
        Button bt_clear = (Button)signatureDialog.findViewById(R.id.imgv_clear_signature);
        Button bt_confirm = (Button)signatureDialog.findViewById(R.id.btn_add_signature);
        bt_confirm.setEnabled(true);
        bt_clear.setEnabled(true);

        bt_clear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mSignaturePad.clear();
                return false;
            }
        });
        bt_clear.findViewById(R.id.imgv_clear_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignaturePad.clear();
            }
        });
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                //Event triggered when the pad is touched
                isSigned = false;
            }

            @Override
            public void onSigned() {
                //Event triggered when the pad is signed
                isSigned = true;
            }

            @Override
            public void onClear() {
                //Event triggered when the pad is cleared
                isSigned = false;
            }
        });

        signatureDialog.findViewById(R.id.btn_cancel_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  signatureDialog.dismiss();
            }
        });

        bt_confirm.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                {
                    try {
                        if (isSigned) {
//                        farmerSignatureButton.setImageBitmap(mSignaturePad.getTransparentSignatureBitmap());
                            File photoFile = null;
                            try {
                                photoFile = createImageFile("signature");
                            } catch (IOException e) {
                                FirebaseCrash.report(new Exception("AddFarmerActivity: Error creating signature image file", e));
                                e.printStackTrace();
                            }
                            if (photoFile != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    try {
                                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                                        m.invoke(null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                Uri fileUri = Uri.fromFile(photoFile);
                                if(signedby.equalsIgnoreCase(getResources().getString(R.string.farmer))){
                                    farmerSign = fileUri.getPath();
                                }else {
                                    staffSign = fileUri.getPath();
                                }

                                try (FileOutputStream out = new FileOutputStream(photoFile)) {
                                    mSignaturePad.getTransparentSignatureBitmap().compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                    // PNG is a lossless format, the compression factor (100) is ignored
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            signatureDialog.dismiss();
                         } else {
                            Toast.makeText(ConsentStatementActivity.this, "No signature available...", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        signatureDialog.show();
    }

    private File createImageFile(String portraitOrSignature) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = (portraitOrSignature.equalsIgnoreCase("portrait")) ? "ofis_farmer_portrait_" + timestamp + "_" : "ofis_farmer_signature_" + timestamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }
}
