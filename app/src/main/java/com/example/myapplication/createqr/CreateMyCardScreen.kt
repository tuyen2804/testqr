package com.example.myapplication.createqr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@Composable
fun CreateMyCardScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var address by remember { mutableStateOf(TextFieldValue("")) }
    var birthday by remember { mutableStateOf(TextFieldValue("")) }
    var org by remember { mutableStateOf(TextFieldValue("")) }
    var note by remember { mutableStateOf(TextFieldValue("")) }

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
                text = "My Card",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Nội dung cuộn
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            MyOutlinedField("My name", name) { name = it }
            Spacer(modifier = Modifier.height(16.dp))
            MyOutlinedField("Phone number", phone, R.drawable.telephone) { phone = it }
            Spacer(modifier = Modifier.height(16.dp))
            MyOutlinedField("Email", email, R.drawable.mail) { email = it }
            Spacer(modifier = Modifier.height(16.dp))
            MyOutlinedField("Address", address, R.drawable.location) { address = it }
            Spacer(modifier = Modifier.height(16.dp))
            MyOutlinedField("Birthday", birthday, R.drawable.birth) { birthday = it }
            Spacer(modifier = Modifier.height(16.dp))
            MyOutlinedField("Org", org, R.drawable.contacts) { org = it }
            Spacer(modifier = Modifier.height(16.dp))
            MyOutlinedField("Note", note, R.drawable.contacts) { note = it }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Nút tạo QR
        Button(
            onClick = {
                val vcard = buildVCard(
                    name = name.text,
                    phone = phone.text,
                    email = email.text,
                    address = address.text,
                    birthday = birthday.text,
                    org = org.text,
                    note = note.text
                )

                val qrCodeWriter = QRCodeWriter()
                val bitMatrix = qrCodeWriter.encode(vcard, BarcodeFormat.QR_CODE, 512, 512)
                val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888).apply {
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            setPixel(x, y, if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb())
                        }
                    }
                }

                val imageUri = saveBitmapToInternalStorage(context, bitmap)
                if (imageUri != null) {
                    scope.launch {
                        val qrEntity = QrCreateEntity(
                            value = vcard,
                            type = 5, // Type 5 cho MyCard
                            imagePath = imageUri.toString()
                        )
                        db.qrCreate().insert(qrEntity)

                        val intent = Intent(context, ResultMultiQrDetailActivity::class.java)
                        intent.putExtra("scan_result", qrEntity.value)
                        intent.putExtra("scan_type", qrEntity.type)
                        intent.putExtra("qr_image_uri", qrEntity.imagePath)
                        context.startActivity(intent)
                    }
                } else {
                    Log.e("QrScanner", "Lỗi khi lưu QR MyCard")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(25.dp),
            enabled = name.text.isNotEmpty() || phone.text.isNotEmpty()
        ) {
            Text("Create", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun MyOutlinedField(
    label: String,
    value: TextFieldValue,
    icon: Int? = null,
    onChange: (TextFieldValue) -> Unit
) {
    Text(text = label, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it) },
        placeholder = { Text(label, color = Color.Gray) },
        leadingIcon = {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
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
}

private fun buildVCard(
    name: String,
    phone: String,
    email: String,
    address: String,
    birthday: String,
    org: String,
    note: String
): String {
    return """
        BEGIN:VCARD
        VERSION:3.0
        FN:$name
        ORG:$org
        TEL:$phone
        EMAIL:$email
        ADR:$address
        BDAY:$birthday
        NOTE:$note
        END:VCARD
    """.trimIndent()
}
