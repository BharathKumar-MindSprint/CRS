package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSectionsItem;

import java.util.List;

@Dao
public interface DmSectionsItemDao {

    @Query("SELECT * FROM DmSectionsItem")
    List<DmSectionsItem> getDmSectionsItem();

    @Insert
    void insert(DmSectionsItem... dmSectionsItem);

    @Delete
    void delete(DmSectionsItem dmSectionsItem);
}
