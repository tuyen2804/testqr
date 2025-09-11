package com.example.myapplication.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.room.dao.QrScanDao
import com.example.myapplication.room.entity.QrScanEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val dao: QrScanDao) : ViewModel() {
    private val _items = MutableStateFlow<List<QrScanEntity>>(emptyList())
    val items: StateFlow<List<QrScanEntity>> = _items

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _items.value = dao.getAll()
        }
    }

    fun deleteById(id: Int) {
        viewModelScope.launch {
            dao.deleteById(id)
            loadHistory()
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            dao.deleteAll()
            loadHistory()
        }
    }
}
