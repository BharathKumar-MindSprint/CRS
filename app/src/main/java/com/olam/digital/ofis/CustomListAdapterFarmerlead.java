package com.olam.digital.ofis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.olam.digital.ofis.model.FarmerLeadDTO;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomListAdapterFarmerlead extends ArrayAdapter<FarmerLeadDTO> {

    Context context;
    int resource;
    List<FarmerLeadDTO> items, tempItems, suggestions;


    public CustomListAdapterFarmerlead(@NonNull Context context, int resource, List<FarmerLeadDTO> items) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.items = items;
        tempItems = new ArrayList<FarmerLeadDTO>(items);
        suggestions = new ArrayList<FarmerLeadDTO>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.farmerlead_view, parent, false);
        }
        FarmerLeadDTO leadDTO = items.get(position);
        if (leadDTO != null) {
            TextView flName = (TextView) view.findViewById(R.id.farmerleadname);
            TextView flId = (TextView) view.findViewById(R.id.farmerleadid);
            TextView flPhone = (TextView) view.findViewById(R.id.phoneno);
            if (flName != null) flName.setText(leadDTO.getFarmerLeadFullName());
            if (flId != null) flId.setText(leadDTO.getFarmerLeadDisplayId());
            if (flPhone != null) flPhone.setText(leadDTO.getContactNumber());
        }
        return view;

    }

    @NonNull
    @Override
    public Filter getFilter() {
        return nameFilter;
    }


    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = ((FarmerLeadDTO) resultValue).getFarmerLeadFullName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (FarmerLeadDTO farmerLeadDTO : items) {
                   if (farmerLeadDTO.getFarmerLeadFullName().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            farmerLeadDTO.getFarmerLeadDisplayId().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            farmerLeadDTO.getContactNumber().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(farmerLeadDTO);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<FarmerLeadDTO> filterList = (ArrayList<FarmerLeadDTO>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (FarmerLeadDTO leadDTO : filterList) {
                    add(leadDTO);
                    notifyDataSetChanged();
                }
            }
        }
    };
}
