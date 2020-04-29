package com.olam.digital.ofis;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.olam.digital.ofis.model.NationalIdDTO;
import com.olam.digital.ofis.recordpolygon.Utils;

import java.util.ArrayList;
import java.util.Hashtable;

public class CustomListAdapter_NationalId extends ArrayAdapter<NationalIdDTO> {

    private final Context context;
    private final static String SELECT_NATIONALID = "ID TYPE";
    ArrayList<NationalIdDTO> nidDtoList = new ArrayList<>();
    ArrayList<String> nidList = new ArrayList<>();
    private DataLayer dl;
    Resources res;
//    Map<Integer, Integer> typeMap = new HashMap<Integer, Integer>();
//    Map<Integer, String> nidvalueMap = new HashMap<Integer, String>();
    int type_c;

   /* public CustomListAdapter_NationalId(Context context, Resources res, ArrayList<NationalIdTypeDTO> nidtypelist, ArrayList<NationalIdDTO> nidDtoList, ArrayList<String> nidList, Map<Integer, Integer> typeMap, Map<Integer, String> nidvalueMap, int type_c) {
        super(context, R.layout.add_nationalid, nidDtoList);
        this.context =context;
        this.nidtypelist =nidtypelist;
        this.nidDtoList = nidDtoList;
        this.nidList = nidList;
        this.res = res;
        this.typeMap = typeMap;
        this.nidvalueMap = nidvalueMap;
        this.type_c = type_c;
    }*/

    public CustomListAdapter_NationalId(Context context, Resources res, ArrayList<String> nidList,ArrayList<NationalIdDTO> nidDtoList, int type_c) {
        super(context, R.layout.add_nationalid, nidDtoList);
        this.context =context;
        this.nidList = nidList;
        this.res = res;
        this.type_c = type_c;
        this.nidDtoList = nidDtoList;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.add_nationalid, null,true);
        dl = new DataLayer(res, context);
//        nidDtoList = bindnationaidvaluesfromdb();
        ImageView delete = view.findViewById(R.id.deleteNationalid);
        ImageButton showNIDImage = (ImageButton) view.findViewById(R.id.nationalidphoto);
        ImageButton captureimg = view.findViewById(R.id.nidphotoActionButton);
        Spinner idtypespinner = view.findViewById(R.id.idtypespinner);
        EditText nidnumber = view.findViewById(R.id.nidnumber);
        nidnumber.setFilters(new InputFilter[]{new Utils.EmojiExcludeFilter()});
        nidnumber.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        LinearLayout ll_capturephoto = view.findViewById(R.id.ll_capturephoto);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, nidList);

        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        idtypespinner.setAdapter(spinnerArrayAdapter);

        int spinnerPosition = spinnerArrayAdapter.getPosition(getNidTypebyid(Integer.parseInt(nidDtoList.get(position).getNationalidtype())));
        idtypespinner.setSelection(spinnerPosition);

//        nidnumber.setText(nidDtoList.get(position).getNationalidNumber());
        nidnumber.setText(nidDtoList.get(position).getNationalidNumber());
        captureimg.setVisibility(View.GONE);
//        idtypespinner.setEnabled(false);
//        nidnumber.setEnabled(false);

        if(nidDtoList.get(position).getPhoto() != null) {
            String farmerNIDPath = nidDtoList.get(position).getPhoto();
            if (farmerNIDPath != null && farmerNIDPath.length() > 0) {
                int baseWidth = 100;
                int baseHeight = 133;
                Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
                Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
                Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());
                Bitmap bitmap = Utilities.loadSizedBitmap(farmerNIDPath, outputWidth, outputHeight);
                showNIDImage.setImageBitmap(bitmap);
            }
        }else {
            showNIDImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_capture_id_default));
        }
        if(getViewTypeCount() == 1 || position == 0){
            delete.setVisibility(View.INVISIBLE);
        }

        /*nidnumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = idtypespinner.getSelectedItem().toString();
                if(value.equalsIgnoreCase(SELECT_NATIONALID)){
                    Toast.makeText(context,context.getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_SHORT).show();
                    return;
                }
                if (! getNidCaptureStatus(value).equalsIgnoreCase("false")) {
                    if(nidDtoList.get(position).getPhoto().isEmpty()) {
                        Toast.makeText(context,context.getResources().getString(R.string.nid_photo_select),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });*/
       delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dl.deletelocalfarmervalues(nidDtoList.get(position).getId());
                notifyDataSetChanged();
            }
        });

        captureimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = idtypespinner.getSelectedItem().toString();
                if(value.equalsIgnoreCase(SELECT_NATIONALID)){
                    Toast.makeText(context,context.getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_SHORT).show();
                    return;
                }
                String nid = nidnumber.getText().toString();
                 if(nid.trim() == "" || nid.trim().equalsIgnoreCase("")){
                    nidnumber.setSelected(true);
                    Toast.makeText(context,context.getResources().getString(R.string.nid_value_enter),Toast.LENGTH_SHORT).show();
                    return;
                }



                /*int type = getNidType(value);
                NationalIdDTO idDTO = new NationalIdDTO();
                idDTO.setNationalidNumber(nid);
                idDTO.setNationalidtype(String.valueOf(type));

                idDTO.setNationalidtype(String.valueOf(0));
                if(type_c == 1) {
                    ((AddFarmerActivity) context).captureSetImage(position, idDTO, type, nid);
                }else{
                    ((AddNationalidActivity) context).captureSetImage(position, idDTO, type, nid);
                }*/
                int id =  nidDtoList.get(position).getId();
                if(type_c == 1) {
//                    ((AddFarmerActivity) context).captureSetImage(id);
                }else{
//                    ((AddNationalidActivity) context).captureSetImage(id);
                }
            }
        });



        idtypespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = idtypespinner.getSelectedItem().toString().trim();
                int data = getNidType(value);

                if(! value.equalsIgnoreCase(SELECT_NATIONALID)) {
                    int limit = getNidLimit(value);
                    InputFilter[] filters = new InputFilter[1];
                    filters[0] = new InputFilter.LengthFilter(limit); //Filter to 10 characters
                    nidnumber.setFilters(filters);
                }
               /* if(value.equalsIgnoreCase(SELECT_NATIONALID)){
                    Toast.makeText(context,context.getResources().getString(R.string.nationalidtype_selection),Toast.LENGTH_SHORT).show();
                    return;
                }*/
                if (getNidCaptureStatus(value).equalsIgnoreCase("false")) {
                    ll_capturephoto.setVisibility(View.GONE);
                } else {
                    ll_capturephoto.setVisibility(View.VISIBLE);
                }
                if(type_c == 1) {
//                    ((AddFarmerActivity) context).addTypeintoSelectionList(value);
                }else{
//                    ((AddNationalidActivity) context).addTypeintoSelectionList(value);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getViewTypeCount() {
        if(getCount() > 0){
            return nidDtoList.size();
        }else{
            return super.getViewTypeCount();
        }

    }
    @Override
    public boolean isEmpty() {
        return false;
    }

    public String getNidTypebyid(int id) {
        String type=null;
        Cursor nid=dl.getNIDById(id);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            type= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE));
        }
        return  type;
    }

    // getting nationalids type id values
    public int getNidType(String type) {
        int id=0;
        Cursor  nid=dl.getNIDByName(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            id= nid.getInt(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_ID));
        }
        return  id;
    }

    public ArrayList<NationalIdDTO> getValues(){

        return nidDtoList;
    }

    // getting nationalids type id values
    public String getNidCaptureStatus(String type) {
        String photostatus="true";
        Cursor  nid=dl.getNIDPhotostatusByName(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            photostatus= nid.getString(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_PHOTO_REQUIRED));
        }
        return  photostatus;
    }

    // getting nationalids type id values
    public int getNidLimit(String type) {
        int id=0;
        Cursor  nid=dl.getNIDByName(type);
        if (nid.getCount() > 0) {
            nid.moveToFirst();
            id= nid.getInt(nid.getColumnIndex(OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_LIMIT));
        }
        return  id;
    }


    /*private ArrayList<NationalIdDTO> bindnationaidvaluesfromdb() {
        Cursor nids = dl.getnidsfromdb();

        ArrayList<NationalIdDTO> nidDtoList = new ArrayList<>();
        int i=0;
        while (nids.moveToNext()) {
            NationalIdDTO nationalIdDTO = new NationalIdDTO();
            nationalIdDTO.setId(nids.getColumnIndex(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NAME_ID));
            nationalIdDTO.setNationalidtype(String.valueOf(nids.getColumnIndex(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_TYPE)));
            nationalIdDTO.setNationalidNumber(nids.getString(nids.getColumnIndex(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_NUMBER)));
            String farmerNIDPath = nids.getString(nids.getColumnIndex(OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_PHOTO));
            if (farmerNIDPath != null && farmerNIDPath.length() > 0) {
                nationalIdDTO.setPhoto(farmerNIDPath);
            }else{
                nationalIdDTO.setPhoto("");
            }
            nidDtoList.add(nationalIdDTO);
            i++;
        }

        return nidDtoList;
    }*/
}
