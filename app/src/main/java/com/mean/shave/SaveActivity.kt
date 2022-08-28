package com.mean.shave

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.mean.shave.ui.components.AgreementDialog
import com.mean.shave.ui.theme.ShaveTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SaveActivity : ComponentActivity() {
    private val state = MutableStateFlow(State.Others)
    private val text = MutableStateFlow("")
    private var sourceUri: Uri? = null
    private var saveLauncher: ActivityResultLauncher<String>? = null
    private val error = MutableStateFlow("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XLog.d("action: %s", intent.action)
        XLog.d("intent type: %s", intent.type)
        saveLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument(intent.type ?: "*/*")
        ) { save(it) }

        if (!App.isFirstLaunch) {
            save()
        }
        setContent {
            val contentState by state.collectAsState()
            val textState by text.collectAsState()
            val errorState by error.collectAsState()
            var showAgreement by remember { mutableStateOf(App.isFirstLaunch) }

            ShaveTheme {
                if (showAgreement) {
                    AgreementDialog(
                        context = this,
                        onAgree = {
                            showAgreement = false
                            save()
                        }, onDisagree = { finish() })
                } else {
                    AlertDialog(
                        properties = DialogProperties(dismissOnClickOutside = false),
                        modifier = Modifier.wrapContentSize(),
                        title = {
                            Text(
                                when (contentState) {
                                    State.Saving -> "保存中"
                                    State.Error -> "错误"
                                    else -> stringResource(R.string.app_name)
                                }
                            )
                        },
                        onDismissRequest = { finish() },
                        confirmButton = {
                            when (contentState) {
                                State.Text -> {
                                    TextButton(onClick = {
                                        saveLauncher?.launch(textState.take(10) + ".txt")
                                    }) {
                                        Text("保存")
                                    }
                                }
                                State.Error, State.Others -> {
                                    TextButton(onClick = { finish() }) {
                                        Text("退出")
                                    }
                                }
                                else -> {}
                            }
                        },
                        dismissButton = {
                            when (contentState) {
                                State.Text -> {
                                    TextButton(onClick = {
                                        copy(textState)
                                        finish()
                                    }) {
                                        Text("复制")
                                    }
                                }
                                State.Error -> {
                                    TextButton(onClick = { save() }) {
                                        Text("重试")
                                    }
                                }
                                else -> {}
                            }
                        },
                        text = {
                            when (contentState) {
                                State.Text -> {
                                    OutlinedTextField(
                                        label = { Text("文本") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(HORIZONTAL_MARGIN),
                                        value = textState,
                                        onValueChange = { text.value = it }
                                    )
                                }
                                State.Saving, State.Others -> {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        CircularProgressIndicator(Modifier.size(64.dp))
                                    }
                                }
                                State.Error -> {
                                    Text(errorState)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * 将文件保存到 URI 中
     */
    private fun save(uri: Uri?) {
        if (uri != null) {
            if (state.value == State.Text) {
                save(text.value, uri)
            } else {
                sourceUri?.let { sourceUri -> save(sourceUri, uri) }
            }
        } else {
            state.value = State.Error
            error.value = "文件保存失败"
            Toast.makeText(
                this,
                "文件保存失败",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun save() {
        when (intent.action) {
            Intent.ACTION_SEND, Intent.ACTION_VIEW -> {
                if ("text/plain" == intent.type) {
                    state.value = State.Text
                    text.value = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                } else {
                    state.value = State.Others
                    sourceUri = getUri()
                    if (sourceUri != null && sourceUri!!.path != null) {
                        val filename =
                            contentResolver.query(sourceUri!!, null, null, null, null)?.use {
                                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                it.moveToFirst()
                                it.getString(index)
                            }
                        saveLauncher?.launch(filename)
                    }
                }
            }
            else -> {
                state.value = State.Error
                error.value = "不支持的类型"
            }
        }
    }

    private fun getUri(): Uri? {
        return if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Parcelable::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } as? Uri
            ?: intent.data
    }

    /**
     * 保存文件
     */
    private fun save(sourceUri: Uri, uri: Uri) {
        state.value = State.Saving
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
        state.value = State.Saving
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
            state.value = State.Text
        }
    }

    /**
     * 复制文本到剪贴板
     */
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
}

enum class State {
    Text, Others, Saving, Error
}