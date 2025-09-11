package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.scan.QrScannerScreen

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

