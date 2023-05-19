package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.FollowUp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.DiagnosticModel;
import com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.DiagnosticModuleActivity;

import java.util.ArrayList;

public class FollowUpRecyclerAdapter extends RecyclerView.Adapter<FollowUpRecyclerAdapter.ViewHolder> {
    private ArrayList<String> items;
    private Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public FollowUpRecyclerAdapter(ArrayList<String> items , Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.follow_up_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindItem(items.get(position), position);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView itemTextView;
        private ImageView tickImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTextView = itemView.findViewById(R.id.itemTextView);
            tickImageView = itemView.findViewById(R.id.tickImageView);

            itemView.setOnClickListener(this);
        }

        @SuppressLint("ResourceAsColor")
        public void bindItem(String item, int position) {
            itemTextView.setText(item);

            if (position == selectedPosition) {
                tickImageView.setVisibility(View.VISIBLE);
                Intent intent = new Intent(context , DiagnosticModuleActivity.class);
                context.startActivity(intent);

            } else {
                tickImageView.setVisibility(View.GONE);
            }

            /*if (position == selectedPosition) {
                Intent intent = new Intent(context , DiagnosticModuleActivity.class);
                context.startActivity(intent);
            }*/
        }

        @Override
        public void onClick(View v) {
            int previousPosition = selectedPosition;
            selectedPosition = getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }
    }
}

