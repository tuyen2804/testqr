package com.example.myapplication.resultscan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.model.QrCodeInfo

class ResultMultiQrViewModelFactory(
    private val initialList: List<QrCodeInfo>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultMultiQrViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResultMultiQrViewModel(initialList) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}