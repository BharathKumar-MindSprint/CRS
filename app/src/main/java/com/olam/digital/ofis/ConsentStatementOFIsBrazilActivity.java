package com.olam.digital.ofis;

import android.app.Dialog;
import android.content.ContentValues;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConsentStatementOFIsBrazilActivity extends BaseActivity implements View.OnClickListener {
    LinearLayout  consentStatement1,consentStatement2,consentStatement3,consentStatement4,consentStatement5;
    AppCompatCheckBox cb_sc7,cb_sc1,cb_sc2,cb_sc3,cb_sc4,cb_sc5,cb_sc6,cb_sc8;
    EditText childname,occupationET;
    TextView et_farmername, et_farmername_first;
    Button nextsavebtn,prevbtn;
    Boolean reachedbottom = false;
    Long farmerId;
    String farmername,olamfarmername, childrenname, displayFarmerId, farmerSign = "", staffSign = "",witness1_sign = "",witness2_sign = "",type;
    String occupation="";
    TextView farmer_name,currentdate,currentdate2,enum_name,farmer_signature,enum_signature,
            witness1_signature, witness2_signature;
    int page = 1;
    ScrollView scrollView;
    DataLayer dataLayer;

    EditText et_maritalstatus,et_profession, et_cpfNumber, et_rgNumber,et_fulladdress,et_city,et_state, et_subname,
            et_subnationality, et_submaritalsts, et_subProfessinal, et_subCpf, et_subRg, et_subAddress, et_subCity,
            et_subState, dateEditT, contactOfi, witness1Name, witness1CPF, witness1RG ,witness2Name, witness2CPF, witness2RG;

    String maritalstatus,profession, cpfNumber, rgNumber,fulladdress,city,state, subname,
            subnationality, submaritalsts, subProfessinal, subCpf, subRg, subAddress, subCity,
            subState, contactOfiStr, witness1NameStr, witness1CPFStr, witness1RGStr ,witness2NameStr, witness2CPFStr, witness2RGStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consentstatement_ofi_brazil);
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
        cb_sc8 = (AppCompatCheckBox) findViewById(R.id.cb_sc8);
        et_farmername = (TextView) findViewById(R.id.et_farmername);
        et_farmername_first = (TextView) findViewById(R.id.et_farmername_first);
        childname = (EditText) findViewById(R.id.childname);
        occupationET=(EditText) findViewById(R.id.occupationET);

        et_maritalstatus =(EditText) findViewById(R.id.et_maritalstatus);
        et_profession =(EditText) findViewById(R.id.et_profession);
        et_cpfNumber =(EditText) findViewById(R.id.et_cpfNumber);
        et_rgNumber =(EditText) findViewById(R.id.et_rgNumber);
        et_fulladdress =(EditText) findViewById(R.id.et_fulladdress);
        et_city =(EditText) findViewById(R.id.et_city);
        et_state =(EditText) findViewById(R.id.et_state);
        et_subname =(EditText) findViewById(R.id.et_subname);
        et_subnationality =(EditText) findViewById(R.id.et_subnationality);
        et_submaritalsts =(EditText) findViewById(R.id.et_submaritalsts);
        et_subProfessinal =(EditText) findViewById(R.id.et_subProfessinal);
        et_subCpf =(EditText) findViewById(R.id.et_subCpf);
        et_subRg =(EditText) findViewById(R.id.et_subRg);
        et_subAddress =(EditText) findViewById(R.id.et_subAddress);
        et_subCity =(EditText) findViewById(R.id.et_subCity);
        et_subState =(EditText) findViewById(R.id.et_subState);
        contactOfi =(EditText) findViewById(R.id.contactOfi);
        witness1Name =(EditText) findViewById(R.id.witness1Name);
        witness1CPF =(EditText) findViewById(R.id.witness1CPF);
        witness1RG =(EditText) findViewById(R.id.witness1RG);
        witness2Name =(EditText) findViewById(R.id.witness2Name);
        witness2CPF =(EditText) findViewById(R.id.witness2CPF);
        witness2RG =(EditText) findViewById(R.id.witness2RG);

        farmer_name = (TextView) findViewById(R.id.farmer_name);
        currentdate = (TextView) findViewById(R.id.currentdate);
        currentdate2 = (TextView) findViewById(R.id.currentdate2);
        enum_name = (TextView) findViewById(R.id.enum_name);
        farmer_signature = (TextView) findViewById(R.id.farmer_signature);
        enum_signature = (TextView) findViewById(R.id.enum_signature);
        witness1_signature = (TextView) findViewById(R.id.witness1_signature);
        witness2_signature = (TextView) findViewById(R.id.witness2_signature);
        nextsavebtn = (Button) findViewById(R.id.nextsavebtn);
        prevbtn = (Button) findViewById(R.id.prevbtn);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        nextsavebtn.setOnClickListener(this);
        prevbtn.setOnClickListener(this);
        farmer_signature.setOnClickListener(this);
        enum_signature.setOnClickListener(this);
        witness1_signature.setOnClickListener(this);
        witness2_signature.setOnClickListener(this);
        loadpage();
        nextsavebtn.setClickable(false);
        et_farmername.setText(olamfarmername+" ,");
        et_farmername_first.setText(olamfarmername+" ,");
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
        scrollView.fullScroll(ScrollView.FOCUS_UP);
        scrollView.smoothScrollTo(0,0);
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.nextsavebtn){
            if(page <= 5 && page >= 1) {
                if (page != 5) {
                    boolean nextPage = true;
                    if(page == 1){
                        maritalstatus = et_maritalstatus.getText().toString();
                        profession = et_profession.getText().toString();
                        cpfNumber = et_cpfNumber.getText().toString();
                        rgNumber = et_rgNumber.getText().toString();
                        fulladdress = et_fulladdress.getText().toString();
                        city = et_city.getText().toString();
                        state = et_state.getText().toString();
                    }
                    if(page == 2){
                        if(!cb_sc1.isChecked() || !cb_sc3.isChecked()   ||
                           !cb_sc5.isChecked() || !cb_sc6.isChecked() || !cb_sc7.isChecked()|| !cb_sc8.isChecked() ){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.please_click_checkbox), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.please_fill_farmer_name), Toast.LENGTH_LONG).show();
                            return;
                        }else {
//                            nextsavebtn.setClickable(false);
//                            nextsavebtn.setBackgroundResource(R.drawable.border);
                            farmername = (et_farmername.getText().toString()).replace(",","");
                            farmer_name.setText(farmername);
                            Date today = new Date();
                            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                            String date = format.format(today);
                            currentdate.setText(date);
                            currentdate2.setText(date);
                            enum_name.setText(Utilities.getEnumeratorNameSharedPrefs(context));
                            addingGlobalLayoutListener();

                            subname= et_subname.getText().toString();
                            subnationality = et_subnationality.getText().toString();
                            submaritalsts = et_submaritalsts.getText().toString();
                            subProfessinal = et_subProfessinal.getText().toString();
                            subCpf = et_subCpf.getText().toString();
                            subRg = et_subRg.getText().toString();
                            subAddress = et_subAddress.getText().toString();
                            subCity = et_subCity.getText().toString();
                            subState = et_subState.getText().toString();
                        }
                    }
                    if(page == 4){
                        nextPage = false;
                        subname = childname.getText().toString();
                        occupation = occupationET.getText().toString();
//                        dateStr = dateEditT.getText().toString();
                        contactOfiStr = contactOfi.getText().toString();
                        witness1NameStr = witness1Name.getText().toString();
                        witness1CPFStr = witness1CPF.getText().toString();
                        witness1RGStr = witness1RG.getText().toString();
                        witness2NameStr = witness2Name.getText().toString();
                        witness2CPFStr = witness2CPF.getText().toString();
                        witness2RGStr = witness2RG.getText().toString();


                        if(farmerSign.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.farmer_sign_required), Toast.LENGTH_LONG).show();
                            farmer_signature.requestFocus();

                        }
                        else if(staffSign.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.staff_sign_required), Toast.LENGTH_LONG).show();
                            enum_signature.requestFocus();
                        }

                        else if(witness1NameStr.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.please_enter_witness1_name), Toast.LENGTH_LONG).show();
                            witness1Name.requestFocus();
                        }
                        else if(witness1CPFStr.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.please_enter_witness1_CPF), Toast.LENGTH_LONG).show();
                            witness1CPF.requestFocus();
                        }
                        else if(witness1RGStr.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.please_enter_witness1_RG), Toast.LENGTH_LONG).show();
                            witness1RG.requestFocus();
                        }
                        else if(witness1_sign.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.witness1_sign_required), Toast.LENGTH_LONG).show();
                            witness1_signature.requestFocus();
                        }
                        else if(witness2NameStr.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.please_enter_witness2_name), Toast.LENGTH_LONG).show();
                            witness2Name.requestFocus();
                        }
                        else if(witness2CPFStr.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.please_enter_witness2_CPF), Toast.LENGTH_LONG).show();
                            witness2CPF.requestFocus();
                        }
                        else if(witness2RGStr.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.please_enter_witness2_RG), Toast.LENGTH_LONG).show();
                            witness1RG.requestFocus();
                        }
                        else if(witness2_sign.matches("")){
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, getResources().getString(R.string.witness2_sign_required), Toast.LENGTH_LONG).show();
                            witness2_signature.requestFocus();
                        }else {
                            addingGlobalLayoutListener();
                            nextPage = true;
                        }
//                        nextsavebtn.setClickable(false);
//                        nextsavebtn.setBackgroundResource(R.drawable.border);
                    }
                    if(nextPage) {
                        page++;
                        reachedbottom = false;
                        loadpage();
                    }
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

                if(! maritalstatus.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_MARITAL,maritalstatus);
                if(! profession.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_PROFESSION,profession);
                if(! cpfNumber.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CPF_NO,cpfNumber);
                if(! rgNumber.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_RG_NO,rgNumber);
                if(! fulladdress.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FULL_ADDRESS,fulladdress);
                if(! city.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CITY,city);
                if(! state.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_STATE,state);
                if(! subname.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBNAME,subname);
                if(! subnationality.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBNATIONALITY,subnationality);
                if(! submaritalsts.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBMARITAL,submaritalsts);
                if(! subProfessinal.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBPROFESSION,subProfessinal);
                if(! subCpf.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBCPF_NO,subCpf);
                if(! subRg.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBRG_NO,subRg);
                if(! subAddress.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBFULL_ADDRESS,subAddress);
                if(! subCity.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBCITY,subCity);
                if(! subState.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBSTATE,subState);
                if(! contactOfiStr.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CONTACT,contactOfiStr);
                if(! witness1NameStr.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT1_NAME,witness1NameStr);
                if(! witness1RGStr.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT1_RG,witness1RGStr);
                if(! witness1CPFStr.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT1_CPF,witness1CPFStr);
                if(! witness1_sign.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT1_SIGN,witness1_sign);
                if(! witness2NameStr.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT2_NAME,witness2NameStr);
                if(! witness2RGStr.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT2_RG,witness2RGStr);
                if(! witness2CPFStr.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT2_CPF,witness2CPFStr);
                if(! witness2_sign.isEmpty())
                    contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT2_SIGN,witness2_sign);

                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String registereddate = format.format(today);
                contentValue.put(OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CONFIRMED_DATE,registereddate);
               if(type.equalsIgnoreCase(UPDATE)){
                    dataLayer.updateConsentStatus(farmerId);
                    dataLayer.insertConsentStatementDetails(contentValue,olamfarmername,type);
                }
                Intent intent=new Intent();
                intent.putExtra(STATUS,true);
                intent.putExtra(CONSENTVALUE,contentValue);
                setResult(2,intent);
                finish();

            }
            addingGlobalLayoutListener();
        }else if(id == R.id.prevbtn){
            page--;
            loadpage();
            scrollView.scrollTo(0, scrollView.getBottom());
            addingGlobalLayoutListener();
        }else if(id == R.id.farmer_signature){
            showSignatureDialog(getResources().getString(R.string.farmer));
        }else if(id == R.id.enum_signature){
            showSignatureDialog(getResources().getString(R.string.enum_name));
        }else if(id == R.id.witness1_signature){
            showSignatureDialog("witness1");
        }else if(id == R.id.witness2_signature){
            showSignatureDialog("witness2");
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
                                }if(signedby.equalsIgnoreCase("witness1")){
                                    witness1_sign = fileUri.getPath();
                                }if(signedby.equalsIgnoreCase("witness2")){
                                    witness2_sign = fileUri.getPath();
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
                            Toast.makeText(ConsentStatementOFIsBrazilActivity.this, "No signature available...", Toast.LENGTH_SHORT).show();
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
