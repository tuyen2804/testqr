package com.example.myapplication.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.room.entity.QrCreateEntity

@Dao
interface QrCreateDao {
    @Insert
    suspend fun insert(qrCreate: QrCreateEntity)

    @Query("SELECT * FROM qr_create")
    suspend fun getAll(): List<QrCreateEntity>

    @Query("DELETE FROM qr_create WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM qr_create")
    suspend fun deleteAll()
}
