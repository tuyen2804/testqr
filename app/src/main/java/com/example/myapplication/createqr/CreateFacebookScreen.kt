package com.example.myapplication.createqr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.resultscan.ResultMultiQrDetailActivity
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.room.entity.QrCreateEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CreateFacebookScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    var facebookId by remember { mutableStateOf(TextFieldValue("")) }
    var facebookUrl by remember { mutableStateOf(TextFieldValue("")) }
    val db = AppDatabase.getInstance(context)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1E26))
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Facebook",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Facebook icon
        Icon(
            painter = painterResource(R.drawable.facebook),
            contentDescription = "Facebook Icon",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Open button
        Button(
            onClick = {
                val url = when (selectedTabIndex) {
                    0 -> if (facebookId.text.isNotEmpty()) "https://www.facebook.com/${facebookId.text}" else ""
                    1 -> facebookUrl.text
                    else -> ""
                }
                if (url.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("QrScanner", "Lỗi khi mở URL: $url", e)
                    }
                } else {
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2C36)),
            shape = RoundedCornerShape(8.dp),
            enabled = (selectedTabIndex == 0 && facebookId.text.isNotEmpty()) || (selectedTabIndex == 1 && facebookUrl.text.isNotEmpty())
        ) {
            Text("Open", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row for Facebook ID or URL
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF1C1E26),
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFF2196F3)
                )
            }
        ) {
            val tabs = listOf("Facebook ID", "URL")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, color = if (selectedTabIndex == index) Color.White else Color.Gray) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input field based on selected tab
        when (selectedTabIndex) {
            0 -> {
                Text(
                    text = "Enter Facebook ID",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = facebookId,
                    onValueChange = { facebookId = it },
                    placeholder = { Text("Enter Facebook ID", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFF2A2C36),
                        unfocusedContainerColor = Color(0xFF2A2C36),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }
            1 -> {
                Text(
                    text = "Facebook Profile URL",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = facebookUrl,
                    onValueChange = { facebookUrl = it },
                    placeholder = { Text("Facebook Profile URL", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFF2A2C36),
                        unfocusedContainerColor = Color(0xFF2A2C36),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Create button
        Button(
            onClick = {
                val input = when (selectedTabIndex) {
                    0 -> if (facebookId.text.isNotEmpty()) "https://www.facebook.com/${facebookId.text}" else ""
                    1 -> facebookUrl.text
                    else -> ""
                }
                if (input.isNotEmpty()) {
                    // Tạo mã QR
                    val qrCodeWriter = QRCodeWriter()
                    val bitMatrix = qrCodeWriter.encode(input, BarcodeFormat.QR_CODE, 512, 512)
                    val bitmap = Bitmap.createBitmap(
                        512, 512, Bitmap.Config.ARGB_8888
                    ).apply {
                        for (x in 0 until width) {
                            for (y in 0 until height) {
                                setPixel(x, y, if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb())
                            }
                        }
                    }

                    // Lưu ảnh QR vào bộ nhớ trong
                    val imageUri = saveBitmapToInternalStorage(context, bitmap)
                    if (imageUri != null) {
                        // Lưu thông tin vào Room trong coroutine
                        scope.launch {
                            val qrEntity = QrCreateEntity(
                                value = input,
                                type = 5, // Type 5 cho Facebook
                                imagePath = imageUri.toString()
                            )
                            db.qrCreate().insert(qrCreate = qrEntity)

                            // Chuyển hướng đến ResultMultiQrDetailActivity
                            val intent = Intent(context, ResultMultiQrDetailActivity::class.java)
                            intent.putExtra("scan_result", qrEntity.value)
                            intent.putExtra("scan_type", qrEntity.type)
                            intent.putExtra("qr_image_uri", qrEntity.imagePath)
                            context.startActivity(intent)
                        }
                    } else {
                        Log.e("QrScanner", "Lỗi khi lưu ảnh QR")
                    }
                } else {
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(25.dp),
            enabled = (selectedTabIndex == 0 && facebookId.text.isNotEmpty()) || (selectedTabIndex == 1 && facebookUrl.text.isNotEmpty())
        ) {
            Text("Create", color = Color.White, fontSize = 16.sp)
        }
    }
}
