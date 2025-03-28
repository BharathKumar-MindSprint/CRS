package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.mvvmmodule.roomdb.entity.OnGroundSurveyNotification;

import java.util.List;

@Dao
public interface OnGroundSurveyNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OnGroundSurveyNotification> onGroundSurveyNotifications);

    @Query("SELECT COUNT(DISTINCT(farmerId)) from OnGroundSurveyNotification WHERE (:eudr = '' OR surveyType = :eudr)")
    int getPendingEUDRSurveyCount(String eudr);

    @Query(
        """
            SELECT DISTINCT(farmerId) AS _id, farmerName AS name, photo, olamFarmerId AS olam_farmer_id, createdOnPhone AS created_on_this_phone, countryId AS country_id, odFarmerId 
            FROM OnGroundSurveyNotification 
            WHERE (:q = '' OR (farmerName LIKE '%' || :q || '%' OR olam_farmer_id LIKE '%' || :q || '%'))
            AND surveyType = :surveyType 
            AND (:farmerGroupId == 0 OR farmerGroupId = :farmerGroupId) 
            AND (:sectionId == 0 OR sectionId = :sectionId)
            ORDER BY name ASC
        """)
    Cursor getFarmersBySurveyType(String surveyType, String q, long farmerGroupId, long sectionId);

    @Query("DELETE FROM OnGroundSurveyNotification")
    void deleteAll();

    @Query("SELECT DISTINCT(farmerGroupId) from ongroundsurveynotification WHERE surveyType = :notificationType")
    List<Integer> getFarmerGroupsBasedOnSurveyType(String notificationType);

    @Query("DELETE FROM ongroundsurveynotification WHERE farmerId = :farmerId AND plotId = :plotId AND surveyType LIKE :s")
    void deleteBySurveyType(long farmerId, int plotId, String s);

    @Query("SELECT EXISTS(SELECT 1 FROM ongroundsurveynotification WHERE farmerId = :farmerId)")
    boolean checkSurveyExists(long farmerId);
}
