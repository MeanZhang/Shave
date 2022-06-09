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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val HORIZONTAL_MARGIN = 16.dp

class MainActivity : ComponentActivity() {
    private var type = MutableStateFlow(Type.Main)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        var sourceUri: Uri? = null
        val text = MutableStateFlow("")
        val saveLauncher =
            registerForActivityResult(ActivityResultContracts.CreateDocument()) {
                if (it != null) {
                    if (type.value == Type.Text) {
                        save(text.value, it)
                    } else {
                        sourceUri?.let { sourceUri -> save(sourceUri, it) }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "文件保存失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        log(intent.action)
        log(intent.type)
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    type.value = Type.Text
                    text.value = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                } else {
                    type.value = Type.Others
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
                text.value = "不支持的分享类型"
            }
        }
        setContent {
            ShaveTheme {
                val contentType by type.collectAsState()
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
                    val textState by text.collectAsState()
                    Surface(
                        Modifier
                            .padding(contentPadding)
                            .fillMaxSize()
                    ) {
                        when (contentType) {
                            Type.Main -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(WindowInsets.navigationBars.asPaddingValues()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "欢迎使用" + stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                            Type.Text -> {
                                LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues()) {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp),
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
                                        TextField(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(HORIZONTAL_MARGIN),
                                            value = textState,
                                            onValueChange = { text.value = it }
                                        )
                                    }
                                }
                            }
                            Type.Saving -> {
                                Column(
                                    Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(text = "保存中")
                                }
                            }
                            else -> {
                                Text("欢迎使用" + stringResource(R.string.app_name))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun save(sourceUri: Uri, uri: Uri) {
        type.value = Type.Saving
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
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "保存成功",
                    Toast.LENGTH_SHORT
                ).show()
            }
            finish()
        }
    }

    private fun save(text: String, uri: Uri) {
        type.value = Type.Saving
        lifecycleScope.launch(Dispatchers.IO) {
            val outputStream = contentResolver.openOutputStream(uri)
            outputStream?.write(text.toByteArray())
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "保存成功",
                    Toast.LENGTH_SHORT
                ).show()
            }
            finish()
        }
    }

    private fun log(msg: Any?) {
        Log.d("Shave", msg.toString())
    }
}

enum class Type {
    Main, Text, Others, Saving
}
