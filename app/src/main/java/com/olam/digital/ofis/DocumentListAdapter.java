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

    public List<DocumentList.data> arrayList;
    public DocumentListAdapter(List<DocumentList.data> arrayList) {
        this.arrayList=arrayList;
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
    public void onBindViewHolder(@NonNull DocumentListAdapter.ViewHolder holder, int position) {
        DocumentList.data data=arrayList.get(position);
        holder.detail.setText(data.getDescription());
        holder.file.setText(data.getFileName());
        if(data.getFileFormat().equals("pdf")){
             holder.imageView.setImageResource(R.drawable.pdf2);
        }else{
            holder.imageView.setImageResource(R.drawable.docx);
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WebViewer.class);
                intent.putExtra("Filename",data.getFileName());
                intent.putExtra("DocId",data.gettDocsLanId() );
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
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
//    ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//    @SuppressLint("MissingPermission") NetworkInfo networkInfo = manager.getActiveNetworkInfo();
//    boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
//                if(isConnected){
//                        Intent intent = new Intent(context, WebViewer.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
//        }else{
//        ManagingTrainingActivity activity=(ManagingTrainingActivity) view.getContext();
//        NetworkFragment frag=new NetworkFragment();
//        activity.getSupportFragmentManager()
//        .beginTransaction()
//        .replace(R.id.parent_layout,frag)
//        .addToBackStack(null)
//        .commit();
//
//        }
