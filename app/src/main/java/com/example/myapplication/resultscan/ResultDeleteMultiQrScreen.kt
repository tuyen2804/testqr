package com.example.myapplication.resultscan

import android.app.Activity
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
fun ResultDeleteMultiQrScreen(viewModel: ResultMultiQrViewModel) {
    val qrList = viewModel.qrList
    val selectedItems = viewModel.selectedItems.value
    val showDetail = viewModel.showDetailQr.value
    val allSelected = selectedItems.size == qrList.size && qrList.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Delete") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.exitDeleteMode() }) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            tint = Color.White,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { viewModel.toggleSelectAll() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2196F3),
                            uncheckedColor = Color.Gray
                        )
                    )
                    IconButton(onClick = { viewModel.deleteSelected() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete selected items"
                        )
                    }
                }
            )
        }
    ) { padding ->
        QrListSelectScreen(
            isShowDetail = showDetail,
            qrList = qrList,
            selectedItems = selectedItems,
            onSelectedItemsChange = { index -> viewModel.toggleItem(index) },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun QrListSelectScreen(
    isShowDetail: Boolean,
    qrList: List<QrCodeInfo>, // Thay đổi thành List<QrCodeInfo>
    selectedItems: Set<Int>,
    onSelectedItemsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(qrList) { index, qrInfo -> // Thay đổi thành qrInfo: QrCodeInfo
            val isSelected = selectedItems.contains(index)
            val toggle: (Boolean) -> Unit = { checked ->
                onSelectedItemsChange(index)
            }

            if (isShowDetail) {
                DetailQrListItemSelect(
                    qrInfo = qrInfo, // Thay đổi thành qrInfo
                    index = index,
                    isSelected = isSelected,
                    onCheckedChange = toggle
                )
            } else {
                QrListItemSelect(
                    qrInfo = qrInfo, // Thay đổi thành qrInfo
                    index = index,
                    isSelected = isSelected,
                    onCheckedChange = toggle
                )
            }
        }
    }
}

@Composable
fun QrListItemSelect(
    qrInfo: QrCodeInfo, // Thay đổi tham số thành QrCodeInfo
    index: Int,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = Color(0xFF2C2C2E),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isSelected) } // Click row để toggle checkbox
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
                    text = getBarcodeTypeString(qrInfo.type), // Hiển thị loại QR thay vì logic if
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "21:37", // Giả lập, bạn có thể lưu timestamp từ QrCodeInfo nếu thêm trường
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onCheckedChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF2196F3),
                    uncheckedColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun DetailQrListItemSelect(
    qrInfo: QrCodeInfo, // Thay đổi tham số thành QrCodeInfo
    index: Int,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = Color(0xFF2C2C2E),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isSelected) } // Click row để toggle checkbox
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon QR
            Icon(
                painter = painterResource(id = R.drawable.qr), // Giữ icon cho detail mode
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
                    text = getBarcodeTypeString(qrInfo.type), // Hiển thị loại QR thay vì logic if
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "21:37", // Giả lập, bạn có thể lưu timestamp từ QrCodeInfo nếu thêm trường
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onCheckedChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF2196F3),
                    uncheckedColor = Color.Gray
                )
            )
        }
    }
}

