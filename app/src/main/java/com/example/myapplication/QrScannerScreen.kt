package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.room.Room
import com.example.myapplication.resultscan.ResultScanActivity
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.room.dao.QrScanDao
import com.example.myapplication.room.entity.QrScanEntity
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen() {
    var isBatchScan by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var zoom by remember { mutableStateOf(0f) }
    val scannedQRs by remember { mutableStateOf(mutableSetOf<String>()) }
    var latestQR by remember { mutableStateOf<Pair<Int, String>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(zoom) {
        camera?.cameraControl?.setZoomRatio(1f + zoom)
    }

    LaunchedEffect(isBatchScan) {
        isScanning = true
        scannedQRs.clear()
        latestQR = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val analyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        val scanner = BarcodeScanning.getClient()
                        val executor = Executors.newSingleThreadExecutor()
                        analyzer.setAnalyzer(executor) { imageProxy: ImageProxy ->
                            if (isScanning) {
                                processImageProxy(scanner, imageProxy, isBatchScan, ctx, scannedQRs) { isBatch, qrValue, qrUri ->
                                    if (!isBatch) {
                                        isScanning = false
                                    } else if (qrValue != null) {
                                        latestQR = Pair(scannedQRs.size, qrValue)
                                    }
                                    if (qrValue != null && qrUri != null) {

                                        val intent = Intent(ctx, ResultScanActivity::class.java)
                                        intent.putExtra("scan_result", qrValue)
                                        intent.putExtra("scan_type", Barcode.TYPE_TEXT) // Update type if needed
                                        intent.putExtra("qr_image_uri", qrUri.toString())
                                        ctx.startActivity(intent)
                                    }
                                }
                            } else {
                                imageProxy.close()
                            }
                        }
                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                ctx as ComponentActivity,
                                CameraSelector.Builder()
                                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                    .build(),
                                preview,
                                analyzer
                            )
                        } catch (e: Exception) {
                            Log.e("QrScanner", "Camera bind failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (isBatchScan && latestQR != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(0.9f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "#${latestQR!!.first}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = latestQR!!.second,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = {
                            val intent = Intent(context, ResultScanActivity::class.java)
                            intent.putExtra("scan_result", latestQR!!.second)
                            context.startActivity(intent)
                        }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_media_next),
                                contentDescription = "View QR Details",
                                tint = Color.White
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.Center)
                        .border(2.dp, Color.Cyan)
                )

                ZoomSliderDemo(
                    zoom = zoom,
                    onZoomChange = { newZoom -> zoom = newZoom }
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Open Gallery */ }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.gallery),
                                contentDescription = "Gallery",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Gallery",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                    IconButton(onClick = { /* Toggle Flashlight */ }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.flashlight),
                                contentDescription = "Flashlight",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Flashlight",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                    IconButton(onClick = {
                        isBatchScan = !isBatchScan
                    }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.batch),
                                contentDescription = "Batch",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Batch",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có quyền camera", color = Color.White)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isScanning = true
            scannedQRs.clear()
            latestQR = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoomSliderDemo(zoom: Float, onZoomChange: (Float) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "-",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable {
                        onZoomChange((zoom - 0.5f).coerceAtLeast(0f))
                    }
                    .padding(8.dp)
            )

            Slider(
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Zoom Level Slider" },
                value = zoom,
                onValueChange = { onZoomChange(it) },
                valueRange = 0f..5f,
                steps = 0,
                onValueChangeFinished = { },
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        thumbSize = DpSize(10.dp, 10.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Blue
                        ),
                        modifier = Modifier.offset(y = 3.dp)
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(4.dp),
                        thumbTrackGapSize = 0.dp,
                        trackInsideCornerSize = 3.dp,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color.Blue,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            )

            Text(
                text = "+",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable {
                        onZoomChange((zoom + 0.5f).coerceAtMost(5f))
                    }
                    .padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    isBatchScan: Boolean,
    context: android.content.Context,
    scannedQRs: MutableSet<String>,
    onProcessed: (Boolean, String?, Uri?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (isBatchScan) {
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: continue
                        if (!scannedQRs.contains(rawValue)) {
                            scannedQRs.add(rawValue)
                            val qrUri = extractQRImage(imageProxy, barcode, context)
                            onProcessed(true, rawValue, qrUri)
                        }
                    }
                    onProcessed(true, null, null)
                } else {
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes[0]
                        val rawValue = barcode.rawValue ?: return@addOnSuccessListener
                        if (!scannedQRs.contains(rawValue)) {
                            scannedQRs.add(rawValue)
                            val qrUri = extractQRImage(imageProxy, barcode, context)
                            val db = Room.databaseBuilder(
                                context,
                                AppDatabase::class.java,
                                "my_app_db"
                            ).build()

                            val dao = db.qrScanDao()

                            CoroutineScope(Dispatchers.IO).launch {
                                dao.insert(
                                    QrScanEntity(
                                        value = rawValue,
                                        type = barcode.valueType,
                                        imagePath = qrUri?.path ?: ""
                                    )
                                )
                            }
                            onProcessed(false, rawValue, qrUri)
                        }
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

@OptIn(ExperimentalGetImage::class)
private fun extractQRImage(imageProxy: ImageProxy, barcode: Barcode, context: android.content.Context): Uri? {
    try {
        val mediaImage = imageProxy.image ?: return null
        // Convert ImageProxy to Bitmap
        val bitmap = imageProxyToBitmap(imageProxy) ?: return null
        val boundingBox = barcode.boundingBox ?: return null
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            boundingBox.left.coerceAtLeast(0),
            boundingBox.top.coerceAtLeast(0),
            boundingBox.width().coerceAtMost(bitmap.width - boundingBox.left),
            boundingBox.height().coerceAtMost(bitmap.height - boundingBox.top)
        )
//        // Save cropped Bitmap to temporary file
//        val tempFile = File(context.cacheDir, "qr_${System.currentTimeMillis()}.png")
//        tempFile.outputStream().use { output ->
//            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
//        }
//        // Create URI from temporary file
//        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
        val imagePath = saveBitmapToInternalStorage(context, croppedBitmap)
        return if (imagePath != null) Uri.fromFile(File(imagePath)) else null

    } catch (e: Exception) {
        Log.e("QrScanner", "Failed to extract QR image", e)
        return null
    }
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    val image = imageProxy.image ?: return null
    if (image.format != ImageFormat.YUV_420_888) return null

    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = android.graphics.YuvImage(
        nv21, ImageFormat.NV21, image.width, image.height, null
    )
    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 100, out)
    val jpegBytes = out.toByteArray()
    var bitmap = android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

    val matrix = android.graphics.Matrix().apply {
        postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
    }
    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    return bitmap
}
private fun saveBitmapToInternalStorage(
    context: android.content.Context,
    bitmap: Bitmap
): String? {
    return try {
        val filename = "qr_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, filename)
        file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        file.absolutePath  // trả về path để lưu DB
    } catch (e: Exception) {
        Log.e("QrScanner", "Error saving bitmap", e)
        null
    }
}

