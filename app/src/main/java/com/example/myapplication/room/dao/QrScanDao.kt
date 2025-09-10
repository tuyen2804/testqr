package com.example.myapplication.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.room.entity.QrScanEntity

@Dao
interface QrScanDao {
    @Insert
    suspend fun insert(qrScan: QrScanEntity)

    @Query("SELECT * FROM result_scan")
    suspend fun getAll(): List<QrScanEntity>

    @Query("DELETE FROM result_scan WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM result_scan")
    suspend fun deleteAll()
}
