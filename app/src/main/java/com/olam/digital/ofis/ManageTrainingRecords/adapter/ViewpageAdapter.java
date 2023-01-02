package com.olam.digital.ofis.ManageTrainingRecords.adapter;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.olam.digital.ofis.ManageTrainingRecords.Fragment.ViewFileFragment;
import com.olam.digital.ofis.ManageTrainingRecords.model.DocumentList;
import java.io.Serializable;
import java.util.List;

public class ViewpageAdapter extends FragmentStatePagerAdapter {
    public java.util.List<DocumentList.data> List;
    private String[] name;
    String KEY="Doc";

    public ViewpageAdapter(@NonNull FragmentManager fm, List<DocumentList.data> List, String[] name) {
        super(fm);
        this.List=List;
        this.name=name;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return name[position];
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment=new ViewFileFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(KEY, (Serializable) List);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return name.length;
    }
}
