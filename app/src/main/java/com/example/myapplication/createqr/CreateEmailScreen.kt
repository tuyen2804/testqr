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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
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
import java.net.URLEncoder

@Composable
fun CreateEmailScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var subject by remember { mutableStateOf(TextFieldValue("")) }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var showAdditionalFields by remember { mutableStateOf(false) }
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
                text = "Email",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Email field with dropdown
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email address", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.mail),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = { showAdditionalFields = !showAdditionalFields }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
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

        // Show Subject and Content fields when dropdown is clicked
        if (showAdditionalFields) {
            Spacer(modifier = Modifier.height(16.dp))

            // Subject field
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                placeholder = { Text("Subject", color = Color.Gray) },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Content field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Please enter something", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
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

        Spacer(modifier = Modifier.weight(1f))

        // Create button
        Button(
            onClick = {
                if (email.text.isNotEmpty()) {
                    // Định dạng chuỗi mailto
                    val mailto = buildMailtoString(
                        email = email.text,
                        subject = subject.text,
                        content = content.text
                    )

                    // Tạo mã QR
                    val qrCodeWriter = QRCodeWriter()
                    val bitMatrix = qrCodeWriter.encode(mailto, BarcodeFormat.QR_CODE, 512, 512)
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
                                value = mailto,
                                type = 4, // Type 4 cho Email
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
            enabled = email.text.isNotEmpty()
        ) {
            Text("Create", color = Color.White, fontSize = 16.sp)
        }
    }
}


private fun buildMailtoString(
    email: String,
    subject: String,
    content: String
): String {
    val query = mutableListOf<String>()
    if (subject.isNotEmpty()) {
        query.add("subject=${URLEncoder.encode(subject, "UTF-8")}")
    }
    if (content.isNotEmpty()) {
        query.add("body=${URLEncoder.encode(content, "UTF-8")}")
    }
    val queryString = if (query.isNotEmpty()) "?" + query.joinToString("&") else ""
    return "mailto:$email$queryString"
}