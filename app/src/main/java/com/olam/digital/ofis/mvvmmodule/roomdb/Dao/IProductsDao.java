package com.olam.digital.ofis.mvvmmodule.roomdb.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Products;

import java.util.List;

@Dao
public interface IProductsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Products> products);

    @Query("DELETE FROM Products")
    void deleteAll();
}
