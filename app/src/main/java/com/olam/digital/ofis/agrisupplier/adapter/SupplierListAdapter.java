package com.olam.digital.ofis.agrisupplier.adapter;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.agrisupplier.listener.SupplierItemClickListener;
import com.olam.digital.ofis.agrisupplier.dto.SupplierDto;

import java.util.ArrayList;
import java.util.List;

public class SupplierListAdapter extends RecyclerView.Adapter<SupplierListAdapter.ViewHolder> {
    List<SupplierDto> supplierList = new ArrayList<>();
    SupplierItemClickListener listener;
    private Resources res;
    public SupplierListAdapter(List<SupplierDto> supplierList, Resources res) {
        this.supplierList = supplierList;
        this.res = res;
    }

    public void setOnItemClickListener(SupplierItemClickListener listener) {
        this.listener = listener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView supplierName;
        TextView supplierId;

        ConstraintLayout layout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            supplierName = itemView.findViewById(R.id.supplierName);
            supplierId = itemView.findViewById(R.id.supplierId);
            layout = itemView.findViewById(R.id.clSupplierList);
        }
    }

    @NonNull
    @Override
    public SupplierListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.supplier_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SupplierListAdapter.ViewHolder holder, int position) {
        SupplierDto supplier = supplierList.get(position);
        holder.supplierName.setText(supplier.getSupplierName());
        holder.supplierId.setText(res.getString(R.string.supplier_id) + ": " +supplier.getSupplierId());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(supplier);
            }
        });
    }

    @Override
    public int getItemCount() {
        return supplierList == null ? 0 : supplierList.size();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void updateSupplierList(List<SupplierDto> newSupplierList) {
        supplierList.clear();
        if (newSupplierList != null) {
            supplierList.addAll(newSupplierList);
        }
        notifyDataSetChanged();
    }

}
