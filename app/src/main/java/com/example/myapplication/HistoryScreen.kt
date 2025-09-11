package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.history.HistoryListScreen
import com.example.myapplication.history.HistorySelectScreen
import com.example.myapplication.history.HistoryViewModel
import com.example.myapplication.history.HistoryViewModelFactory
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.room.entity.QrScanEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
) {
    val context= LocalContext.current
    val factory = remember {
        HistoryViewModelFactory(AppDatabase.getInstance(context).qrScanDao())
    }

    val viewModel: HistoryViewModel = viewModel(factory = factory)

    var selectMode by remember { mutableStateOf(false) }
    val items by viewModel.items.collectAsState()

    if (selectMode) {
        HistorySelectScreen(
            items = items,
            onExitSelect = { selectMode = false },
            onDeleteById = { id -> viewModel.deleteById(id) },
            onDeleteAll = { viewModel.deleteAll() }
        )
    } else {
        HistoryListScreen(
            items = items,
            onEnterSelect = { selectMode = true },
            onDeleteById = { id -> viewModel.deleteById(id) },
            onDeleteAll = { viewModel.deleteAll() }
        )
    }
}
