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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateCalendarScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("")) }
    var allDay by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(TextFieldValue("Sep 12 10:00")) }
    var endTime by remember { mutableStateOf(TextFieldValue("Sep 12 11:00")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var showLocation by remember { mutableStateOf(false) }
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
                text = "Calendar",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title field with dropdown
        Text(
            text = "Title",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Please enter something", color = Color.Gray) },
            trailingIcon = {
                IconButton(onClick = { showLocation = !showLocation }) {
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

        // Show Location field when dropdown is clicked
        if (showLocation) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Location",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("Please enter something", color = Color.Gray) },
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

        Spacer(modifier = Modifier.height(16.dp))

        // All day toggle
        Text(
            text = "All day",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A2C36).copy(alpha = 0.8f))
                .padding(8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = allDay,
                onCheckedChange = { allDay = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF2196F3),
                    uncheckedThumbColor = Color.Gray,
                    checkedTrackColor = Color(0xFF2A2C36),
                    uncheckedTrackColor = Color(0xFF2A2C36)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start time field
        Text(
            text = "Start",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            placeholder = { Text("Start", color = Color.Gray) },
            enabled = !allDay,
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

        // End time field
        Text(
            text = "End",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            placeholder = { Text("End", color = Color.Gray) },
            enabled = !allDay,
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

        // Description field
        Text(
            text = "Description",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Description", color = Color.Gray) },
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

        Spacer(modifier = Modifier.weight(1f))

        // Create button
        Button(
            onClick = {
                if (title.text.isNotEmpty()) {
                    // Định dạng chuỗi VEVENT
                    val vevent = buildVeventString(
                        title = title.text,
                        location = location.text,
                        allDay = allDay,
                        startTime = startTime.text,
                        endTime = endTime.text,
                        description = description.text
                    )

                    // Tạo mã QR
                    val qrCodeWriter = QRCodeWriter()
                    val bitMatrix = qrCodeWriter.encode(vevent, BarcodeFormat.QR_CODE, 512, 512)
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
                                value = vevent,
                                type = 2, // Type 2 cho Calendar
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
            enabled = title.text.isNotEmpty()
        ) {
            Text("Create", color = Color.White, fontSize = 16.sp)
        }
    }
}


private fun buildVeventString(
    title: String,
    location: String,
    allDay: Boolean,
    startTime: String,
    endTime: String,
    description: String
): String {
    val inputFormatter = SimpleDateFormat("MMM d HH:mm", Locale.US)
    val outputFormatter = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val startDate = try {
        inputFormatter.parse("$startTime $currentYear")
    } catch (e: Exception) {
        Calendar.getInstance().time // Mặc định là thời gian hiện tại
    }

    val endDate = try {
        inputFormatter.parse("$endTime $currentYear")
    } catch (e: Exception) {
        Calendar.getInstance().apply { time = startDate; add(Calendar.HOUR, 1) }.time
    }

    val veventStart = outputFormatter.format(startDate)
    val veventEnd = outputFormatter.format(endDate)

    return """
        BEGIN:VEVENT
        SUMMARY:$title
        ${if (location.isNotEmpty()) "LOCATION:$location" else ""}
        DTSTART:$veventStart
        DTEND:$veventEnd
        ${if (allDay) "RRULE:FREQ=DAILY;COUNT=1" else ""}
        ${if (description.isNotEmpty()) "DESCRIPTION:$description" else ""}
        END:VEVENT
    """.trimIndent()
}