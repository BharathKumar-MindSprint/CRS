package com.olam.digital.ofis.ManageTrainingRecords.Fragment;

import static com.olam.digital.ofis.EnableMultiDex.context;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olam.digital.ofis.ManageTrainingRecords.Listener.ItemClickListener;
import com.olam.digital.ofis.ManageTrainingRecords.WebViewer;
import com.olam.digital.ofis.ManageTrainingRecords.adapter.DocumentListAdapter;
import com.olam.digital.ofis.ManageTrainingRecords.model.DocumentList;
import com.olam.digital.ofis.R;

import java.io.Serializable;
import java.util.List;

public class ViewFileFragment extends Fragment {
    private RecyclerView recyclerView;
    Bundle bundle;
    DocumentListAdapter adapter;
    List<DocumentList.data> data1;
    ItemClickListener itemClickListener;
    String FILENAME="Filename";
    String DOCID="DocId";
    String KEY="Doc";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_view_file, container, false);
        recyclerView=view.findViewById(R.id.recycler_view);
        Bundle bundle=getArguments();
        if(bundle != null){
            data1=(List<DocumentList.data>)bundle.getSerializable(KEY);
            adapter = new DocumentListAdapter(data1,itemClickListener);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
            adapter.itemClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, List<DocumentList.data> documentList) {
                    Intent intent = new Intent(getActivity(), WebViewer.class);
                    intent.putExtra(FILENAME,data1.get(position).getFileName());
                    intent.putExtra(DOCID,data1.get(position).gettDocsLanId());
                    startActivity(intent);
                }
            });
        }
        return view;
    }
}