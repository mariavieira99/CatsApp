package com.catsapp.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CatsDao {
    @Query("SELECT * FROM cats_data_table")
    suspend fun getAllCats(): List<CatModel>

    @Query("SELECT * FROM cats_data_table WHERE isFavourite = 1")
    suspend fun getFavouriteCats(): List<CatModel>

    @Update
    suspend fun updateCat(cat: CatModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace data if it already exists
    suspend fun insertCats(cats: List<CatModel>)

    @Query("DELETE FROM cats_data_table")
    suspend fun deleteAllCats()

    @Query("SELECT * FROM cats_data_table WHERE id = :id")
    suspend fun getCatById(id: String): CatModel?
}