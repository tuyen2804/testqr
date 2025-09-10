package com.example.myapplication

import android.Manifest
import android.content.Intent
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
import com.example.myapplication.resultscan.ResultScanActivity
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
                "scan" -> QrScannerScreen()
                "history" -> HistoryScreen()
                "create" -> CreateScreen()
                "settings" -> SettingsScreen()
            }
        }
    }
}

