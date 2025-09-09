//package com.example.myapplication
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.ImageProxy
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import com.google.mlkit.vision.barcode.BarcodeScanning
//import com.google.mlkit.vision.barcode.common.Barcode
//import com.google.mlkit.vision.common.InputImage
//import java.util.concurrent.Executors
//
//@Composable
//fun QrScannerScreen(onScanned: (String) -> Unit) {
//    val context = LocalContext.current
//    var hasPermission by remember {
//        mutableStateOf(
//            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
//                    PackageManager.PERMISSION_GRANTED
//        )
//    }
//
//    // Xin quyền
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission(),
//        onResult = { granted ->
//            hasPermission = granted
//            if (!granted) {
//                Toast.makeText(context, "Cần quyền camera", Toast.LENGTH_SHORT).show()
//            }
//        }
//    )
//
//    LaunchedEffect(Unit) {
//        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
//    }
//
//    if (hasPermission) {
//        AndroidView(
//            factory = { ctx ->
//                val previewView = PreviewView(ctx)
//
//                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
//                cameraProviderFuture.addListener({
//                    val cameraProvider = cameraProviderFuture.get()
//
//                    // Preview
//                    val preview = androidx.camera.core.Preview.Builder().build().apply {
//                        setSurfaceProvider(previewView.surfaceProvider)
//                    }
//
//                    // Analyzer
//                    val analyzer = ImageAnalysis.Builder()
//                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                        .build()
//
//                    val scanner = BarcodeScanning.getClient()
//                    val executor = Executors.newSingleThreadExecutor()
//
//                    analyzer.setAnalyzer(executor) { imageProxy: ImageProxy ->
//                        processImageProxy(scanner, imageProxy, onScanned)
//                    }
//
//                    try {
//                        cameraProvider.unbindAll()
//                        cameraProvider.bindToLifecycle(
//                            ctx as ComponentActivity,
//                            CameraSelector.DEFAULT_BACK_CAMERA,
//                            preview,
//                            analyzer
//                        )
//                    } catch (e: Exception) {
//                        Log.e("QrScanner", "Camera bind failed", e)
//                    }
//                }, ContextCompat.getMainExecutor(ctx))
//
//                previewView
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//    } else {
//        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
//            Text("Chưa có quyền camera")
//        }
//    }
//}
//
//private fun processImageProxy(
//    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
//    imageProxy: ImageProxy,
//    onScanned: (String) -> Unit
//) {
//    val mediaImage = imageProxy.image
//    if (mediaImage != null) {
//        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//        scanner.process(image)
//            .addOnSuccessListener { barcodes ->
//                for (barcode in barcodes) {
//                    if (barcode.valueType == Barcode.TYPE_TEXT || barcode.valueType == Barcode.TYPE_URL) {
//                        onScanned(barcode.rawValue ?: "")
//                    }
//                }
//            }
//            .addOnFailureListener {
//                Log.e("QrScanner", "Scan error", it)
//            }
//            .addOnCompleteListener {
//                imageProxy.close()
//            }
//    } else {
//        imageProxy.close()
//    }
//}
