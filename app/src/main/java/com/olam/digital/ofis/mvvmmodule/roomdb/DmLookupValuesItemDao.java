package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmLookupValuesItem;

import java.util.List;


@Dao
public interface DmLookupValuesItemDao {
    @Query("SELECT * FROM DmLookupValuesItem")
    List<DmLookupValuesItem> getDmLookupValuesItem();

    @Query("SELECT * FROM DmLookupValuesItem WHERE idQuestionsMudule =:id")
    List<DmLookupValuesItem> getDmAnswersModuleByQuestionId(int id);

    @Insert
    void insert(DmLookupValuesItem... dmLookupValuesItems);

    @Delete
    void delete(DmLookupValuesItem dmLookupValuesItems);

    @Query("DELETE FROM DmLookupValuesItem")
    void deleteAll();

    @Query("SELECT proofFormat FROM DmLookupValuesItem WHERE idQuestionsMudule=:id AND complianceId =:complianceId AND id=:answerID")
    String getProofFormatByQuestionIdDropDown (String id , String complianceId, String answerID);

    @Query("SELECT * FROM DmLookupValuesItem WHERE id =:id")
    DmLookupValuesItem getAllValuesByLookupId(int id);

    @Query("SELECT CASE "
            + "WHEN 'Both' IN (SELECT DISTINCT proofFormat FROM DmLookupValuesItem WHERE id IN (:answerIds) AND complianceId IN (:complianceIds) AND idQuestionsMudule = :questionId) "
            + "OR ('Document (.doc, .docx, .xls, .xlsx, .pdf, .csv)' IN (SELECT DISTINCT proofFormat FROM DmLookupValuesItem WHERE id IN (:answerIds) AND complianceId IN (:complianceIds) AND idQuestionsMudule = :questionId) "
            + "AND 'Image (.png, .jpg, .jpeg)' IN (SELECT DISTINCT proofFormat FROM DmLookupValuesItem WHERE id IN (:answerIds) AND complianceId IN (:complianceIds) AND idQuestionsMudule = :questionId)) "
            + "THEN 'Both' "
            + "WHEN 'Document (.doc, .docx, .xls, .xlsx, .pdf, .csv)' IN (SELECT DISTINCT proofFormat FROM DmLookupValuesItem WHERE id IN (:answerIds) AND complianceId IN (:complianceIds) AND idQuestionsMudule = :questionId) "
            + "THEN 'Document (.doc, .docx, .xls, .xlsx, .pdf, .csv)' "
            + "WHEN 'Image (.png, .jpg, .jpeg)' IN (SELECT DISTINCT proofFormat FROM DmLookupValuesItem WHERE id IN (:answerIds) AND complianceId IN (:complianceIds) AND idQuestionsMudule = :questionId) "
            + "THEN 'Image (.png, .jpg, .jpeg)' "
            + "ELSE 'Both' END")
    String getCaptureProofCheckbox(List<Integer> answerIds, List<Integer> complianceIds, String questionId);
}
