package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp()
        }
    }
}

@Composable
fun MainApp() {
    var selectedTab by remember { mutableStateOf("scan") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "scan",
                    onClick = { selectedTab = "scan" },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.scan), // ảnh trong drawable
                            contentDescription = "Scan",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Scan") }
                )
                NavigationBarItem(
                    selected = selectedTab == "history",
                    onClick = { selectedTab = "history" },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.history),
                            contentDescription = "History",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == "create",
                    onClick = { selectedTab = "create" },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "Create",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Create") }
                )
                NavigationBarItem(
                    selected = selectedTab == "settings",
                    onClick = { selectedTab = "settings" },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.setting),
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Settings") }
                )
            }
        }
    ) { paddingValues ->
        val context = LocalContext.current

        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                "scan" -> QrScannerScreen { result ->
                    Toast.makeText(context, "Scanned: $result", Toast.LENGTH_SHORT).show()
                }
                "history" -> HistoryScreen()
                "create" -> CreateScreen()
                "settings" -> SettingsScreen()
            }
        }
    }
}

@Composable
fun QrScannerScreen(onScanned: (String) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermission) {
            // Camera Preview
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
                            processImageProxy(scanner, imageProxy, onScanned)
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                ctx as ComponentActivity,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analyzer
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("QrScanner", "Camera bind failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay UI
            Box(modifier = Modifier.fillMaxSize()) {
                // Khung quét
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.Center)
                        .border(2.dp, Color.Cyan)
                )

//                val infiniteTransition = rememberInfiniteTransition()
//                val scanOffset by infiniteTransition.animateFloat( initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable( animation = tween(durationMillis = 2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse ) )
//                Box( modifier = Modifier .fillMaxWidth(0.8f) .aspectRatio(1f) .align(Alignment.Center) .background( brush = Brush.verticalGradient( colors = listOf(Color.Transparent, Color.Cyan.copy(alpha = 0.3f), Color.Transparent), startY = 0f, endY = 1000f * scanOffset ) ) )

                // Thanh zoom
                ZoomSliderDemo()

                // 3 nút chức năng
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* mở Gallery */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.gallery),
                            contentDescription = "Gallery",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* bật Flashlight */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.flashlight),
                            contentDescription = "Flashlight",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* Batch scan */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.batch),
                            contentDescription = "Batch",
                            tint = Color.White
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có quyền camera", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoomSliderDemo() {
    var zoom by remember { mutableStateOf(0f) } // state động
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter) // giờ hợp lệ
                .padding(bottom = 120.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("-", color = Color.White, fontSize = 20.sp)

            Slider(
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Zoom Level Slider" },
                value = zoom,
                onValueChange = { zoom = it },
                valueRange = 0f..5f,
                steps = 0,
                onValueChangeFinished = {
                    // launch some business logic update with the state you hold
                },
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        thumbSize = DpSize(40.dp, 40.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White
                        )
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(6.dp),
                        thumbTrackGapSize = 0.dp, // Loại bỏ khoảng trống
                        trackInsideCornerSize = 3.dp,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            )

            Text("+", color = Color.White, fontSize = 20.sp)
        }
    }
}

@Composable
fun HistoryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("History Screen", color = Color.White)
    }
}

@Composable
fun CreateScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Create QR Screen", color = Color.White)
    }
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings Screen", color = Color.White)
    }
}

private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    onScanned(barcode.rawValue ?: "")
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}
