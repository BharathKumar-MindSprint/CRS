package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRangeTextBoxModuleQuestionsTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmTextBoxModuleQuestions;

import java.util.List;

@Dao
public interface DmRangeTextBoxModuleQuestionsTableDao {

    @Query("SELECT * FROM DmRangeTextBoxModuleQuestionsTable")
    List<DmRangeTextBoxModuleQuestionsTable> getDmRangeTextBoxModuleQuestionsTable();


    @Insert
    void insert(DmRangeTextBoxModuleQuestionsTable... dmRangeTextBoxModuleQuestionsTables);

    @Delete
    void delete(DmRangeTextBoxModuleQuestionsTable dmRangeTextBoxModuleQuestionsTables);

    @Query("SELECT * FROM DmRangeTextBoxModuleQuestionsTable WHERE questionId=:id")
    List<DmRangeTextBoxModuleQuestionsTable> getDmTextBoxByQuestionId(int id);

    @Query("DELETE FROM DmRangeTextBoxModuleQuestionsTable")
    void deleteAll();

    @Query("SELECT proofFormat FROM DmRangeTextBoxModuleQuestionsTable WHERE questionId=:id AND complianceId =:complianceId AND id=:answerID")
    String getProofFormatByQuestionId (String id , String complianceId, String answerID);
}
