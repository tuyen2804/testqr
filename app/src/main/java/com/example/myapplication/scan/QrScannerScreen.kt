package com.example.myapplication.scan

import android.Manifest
import android.R
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.myapplication.model.QrCodeInfo
import com.example.myapplication.resultscan.ResultMultiQrHostActivity
import com.example.myapplication.resultscan.ResultScanActivity
import com.example.myapplication.room.AppDatabase
import com.google.mlkit.vision.barcode.BarcodeScanning
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen() {

    var isBatchScan by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val dao = remember {
        AppDatabase.getInstance(context).qrScanDao()
    }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var isFlashLight by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var zoom by remember { mutableStateOf(0f) }
    val scannedQRs by remember { mutableStateOf(mutableListOf<QrCodeInfo>()) } // Sử dụng List thay vì Set
    var latestQR by remember { mutableStateOf<Pair<Int, String>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )
    val coroutineScope = rememberCoroutineScope()
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                processImageFromUri(uri, context, isBatchScan, scannedQRs, dao) { qrInfo ->
                    if (qrInfo != null) {
                        if (!isBatchScan) {
                            val intent = Intent(context, ResultScanActivity::class.java)
                            intent.putExtra("scan_result", qrInfo.value )
                            intent.putExtra("scan_type", qrInfo.type)
                            intent.putExtra("qr_image_uri", qrInfo.imageUri.toString())
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(context, ResultScanActivity::class.java)
                            intent.putExtra("scan_result", qrInfo.value )
                            intent.putExtra("scan_type", qrInfo.type)
                            intent.putExtra("qr_image_uri", qrInfo.imageUri.toString())
                            context.startActivity(intent)
                        }
                    } else {
                        Log.d("QrScannerScreen", "Không tìm thấy mã QR trong ảnh")
                    }
                }
            }
        }
    }
    val multiQrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedQrList = result.data?.getParcelableArrayListExtra<QrCodeInfo>("updated_qr_list") ?: arrayListOf()
            scannedQRs.clear()
            scannedQRs.addAll(updatedQrList)
            // Cập nhật latestQR dựa trên danh sách mới
            latestQR = if (updatedQrList.isNotEmpty()) {
                Pair(updatedQrList.size, updatedQrList.last().value)
            } else {
                null
            }
            Log.d("QrScannerScreen", "Cập nhật scannedQRs từ back: size=${updatedQrList.size}")
        }
    }
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
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isScanning = true
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
                            .setTargetResolution(Size(1280, 1280))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        val scanner = BarcodeScanning.getClient()
                        val executor = Executors.newSingleThreadExecutor()
                        analyzer.setAnalyzer(executor) { imageProxy: ImageProxy ->
                            if (isScanning) {
                                processImageProxy(scanner, imageProxy, isBatchScan, ctx, scannedQRs,dao) { isBatch, qrInfo ->
                                    if (!isBatch) {
                                        if (qrInfo?.value != null && qrInfo.imageUri!= null) {
                                            val intent = Intent(ctx, ResultScanActivity::class.java)
                                            intent.putExtra("scan_result", qrInfo.value )
                                            intent.putExtra("scan_type", qrInfo.type) // Update type if needed
                                            intent.putExtra("qr_image_uri", qrInfo.imageUri.toString())
                                            ctx.startActivity(intent)
                                            isScanning=false

                                        }
                                    } else if (qrInfo != null) {
                                        latestQR = Pair(scannedQRs.size, qrInfo.value)
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
                            val intent = Intent(context, ResultMultiQrHostActivity::class.java)
                            intent.putParcelableArrayListExtra("scan_results", ArrayList(scannedQRs))
                            multiQrLauncher.launch(intent)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_media_next),
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
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.myapplication.R.drawable.gallery),
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
                    IconButton(onClick = {
                        isFlashLight=!isFlashLight
                        camera?.cameraControl?.enableTorch(isFlashLight)

                    }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.myapplication.R.drawable.flashlight),
                                contentDescription = "Flashlight",
                                tint = if(isFlashLight)Color.Blue else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Flashlight",
                                color = if(isFlashLight)Color.Blue else Color.White,
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
                                painter = painterResource(id = com.example.myapplication.R.drawable.batch),
                                contentDescription = "Batch",
                                tint = if(isBatchScan) Color.Blue else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Batch",
                                color = if(isBatchScan) Color.Blue else Color.White,
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
        isScanning = true
        Log.d("QrScannerScreen", "Bắt đầu quét")
        onDispose {
            Log.d("QrScannerScreen", "Dừng quét, giải phóng resource")

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


