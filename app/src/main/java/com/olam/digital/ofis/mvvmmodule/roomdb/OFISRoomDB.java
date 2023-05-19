package com.olam.digital.ofis.mvvmmodule.roomdb;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;


@Database(entities ={DmMainModule.class}, version = 1)
public abstract class OFISRoomDB extends RoomDatabase {
    private static final String DATABASE_NAME = "ofismobileroom";
    public abstract DmMainModuleDao dmMainModuleDao();
    //public abstract ComplianceIndicatorDao complianceIndicatorDao();
    public static volatile OFISRoomDB INSTANCE = null;

    public static OFISRoomDB getDbInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), OFISRoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}