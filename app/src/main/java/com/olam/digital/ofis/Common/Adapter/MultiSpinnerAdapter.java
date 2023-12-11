package com.olam.digital.ofis.Common.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.olam.digital.ofis.Common.Model.MultiSpinnerStatus;
import com.olam.digital.ofis.R;

import java.util.ArrayList;
import java.util.List;

public class MultiSpinnerAdapter extends ArrayAdapter<MultiSpinnerStatus> {
    private Context mContext;
    private ArrayList<MultiSpinnerStatus> listState;
    private MultiSpinnerAdapter myAdapter;
    private boolean isFromView = false;
    private Spinner spinnerView;
    private EditText cert_others_ed;

    public MultiSpinnerAdapter(Context context, int resource, List<MultiSpinnerStatus> objects, Spinner spinnerView, EditText cert_others_ed) {
        super(context, resource, objects);
        this.mContext = context;
        this.listState = (ArrayList<MultiSpinnerStatus>) objects;
        this.spinnerView = spinnerView;
        this.cert_others_ed = cert_others_ed;
        this.myAdapter = this;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(final int position, View convertView,
                              ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(R.layout.multi_select_spinner_item, null);
            holder = new ViewHolder();
            holder.mTextView = (TextView) convertView
                    .findViewById(R.id.txt_certificationName);
            holder.mCheckBox = (CheckBox) convertView
                    .findViewById(R.id.cb_certificationName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTextView.setText(listState.get(position).getTitle());

        // To check weather checked event fire from getview() or user input
        isFromView = true;
        holder.mCheckBox.setChecked(listState.get(position).isSelected());
        holder.mCheckBox.setEnabled(listState.get(position).isEnabled());
        isFromView = false;

        if ((position == 0)) {
            holder.mCheckBox.setVisibility(View.INVISIBLE);
        } else {
            holder.mCheckBox.setVisibility(View.VISIBLE);
        }
        holder.mCheckBox.setTag(position);
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int getPosition = (Integer) buttonView.getTag();

                if (!isFromView) {
                    listState.get(position).setSelected(isChecked);
                    if (spinnerView != null) {
                        spinnerView.setTag(listState);
                    }
                    if (listState.get(position).getTitle().equals(mContext.getString(R.string.others))) {
                        cert_others_ed.setText(null);
                        cert_others_ed.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    }
                    if (listState.get(position).getTitle().equals("None")) {
                        for (int i = 0; i < listState.size(); i++) {
                            if (isChecked) {
                                listState.get(i).setSelected(false);
                                if (i != position) {
                                    listState.get(i).setEnabled(false);
                                }
                            } else {
                                listState.get(i).setEnabled(true);
                            }
                        }
                        if (isChecked) {
                            cert_others_ed.setText(null);
                            cert_others_ed.setVisibility(View.GONE);
                            listState.get(position).setSelected(isChecked);
                        }
                        notifyDataSetChanged();
                    }
                }
            }
        });
        return convertView;
    }

    private class ViewHolder {
        private TextView mTextView;
        private CheckBox mCheckBox;
    }
}