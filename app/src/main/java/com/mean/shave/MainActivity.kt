package com.mean.shave

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mean.shave.ui.components.TopBar
import com.mean.shave.ui.theme.ShaveTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val HORIZONTAL_MARGIN = 16.dp

class MainActivity : ComponentActivity() {
    private var type = Type.Main

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        var sourceUri: Uri? = null
        val saveLauncher =
            registerForActivityResult(ActivityResultContracts.CreateDocument()) {
                if (it != null) {
                    sourceUri?.let { sourceUri -> save(sourceUri, it) }
                } else {
                    Toast.makeText(
                        this,
                        "文件保存失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        var text: String? = null
        log(intent.action)
        log(intent.type)
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    type = Type.Text
                    text = intent.getStringExtra(Intent.EXTRA_TEXT)
                } else {
                    type = if (intent.type?.startsWith("image/") == true) {
                        Type.Image
                    } else {
                        Type.Others
                    }
                    sourceUri =
                        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)
                    if (sourceUri != null && sourceUri.path != null) {
                        val filename =
                            contentResolver.query(sourceUri, null, null, null, null)?.use {
                                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                it.moveToFirst()
                                it.getString(index)
                            }
                        saveLauncher.launch(filename)
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                Toast.makeText(this, intent.type, Toast.LENGTH_SHORT).show()
            }
            else -> {
                text = "不支持的分享类型"
            }
        }
        setContent {
            ShaveTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                SideEffect {
                    systemUiController.setSystemBarsColor(Color.Transparent, useDarkIcons)
                    systemUiController.setNavigationBarColor(Color.Transparent)
                }
                val state = rememberTopAppBarScrollState()
                val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(state) }
                Scaffold(
                    topBar = {
                        TopBar(
                            title = stringResource(R.string.app_name),
                            scrollBehavior = scrollBehavior
                        )
                    },
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) { contentPadding ->
                    var textState by remember { mutableStateOf(text ?: "") }
                    LazyColumn(
                        Modifier
                            .padding(contentPadding)
                            .fillMaxWidth()
                    ) {
                        when (type) {
                            Type.Main -> {
                                item {
                                    Text(text = "欢迎使用" + stringResource(R.string.app_name))
                                }
                            }
                            Type.Text -> {
                                if (text != null) {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Button(onClick = {
                                                val manager =
                                                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                                val clipData =
                                                    ClipData.newPlainText("text", textState)
                                                manager.setPrimaryClip(clipData)
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "已复制到剪贴板",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }) {
                                                Text("复制")
                                            }
                                            Button(onClick = {
                                                saveLauncher.launch(
                                                    textState.take(10) + ".txt"
                                                )
                                            }) {
                                                Text("保存")
                                            }
                                        }
                                    }
                                    item {
                                        TextField(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = HORIZONTAL_MARGIN),
                                            value = textState,
                                            onValueChange = { textState = it })
                                    }
                                }
                            }
//                            Type.Image -> {
//                                item {
//                                    if (sourceUri != null) {
//                                        AsyncImage(model = sourceUri, contentDescription = "Image")
//                                    } else {
//                                        Text("图片打开失败")
//                                    }
//                                }
//                            }
//                            Type.Others -> {
//
//                            }
                            else -> {
                                item {
                                    Text("欢迎使用" + stringResource(R.string.app_name))
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun save(sourceUri: Uri, uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val outputStream = contentResolver.openOutputStream(uri)
            val inputStream = contentResolver.openInputStream(sourceUri)
            val buf = ByteArray(4096)
            var len: Int
            if (inputStream != null && outputStream != null) {
                while (inputStream.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }
                outputStream.close()
                inputStream.close()
            }
            Toast.makeText(this@MainActivity, "保存成功", Toast.LENGTH_SHORT).show()
            if (type != Type.Text) {
                finish()
            }
        }
    }

    private fun log(msg: Any?) {
        Log.d("Shave", msg.toString())
    }
}

enum class Type {
    Main, Text, Image, Others
}
