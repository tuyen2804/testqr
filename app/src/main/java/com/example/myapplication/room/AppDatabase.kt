package com.example.myapplication.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.room.dao.QrScanDao
import com.example.myapplication.room.entity.QrScanEntity

@Database(entities = [QrScanEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun qrScanDao(): QrScanDao
}
