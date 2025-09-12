package com.example.myapplication.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HistoryCreateViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryCreateViewModel::class.java)) {
            return HistoryCreateViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
