package com.example.myapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable // Giữ nguyên nếu bạn dùng cho JSON khác
@Parcelize // Thêm annotation này để tự động implement Parcelable
data class QrCodeInfo(
    val value: String,
    val type: Int,
    val imageUri: String?
) : Parcelable // Thêm : Parcelable