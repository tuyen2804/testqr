package com.example.myapplication

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.createqr.CreateActivity
import com.example.myapplication.history.HistoryActivity
import com.example.myapplication.resultscan.widget.ButtonData

@Composable
fun CreateScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1E26))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Create",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Card History
        CreateCard(
            icon = R.drawable.history,
            title = "History",
            description = null,
            onClick = {
                val intent = Intent(context, HistoryActivity::class.java)
                context.startActivity(intent)                }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card Clipboard
        CreateCard(
            icon = R.drawable.clipboard,
            title = "Clipboard",
            description = "Hôm nay (11/09/2025) e làm...",
            onClick = {
                Toast.makeText(context, "Mở clipboard", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Danh sách button
        val items = listOf(
            ButtonData(R.drawable.link, "Website") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "url") // hoặc wifi, text...
                context.startActivity(intent)            },
            ButtonData(R.drawable.wifi, "Wi-Fi") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "wifi") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.text, "Text") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "text") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.contacts, "Contacts") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "contacts") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.telephone, "Tel") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "tel") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.mail, "E-mail") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "email") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.sms, "SMS") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "sms") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.calendar, "Calendar") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "calendar") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.mycard, "My Card") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "mycard") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.facebook, "Facebook") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "facebook") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.instagram, "Instagram") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "instagram") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.whatsapp, "Whatsapp") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "whatsapp") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.youtube, "Youtube") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "youtube") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.twitter, "Twitter") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "twitter") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.spotify, "Spotify") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "spotify") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.social, "Paypal") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "paypal") // hoặc wifi, text...
                context.startActivity(intent)             },
            ButtonData(R.drawable.viber, "Viber") {
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("screen", "viber") // hoặc wifi, text...
                context.startActivity(intent)             }
        )

        // Hiển thị dạng lưới 3 cột
        for (row in items.chunked(3)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (item in row) {
                    CreateButton(
                        data = item,
                        modifier = Modifier.weight(1f).padding(4.dp)
                    )
                }
                if (row.size < 3) {
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCard(
    icon: Int,
    title: String,
    description: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2C36)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                if (description != null) {
                    Text(description, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun CreateButton(
    data: ButtonData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2C36)),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = data.onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = data.iconRes),
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(data.text, color = Color.White, fontSize = 12.sp)
        }
    }
}
