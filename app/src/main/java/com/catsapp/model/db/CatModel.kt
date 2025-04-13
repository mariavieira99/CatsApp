package com.catsapp.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cats_data_table")
data class CatModel(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "breed_name") val breedName: String,
    @ColumnInfo(name = "image_id") val imageId: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "origin") val origin: String,
    @ColumnInfo(name = "temperament") val temperament: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "favouriteId") val favouriteId: Int,
    @ColumnInfo(name = "isFavourite") val isFavourite: Boolean,
    @ColumnInfo(name = "higherLifespan") val higherLifespan: Int,
)