package com.example.myapplication.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_create")
data class QrCreateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val value: String,
    val type: Int,
    val imagePath: String,
    val createdAt: Long = System.currentTimeMillis()
)
