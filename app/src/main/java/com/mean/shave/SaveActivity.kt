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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.mean.shave.ui.theme.ShaveTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SaveActivity : ComponentActivity() {
    private var type = MutableStateFlow(Type.Others)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var sourceUri: Uri? = null
        val text = MutableStateFlow("")
        val errorFlow = MutableStateFlow("")
        val saveLauncher =
            registerForActivityResult(ActivityResultContracts.CreateDocument()) {
                if (it != null) {
                    if (type.value == Type.Text) {
                        save(text.value, it)
                    } else {
                        sourceUri?.let { sourceUri -> save(sourceUri, it) }
                    }
                } else {
                    type.value = Type.Error
                    errorFlow.value = "文件保存失败"
                    Toast.makeText(
                        this,
                        "文件保存失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
                type.value = Type.Others
            }
        }
        setContent {
            val contentType by type.collectAsState()
            val textState by text.collectAsState()
            val error by errorFlow.collectAsState()

            ShaveTheme {

                AlertDialog(
                    properties = DialogProperties(dismissOnClickOutside = false),
                    modifier = Modifier.wrapContentSize(),
                    title = { Text(stringResource(R.string.app_name)) },
                    onDismissRequest = { finish() },
                    confirmButton = {
                        when (contentType) {
                            Type.Text -> {
                                TextButton(onClick = {
                                    saveLauncher.launch(
                                        textState.take(10) + ".txt"
                                    )
                                }) {
                                    Text("保存")
                                }
                            }
                            Type.Error, Type.Others -> {
                                TextButton(onClick = { finish() }) {
                                    Text("确定")
                                }
                            }
                        }
                    },
                    dismissButton = {
                        when (contentType) {
                            Type.Text -> {
                                TextButton(onClick = {
                                    copy(textState)
                                    finish()
                                }) {
                                    Text("复制")
                                }
                            }
                        }
                    },
                    text = {
                        when (contentType) {
                            Type.Text -> {
                                OutlinedTextField(
                                    label = { Text("文本") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Constants.HORIZONTAL_MARGIN),
                                    value = textState,
                                    onValueChange = { text.value = it }
                                )
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
                            Type.Others -> {
                                Text("不支持的分享类型：${intent.type ?: intent.action}")
                            }
                            Type.Error -> {
                                Text(error)
                            }
                        }
                    }
                )
            }
        }
    }

    /**
     * 保存文件
     */
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
                    this@SaveActivity,
                    "保存成功",
                    Toast.LENGTH_SHORT
                ).show()
            }
            finish()
        }
    }

    /**
     * 保存文本
     */
    private fun save(text: String, uri: Uri) {
        type.value = Type.Saving
        lifecycleScope.launch(Dispatchers.IO) {
            val outputStream = contentResolver.openOutputStream(uri)
            outputStream?.write(text.toByteArray())
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SaveActivity,
                    "保存成功",
                    Toast.LENGTH_SHORT
                ).show()
            }
            type.value = Type.Text
        }
    }

    private fun copy(text: String) {
        val manager =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData =
            ClipData.newPlainText("text", text)
        manager.setPrimaryClip(clipData)
        Toast.makeText(
            this@SaveActivity,
            "已复制到剪贴板",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun log(msg: Any?) {
        Log.d("Shave", msg.toString())
    }
}

enum class Type {
    Text, Others, Saving, Error
}