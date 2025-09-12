package com.example.myapplication.resultscan

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.resultscan.widget.ButtonData
import com.example.myapplication.resultscan.widget.CustomButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactResultScreen(result: String, qrImageUri: Uri?) {
    val context = LocalContext.current

    val contactInfo = parseContactContent(result)
    val displayText = buildDisplayText(contactInfo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2F36))
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Contact Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contact",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    // Quay lại màn hình trước đó
                    (context as? Activity)?.finish()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Handle help */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.help),
                        contentDescription = "Help",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* Handle favorite */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = "Favorite",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2C2F36)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3A3D44)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = displayText,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val buttonList = listOf(
                ButtonData(R.drawable.contacts, "Add to contacts") {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        type = ContactsContract.Contacts.CONTENT_TYPE
                        contactInfo.name?.let { putExtra(ContactsContract.Intents.Insert.NAME, it) }
                        contactInfo.phone?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
                        contactInfo.email?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
                    }
                    context.startActivity(Intent.createChooser(intent, "Add to Contacts"))
                },
                ButtonData(R.drawable.send, "Send email") {
                    contactInfo.email?.let { email ->
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$email")
                            putExtra(Intent.EXTRA_SUBJECT, "Contact from QR Code")
                            putExtra(Intent.EXTRA_TEXT, "Hello ${contactInfo.name ?: ""},\n\nContacted via QR code scan.")
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    }
                },
                ButtonData(R.drawable.copy, "Copy") {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Contact Info", displayText)
                    clipboard.setPrimaryClip(clip)
                },
                ButtonData(R.drawable.share, "Share") {
                    // Chia sẻ thông tin liên hệ
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, displayText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Contact"))
                }
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(buttonList) { button ->
                    CustomButton(
                        painter = painterResource(button.iconRes),
                        text = button.text,
                        onClick = button.onClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            qrImageUri?.let {
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Feedback or suggestion",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class ContactInfo(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val organization: String? = null
)

private fun parseContactContent(result: String): ContactInfo {
    if (result.startsWith("BEGIN:VCARD", ignoreCase = true)) {
        val lines = result.split("\n")
        var name: String? = null
        var phone: String? = null
        var email: String? = null
        var organization: String? = null

        for (line in lines) {
            when {
                line.startsWith("N:", ignoreCase = true) -> name = line.removePrefix("N:").trim()
                line.startsWith("TEL:", ignoreCase = true) -> phone = line.removePrefix("TEL:").trim()
                line.startsWith("EMAIL:", ignoreCase = true) -> email = line.removePrefix("EMAIL:").trim()
                line.startsWith("ORG:", ignoreCase = true) -> organization = line.removePrefix("ORG:").trim()
            }
        }
        return ContactInfo(name, phone, email, organization)
    } else if (result.startsWith("MECARD:", ignoreCase = true)) {
        val parts = result.removePrefix("MECARD:").split(";")
        var name: String? = null
        var phone: String? = null
        var email: String? = null
        var organization: String? = null

        for (part in parts) {
            when {
                part.startsWith("N:", ignoreCase = true) -> name = part.removePrefix("N:").trim()
                part.startsWith("TEL:", ignoreCase = true) -> phone = part.removePrefix("TEL:").trim()
                part.startsWith("EMAIL:", ignoreCase = true) -> email = part.removePrefix("EMAIL:").trim()
                part.startsWith("ORG:", ignoreCase = true) -> organization = part.removePrefix("ORG:").trim()
            }
        }
        return ContactInfo(name, phone, email, organization)
    }
    return ContactInfo(name = result)
}

private fun buildDisplayText(contactInfo: ContactInfo): String {
    val parts = mutableListOf<String>()
    contactInfo.name?.let { parts.add("Name: $it") }
    contactInfo.phone?.let { parts.add("Phone: $it") }
    contactInfo.email?.let { parts.add("Email: $it") }
    contactInfo.organization?.let { parts.add("Organization: $it") }
    return if (parts.isNotEmpty()) parts.joinToString("\n") else "No contact information"
}