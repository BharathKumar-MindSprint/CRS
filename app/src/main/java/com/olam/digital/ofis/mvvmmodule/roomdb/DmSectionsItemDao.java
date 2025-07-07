package com.olam.digital.ofis.mvvmmodule.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSectionsItem;

import java.util.List;

@Dao
public interface DmSectionsItemDao {

    @Query("SELECT * FROM DmSectionsItem")
    List<DmSectionsItem> getDmSectionsItem();

    @Query("SELECT name FROM DmSectionsItem WHERE id =:id")
    String loadSingle(int id);


    @Query("SELECT * FROM DmSectionsItem WHERE moduleId=:id order by position")
    List<DmSectionsItem> getDmSectionsByModuleId(int id);

    @Insert
    void insert(DmSectionsItem... dmSectionsItem);

    @Delete
    void delete(DmSectionsItem dmSectionsItem);

    @Query("DELETE FROM DmSectionsItem")
    void deleteAll();


}
