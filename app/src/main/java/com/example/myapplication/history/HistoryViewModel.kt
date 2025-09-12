package com.example.myapplication.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.room.dao.QrScanDao
import com.example.myapplication.room.entity.QrScanEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val dao: QrScanDao) : ViewModel() {
    // trực tiếp quan sát flow từ Room
    val items: StateFlow<List<QrScanEntity>> =
        dao.getAll()
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun deleteById(id: Int) {
        viewModelScope.launch {
            dao.deleteById(id)
            // không cần loadHistory() nữa vì Flow tự update
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}

