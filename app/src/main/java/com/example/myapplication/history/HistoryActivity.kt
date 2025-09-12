package com.example.myapplication.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.history.ui.theme.MyApplicationTheme
import com.example.myapplication.room.entity.QrCreateEntity

class HistoryActivity : ComponentActivity() {

    private val viewModel: HistoryCreateViewModel by viewModels {
        HistoryCreateViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                HistoryScreen(viewModel = viewModel)
            }
        }

        // load dữ liệu khi mở màn
        viewModel.loadHistory()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryCreateViewModel) {
    val items by viewModel.items.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History Created QR", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1E26)
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No history yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1E26))
                    .padding(innerPadding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { entity ->
                    HistoryCreateItem(
                        entity = entity,
                        onDelete = { id -> viewModel.deleteItem(id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCreateItem(entity: QrCreateEntity, onDelete: (Int) -> Unit) {
    Surface(
        color = Color(0xFF2C2C2E),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.value,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = "Type: ${entity.type}",
                    color = Color.Gray,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
            }
            IconButton(onClick = { onDelete(entity.id) }) {
                Icon(
                    painter = painterResource(id = com.example.myapplication.R.drawable.delete),
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}
