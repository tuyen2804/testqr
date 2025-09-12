package com.example.myapplication.resultscan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.history.getBarcodeTypeString
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.mlkit.vision.barcode.common.Barcode
import java.io.File

class ResultMultiQrDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = intent.getIntExtra("scan_type", Barcode.TYPE_TEXT)
        val qrImageUri = intent.getStringExtra("qr_image_uri")?.let { Uri.parse(it) }
        Log.d("ResultMultiQrDetailActivity", "qrImageUri: $qrImageUri, scheme: ${qrImageUri?.scheme}")
        enableEdgeToEdge()

        val qrValue = intent.getStringExtra("scan_result") ?: "https://example.com"

        setContent {
            MyApplicationTheme {
                DetailQrScreen(
                    qrType = getBarcodeTypeString(type),
                    qrValue = qrValue,
                    imageQrUri = qrImageUri,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailQrScreen(qrType: String, qrValue: String, imageQrUri: Uri?, onBack: () -> Unit) {
    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && imageQrUri != null) {
            try {
                val bitmap = if (imageQrUri.scheme == "file") {
                    BitmapFactory.decodeFile(imageQrUri.path)
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, imageQrUri)
                }
                if (bitmap != null) {
                    saveBitmapToGallery(context, bitmap)
                } else {
                    Toast.makeText(context, "Không thể tải ảnh QR", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DetailQrScreen", "Lỗi khi lấy bitmap: ${e.message}", e)
                Toast.makeText(context, "Lỗi tải ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Bạn cần cấp quyền để lưu ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(qrType, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* settings */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.star),
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            imageQrUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            } ?: Text(
                text = "Unable to display QR code",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = qrValue,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            imageQrUri?.let { uri ->
                                try {
                                    Log.d("DetailQrScreen", uri.toString())

                                    val bitmap = if (uri.scheme == "file") {
                                        BitmapFactory.decodeFile(uri.path)
                                    } else {
                                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                    }

                                    if (bitmap != null) {
                                        saveBitmapToGallery(context, bitmap)
                                    } else {
                                        Toast.makeText(context, "Không thể tải ảnh QR", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("DetailQrScreen", "Lỗi khi lấy bitmap: ${e.message}", e)
                                    Toast.makeText(context, "Lỗi tải ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } ?: run {
                                Toast.makeText(context, "Không có ảnh QR để lưu", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            launcher.launch(permission)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.save),
                        contentDescription = "Save",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Save", color = Color.White)
                }

                Button(
                    onClick = {
                        if (imageQrUri != null) {
                            try {
                                // Read bitmap from Uri
                                val inputStream = context.contentResolver.openInputStream(imageQrUri)
                                val bitmap = BitmapFactory.decodeStream(inputStream)

                                if (bitmap != null) {
                                    // Create cache/images directory
                                    val cachePath = File(context.cacheDir, "images")
                                    cachePath.mkdirs()

                                    // Write bitmap to temporary file
                                    val file = File(cachePath, "qr_share.png")
                                    file.outputStream().use { stream ->
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                    }

                                    // Log file details
                                    Log.d("DetailQrScreen", "File path: ${file.absolutePath}, exists: ${file.exists()}")

                                    // Get contentUri via FileProvider
                                    val contentUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    Log.d("DetailQrScreen", "Content URI: $contentUri")

                                    // Share Intent
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/*"
                                        putExtra(Intent.EXTRA_STREAM, contentUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ QR Code"))
                                } else {
                                    Toast.makeText(context, "Không thể đọc QR", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("DetailQrScreen", "Lỗi share: ${e.message}", e)
                                Toast.makeText(context, "Không thể chia sẻ ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Không có ảnh QR để chia sẻ", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.share),
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Share", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}