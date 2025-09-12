package com.example.myapplication.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.room.dao.QrCreateDao
import com.example.myapplication.room.dao.QrScanDao
import com.example.myapplication.room.entity.QrCreateEntity
import com.example.myapplication.room.entity.QrScanEntity

@Database(entities = [QrScanEntity::class, QrCreateEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun qrScanDao(): QrScanDao
    abstract fun qrCreate(): QrCreateDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qr_scanner.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
