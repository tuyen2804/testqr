package com.example.myapplication.resultscan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.myapplication.R
import com.example.myapplication.resultscan.ui.theme.MyApplicationTheme

class ResultDeleteMultiQrActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val qrList = intent.getStringArrayListExtra("scan_results") ?: arrayListOf()
        setContent {
            MyApplicationTheme {
                var showDetailQr by remember { mutableStateOf(false) }
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("4 Code") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    finish()
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.back),
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { /* Handle favorite */ }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.delete),
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF1C1C1E),
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) { innerPadding ->
                    QrListSelectScreen(showDetailQr,qrList, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun QrListSelectScreen(isShowDetail: Boolean, qrList: List<String>, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(qrList) { index, item ->
            if (isShowDetail) DetailQrListItemSelect(item, index,onClick = {
                val intent = Intent(context, ResultMultiQrDetailActivity::class.java)
                intent.putExtra("qr_value", item)
                context.startActivity(intent)
            }) else QrListItemSelect(item,index,onClick = {
                val intent = Intent(context, ResultScanActivity::class.java)
                intent.putExtra("qr_value", item)
                context.startActivity(intent)
            }
            )
        }
    }
}

@Composable
fun QrListItemSelect(item: String, index: Int,onClick: () -> Unit) {
    Surface(
        color = Color(0xFF2C2C2E),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon QR
            Icon(
                painter = painterResource(id = R.drawable.share),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item,
                    color = Color.White,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "URL",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "21:37", // giả lập, bạn có thể lưu timestamp lúc quét vào DB rồi hiển thị
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
@Composable
fun DetailQrListItemSelect(item: String, index: Int,onClick: () -> Unit) {
    Surface(
        color = Color(0xFF2C2C2E),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon QR
            Icon(
                painter = painterResource(id = R.drawable.qr),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item,
                    color = Color.White,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "URL",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "21:37", // giả lập, bạn có thể lưu timestamp lúc quét vào DB rồi hiển thị
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}