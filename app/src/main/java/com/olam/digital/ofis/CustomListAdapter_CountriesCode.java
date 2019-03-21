package com.olam.digital.ofis;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.olam.digital.ofis.model.CountriesDTO;

import java.util.List;

public class CustomListAdapter_CountriesCode extends BaseAdapter implements SpinnerAdapter {
    private final Context context;
    private List<CountriesDTO> items;


    public CustomListAdapter_CountriesCode(Context context, List<CountriesDTO> dialCodes) {
        this.items = dialCodes;
        this.context = context;
    }


    public int getCount() {
        return items.size();
    }

    public Object getItem(int i) {
        return items.get(i);
    }

    public long getItemId(int i) {
        return (long)i;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView txt = new TextView(context);
        txt.setPadding(16, 16, 16, 2);
        txt.setTextSize(14);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        txt.setText(items.get(position).getDialingCode());
        txt.setTextColor(Color.parseColor("#000000"));
        return  txt;
    }

    public View getView(int i, View view, ViewGroup viewgroup) {
        TextView txt = new TextView(context);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(16, 16, 16, 2);
        txt.setTextSize(14);
//        txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
        txt.setText(items.get(i).getDialingCode());
        txt.setTextColor(Color.parseColor("#000000"));
        return  txt;
    }
}