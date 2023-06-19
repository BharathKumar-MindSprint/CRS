package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.CapturedProofFollowupTable;

import java.util.List;

@Dao
public interface CapturedProofFollowupTableDao {

    @Insert
    void insert(CapturedProofFollowupTable capturedProof);

    @Query("SELECT * FROM CapturedProofFollowupTable")
    List<CapturedProofFollowupTable> getAllProofs();
}
