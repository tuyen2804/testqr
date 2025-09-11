package com.example.myapplication.resultscan

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.history.getBarcodeTypeString
import com.example.myapplication.model.QrCodeInfo
import com.example.myapplication.resultscan.viewmodel.ResultMultiQrViewModel
import com.google.mlkit.vision.barcode.common.Barcode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultMultiQrScreen(viewModel: ResultMultiQrViewModel) {
    val qrList = viewModel.qrList
    val showDetail = viewModel.showDetailQr.value
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${qrList.size} Code") },
                navigationIcon = {
                    IconButton(onClick = {  val qrListUpdated = viewModel.getUpdatedQrList()
                        val intent = Intent()
                        intent.putParcelableArrayListExtra("updated_qr_list", ArrayList(qrListUpdated))
                        (context as? Activity)?.setResult(RESULT_OK, intent)
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            tint = Color.Black,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDetail() }) {
                        Icon(
                            painter = painterResource(
                                id = if (showDetail) R.drawable.information else R.drawable.text
                            ),
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { viewModel.enterDeleteMode() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        QrListScreen(
            isShowDetail = showDetail,
            qrList = qrList,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun QrListScreen(isShowDetail: Boolean, qrList: List<QrCodeInfo>, modifier: Modifier = Modifier) { // Thay đổi thành List<QrCodeInfo>
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(qrList) { index, qrInfo ->
            if (isShowDetail) DetailQrListItem(qrInfo, index, onClick = {
                val intent = Intent(context, ResultMultiQrDetailActivity::class.java)
                intent.putExtra("scan_result", qrInfo.value)
                intent.putExtra("scan_type", qrInfo.type)
                intent.putExtra("qr_image_uri", qrInfo.imageUri)
                context.startActivity(intent)
            }) else QrListItem(qrInfo, index, onClick = {
                val intent = Intent(context, ResultScanActivity::class.java)
                intent.putExtra("scan_result", qrInfo.value)
                intent.putExtra("scan_type", qrInfo.type)
                intent.putExtra("qr_image_uri", qrInfo.imageUri)
                context.startActivity(intent)
            })
        }
    }
}

@Composable
fun QrListItem(qrInfo: QrCodeInfo, index: Int, onClick: () -> Unit) { // Thay đổi tham số thành QrCodeInfo
    Surface(
        color = Color(0xFF2C2C2E),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon QR
            Icon(
                painter = painterResource(id = R.drawable.share),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = qrInfo.value, // Hiển thị nội dung QR
                    color = Color.White,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = getBarcodeTypeString(qrInfo.type), // Hiển thị loại QR thay vì hardcode "URL"
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "21:37", // Giả lập, bạn có thể lưu timestamp từ QrCodeInfo nếu thêm trường
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun DetailQrListItem(qrInfo: QrCodeInfo, index: Int, onClick: () -> Unit) { // Thay đổi tham số thành QrCodeInfo
    Surface(
        color = Color(0xFF2C2C2E),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon QR
            Icon(
                painter = painterResource(id = R.drawable.qr),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = qrInfo.value, // Hiển thị nội dung QR
                    color = Color.White,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = getBarcodeTypeString(qrInfo.type), // Hiển thị loại QR thay vì hardcode "URL"
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "21:37",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

