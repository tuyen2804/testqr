package com.example.myapplication.history

import android.text.format.DateUtils.isToday
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.room.entity.QrScanEntity
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListScreen(
    items: List<QrScanEntity>,
    onEnterSelect: () -> Unit,
    onDeleteById: (Int) -> Unit,
    onDeleteAll: () -> Unit
) {
    var filterExpanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("URL", "Text", "Product", "Youtube", "Barcode")
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", color = Color.White) },
                actions = {
                    Box {
                        IconButton(onClick = { filterExpanded = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.filter),
                                contentDescription = "Filter",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false },
                            modifier = Modifier.background(Color(0xFF2C2C2E))
                        ) {
                            filterOptions.forEach { option ->
                                val checked = selectedFilters.contains(option)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                option,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = {
                                                    selectedFilters =
                                                        if (checked) selectedFilters - option
                                                        else selectedFilters + option
                                                }
                                            )
                                        }
                                    },
                                    onClick = {
                                        // toggle khi click row
                                        selectedFilters =
                                            if (checked) selectedFilters - option
                                            else selectedFilters + option
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDeleteAll) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete All",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1C1C1E))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        val filteredItems = if (selectedFilters.isEmpty()) {
            items
        } else {
            items.filter { qr ->
                when (getBarcodeTypeString(qr.type)) {
                    in selectedFilters -> true
                    else -> false
                }
            }
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No history", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val grouped = filteredItems.groupBy { formatDate(it.createdAt, "yyyy-MM-dd") }
                grouped.forEach { (date, list) ->
                    item {
                        Text(
                            text = if (isToday(date)) "Today" else formatDateLabel(date),
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(list) { qr ->
                        HistoryItem(entity = qr, onDelete = { onDeleteById(qr.id) })
                    }
                }
            }
        }
    }
}
