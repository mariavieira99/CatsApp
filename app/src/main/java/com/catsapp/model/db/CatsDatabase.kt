package com.catsapp.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CatModel::class], version = 2)
abstract class CatsDatabase : RoomDatabase() {

    abstract fun catsDao(): CatsDao

    companion object {
        @Volatile
        private var instance: CatsDatabase? = null

        fun getDatabase(context: Context) = instance ?: synchronized(this) {
            Room.databaseBuilder(
                context = context.applicationContext,
                klass = CatsDatabase::class.java,
                name = "cats_database"
            )
                .addMigrations(MIGRATION_1_2)
                .build().also { instance = it }
        }
    }
}