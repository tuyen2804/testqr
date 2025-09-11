package com.example.myapplication.resultscan.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.QrCodeInfo

class ResultMultiQrViewModel(initialList: List<QrCodeInfo>) : ViewModel() {
    var qrList = mutableStateListOf<QrCodeInfo>().apply { addAll(initialList) }
        private set

    var selectedItems = mutableStateOf(setOf<Int>())
        private set

    // Hiển thị chi tiết hay không
    var showDetailQr = mutableStateOf(false)
        private set
    var inDeleteMode = mutableStateOf(false)
        private set

    fun enterDeleteMode() {
        inDeleteMode.value = true
    }
    fun getUpdatedQrList(): List<QrCodeInfo>{
        return qrList
    }

    fun exitDeleteMode(clearSelection: Boolean = true) {
        inDeleteMode.value = false
        if (clearSelection) selectedItems.value = emptySet()
    }

    fun toggleSelectAll() {
        selectedItems.value = if (selectedItems.value.size == qrList.size) {
            emptySet()
        } else {
            qrList.indices.toSet()
        }
    }

    fun toggleItem(index: Int) {
        selectedItems.value = if (selectedItems.value.contains(index)) {
            selectedItems.value - index
        } else {
            selectedItems.value + index
        }
    }

    fun toggleDetail() {
        showDetailQr.value = !showDetailQr.value
    }

    fun deleteSelected() {
        val newList = qrList.toMutableList()
        selectedItems.value.sortedDescending().forEach { index ->
            if (index in newList.indices) {
                newList.removeAt(index)
            }
        }
        qrList.clear()
        qrList.addAll(newList)
        selectedItems.value = emptySet()
    }
}