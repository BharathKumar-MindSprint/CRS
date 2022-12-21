package com.olam.digital.ofis;

import static com.olam.digital.ofis.EnableMultiDex.context;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.model.DocumentList;

import java.util.ArrayList;
import java.util.List;

public class DocumentListAdapter extends RecyclerView.Adapter<DocumentListAdapter.ViewHolder> {
    private final static String PDF="pdf";
    public List<DocumentList.data> arrayList;
    private com.olam.digital.ofis.ItemClickListener ItemClickListener;

    public DocumentListAdapter(List<DocumentList.data> arrayList,ItemClickListener itemClickListener) {
        this.arrayList=arrayList;
        this.ItemClickListener=itemClickListener;
    }
    @NonNull
    @Override
    public DocumentListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.item_doc_pdf_cardview, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DocumentList.data data=arrayList.get(position);
        holder.detail.setText(data.getDescription());
        holder.file.setText(data.getFileName());
        if(data.getFileFormat().equals(PDF)){
             holder.imageView.setImageResource(R.drawable.ic_pdf);
        }else{
            holder.imageView.setImageResource(R.drawable.ic_word);
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemClickListener.onClick(view,position,data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void itemClickListener(ItemClickListener documentList) {
        this.ItemClickListener=documentList;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView file;
        public TextView detail;
        public LinearLayout linearLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            this.file = (TextView) itemView.findViewById(R.id.tnFileList);
            this.detail = (TextView) itemView.findViewById(R.id.tvDetailList);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.LinearLayout);
        }
    }

}

