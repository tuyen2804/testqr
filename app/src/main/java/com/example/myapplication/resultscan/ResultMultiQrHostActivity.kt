package com.example.myapplication.resultscan

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.QrCodeInfo
import com.example.myapplication.resultscan.viewmodel.ResultMultiQrViewModel
import com.example.myapplication.resultscan.viewmodel.ResultMultiQrViewModelFactory
import com.example.myapplication.ui.theme.MyApplicationTheme

class ResultMultiQrHostActivity : ComponentActivity() {
    private lateinit var viewModel: ResultMultiQrViewModel // Lưu reference để dùng trong callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val qrList = getParcelableArrayListExtraSafely<QrCodeInfo>("scan_results") ?: arrayListOf()

        setContent {
            MyApplicationTheme {
                viewModel = viewModel( // Khởi tạo và lưu reference
                    factory = ResultMultiQrViewModelFactory(qrList)
                )
                ResultMultiQrScreenHost(viewModel)
            }
        }

        onBackPressedDispatcher.addCallback(
            this /* owner */,
            object : OnBackPressedCallback(true /* enabled */) {
                override fun handleOnBackPressed() {
                    // Xử lý back press: Gửi kết quả cập nhật
                    val qrListUpdated = viewModel.getUpdatedQrList()
                    val intent = Intent()
                    intent.putParcelableArrayListExtra("updated_qr_list", ArrayList(qrListUpdated))
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        )
    }


    @Suppress("DEPRECATION")
    inline fun <reified T : Parcelable> getParcelableArrayListExtraSafely(key: String): ArrayList<T>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(key, T::class.java)
        } else {
            intent.getParcelableArrayListExtra(key)
        }
    }
}

@Composable
fun ResultMultiQrScreenHost(viewModel: ResultMultiQrViewModel) {
    BackHandler(enabled = viewModel.inDeleteMode.value) {
        viewModel.exitDeleteMode()
    }

    if (viewModel.inDeleteMode.value) {
        ResultDeleteMultiQrScreen(viewModel)
    } else {
        ResultMultiQrScreen(viewModel)
    }
}