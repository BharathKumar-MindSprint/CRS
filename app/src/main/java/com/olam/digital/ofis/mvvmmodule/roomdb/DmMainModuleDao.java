package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;

import java.util.List;

@Dao
public interface DmMainModuleDao {

    @Query("SELECT * FROM DmMainModule WHERE diagnosticTypeId != :diagnosticTypeId")
    List<DmMainModule> getDMmodule(int diagnosticTypeId);

    @Insert
    void insert(DmMainModule... dmModule);

    @Delete
    void delete(DmMainModule dmModule);

    @Query("DELETE FROM DmMainModule")
    void deleteAll();

    @Query("SELECT assessmentType FROM DmMainModule WHERE id=:moduleId")
    String getAssessmentType(int moduleId);

    @Query("SELECT displayLogic FROM DmMainModule WHERE id=:moduleId")
    String getDisplayLogic(int moduleId);

    @Query("SELECT maxScore FROM DmMainModule WHERE id=:moduleId")
    int getMaxScore(int moduleId);

    @Query("SELECT startDate FROM DmMainModule WHERE id=:moduleId")
    String getStartData(int moduleId);
    @Query("SELECT followUpDurationVal FROM DmMainModule WHERE id=:moduleId")
    int getFollowUpDurationVal(String moduleId);

    @Query("SELECT followUpAttemptsVal FROM DmMainModule WHERE id=:moduleId")
    String getFollowUpAttemptsVal(String moduleId);

    @Query("UPDATE DmMainModule SET recurrenceCount =:recurrenceCount, recurrenceStartDate = :startDate, recurrenceEndDate =:endDate WHERE id = :moduleId")
    void updateRecurrenceCountStartEndDate(int moduleId , String startDate , String endDate , int recurrenceCount);

    @Query("SELECT * FROM DmMainModule WHERE diagnosticTypeId = :diagnosticTypeId")
    List<DmMainModule> getDmPlotModuleByDiagnosticTypeId(int diagnosticTypeId);

    @Query("SELECT * FROM DmMainModule WHERE id = :moduleId")
    DmMainModule getDmMainModule(int moduleId);


    @Query("""
        SELECT * FROM DmMainModule INNER JOIN DmModuleAssignMents ON DmMainModule.id = DmModuleAssignMents.moduleId 
        WHERE DmMainModule.diagnosticTypeId != :diagnosticTypeId AND 
        ((DmModuleAssignMents.assignmentType = 'Country' AND DmModuleAssignMents.assignmentId = :countryId) OR
        (DmModuleAssignMents.assignmentType = 'Region' AND DmModuleAssignMents.assignmentId = :regionId) OR
        (DmModuleAssignMents.assignmentType = 'District' AND DmModuleAssignMents.assignmentId = :districtId) OR
        (DmModuleAssignMents.assignmentType = 'Place' AND DmModuleAssignMents.assignmentId = :placeId) OR
        (DmModuleAssignMents.assignmentType = 'FarmerGroup' AND DmModuleAssignMents.assignmentId = :farmerGroupId) OR
        (DmModuleAssignMents.assignmentType = 'Section' AND DmModuleAssignMents.assignmentId = :sectionId) OR
        (DmModuleAssignMents.assignmentType = 'Programme' AND DmModuleAssignMents.assignmentId = :programmeId) OR
        (DmModuleAssignMents.assignmentType = 'Product' AND DmModuleAssignMents.assignmentId = :productId) OR
        (DmModuleAssignMents.assignmentType = 'Partner' AND DmModuleAssignMents.assignmentId = :partnerId))
""")
    List<DmMainModule> getDmMainModuleByAssignments(String countryId, String regionId, String farmerGroupId, String districtId, String sectionId, String placeId, String partnerId, String programmeId, String productId, int diagnosticTypeId);


    @Query("""
        SELECT * FROM DmMainModule INNER JOIN DmModuleAssignMents ON DmMainModule.id = DmModuleAssignMents.moduleId 
        WHERE DmMainModule.diagnosticTypeId = :diagnosticTypeId AND 
        ((DmModuleAssignMents.assignmentType = 'Country' AND DmModuleAssignMents.assignmentId = :countryId) OR
        (DmModuleAssignMents.assignmentType = 'Region' AND DmModuleAssignMents.assignmentId = :regionId) OR
        (DmModuleAssignMents.assignmentType = 'District' AND DmModuleAssignMents.assignmentId = :districtId) OR
        (DmModuleAssignMents.assignmentType = 'Place' AND DmModuleAssignMents.assignmentId = :placeId) OR
        (DmModuleAssignMents.assignmentType = 'FarmerGroup' AND DmModuleAssignMents.assignmentId = :farmerGroupId) OR
        (DmModuleAssignMents.assignmentType = 'Section' AND DmModuleAssignMents.assignmentId = :sectionId) OR
        (DmModuleAssignMents.assignmentType = 'Programme' AND DmModuleAssignMents.assignmentId = :programmeId) OR
        (DmModuleAssignMents.assignmentType = 'Product' AND DmModuleAssignMents.assignmentId = :productId) OR
        (DmModuleAssignMents.assignmentType = 'Partner' AND DmModuleAssignMents.assignmentId = :partnerId))
""")
    List<DmMainModule> getDmMainModuleByAssignmentsByDiagnosticTypeId(String countryId, String regionId, String farmerGroupId, String districtId, String sectionId, String placeId, String partnerId, String programmeId, String productId, int diagnosticTypeId);

}
