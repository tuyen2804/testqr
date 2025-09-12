package com.example.myapplication.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.room.entity.QrCreateEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryCreateViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).qrCreate()

    private val _items = MutableStateFlow<List<QrCreateEntity>>(emptyList())
    val items: StateFlow<List<QrCreateEntity>> = _items

    fun loadHistory() {
        viewModelScope.launch {
            _items.value = dao.getAll()
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            dao.deleteById(id)
            loadHistory()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.deleteAll()
            _items.value = emptyList()
        }
    }
}
