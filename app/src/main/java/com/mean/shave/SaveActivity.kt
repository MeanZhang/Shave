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
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.mean.shave.ui.Constants.HORIZONTAL_MARGIN
import com.mean.shave.ui.theme.ShaveTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SaveActivity : ComponentActivity() {
    private val viewModel by viewModels<SaveViewModel> { SaveViewModelFactory(intent) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        XLog.d("action: %s", intent.action)
        XLog.d("intent type: %s", intent.type)

        val sourceUri =
            if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Parcelable::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            } as? Uri ?: intent.data

        XLog.d("源文件 URI：$sourceUri")

        val saveLauncher =
            registerForActivityResult(
                ActivityResultContracts.CreateDocument(intent.type ?: "*/*"),
            ) {
                if (it == null) {
                    viewModel.setError(getString(R.string.no_file_selected))
                    Toast.makeText(this, getString(R.string.no_file_selected), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    save(it, sourceUri)
                }
            }

        if (viewModel.state.value == State.Text || viewModel.state.value == State.File) {
            saveOrCopy(saveLauncher, sourceUri)
        }

        setContent {
            val state by viewModel.state.collectAsState()
            val text by viewModel.text.collectAsState()
            val error by viewModel.error.collectAsState()
            val progress by viewModel.progress.collectAsState()

            ShaveTheme {
                AlertDialog(
                    properties = DialogProperties(dismissOnClickOutside = false),
                    modifier = Modifier.wrapContentSize(),
                    title = {
                        Text(
                            when (state) {
                                State.Saving -> stringResource(R.string.saving)
                                State.Error -> stringResource(R.string.error)
                                else -> stringResource(R.string.app_name)
                            },
                        )
                    },
                    onDismissRequest = { finish() },
                    confirmButton = {
                        when (state) {
                            State.Text -> {
                                TextButton(onClick = {
                                    saveLauncher.launch((text ?: "").take(10) + ".txt")
                                }) {
                                    Text(stringResource(R.string.save))
                                }
                            }

                            State.Error, State.File -> {
                                TextButton(onClick = { finish() }) {
                                    Text(stringResource(R.string.exit))
                                }
                            }

                            else -> {}
                        }
                    },
                    dismissButton = {
                        when (state) {
                            State.Text -> {
                                TextButton(onClick = {
                                    copy(text ?: "")
                                    finish()
                                }) {
                                    Text(stringResource(R.string.copy))
                                }
                            }

                            State.Error -> {
                                TextButton(onClick = { saveOrCopy(saveLauncher, sourceUri) }) {
                                    Text(stringResource(R.string.retry))
                                }
                            }

                            else -> {}
                        }
                    },
                    text = {
                        when (state) {
                            State.Text -> {
                                OutlinedTextField(
                                    label = { Text(stringResource(R.string.text)) },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(HORIZONTAL_MARGIN),
                                    value = text ?: "",
                                    onValueChange = { viewModel.setText(it) },
                                )
                            }

                            State.Saving, State.File -> {
                                if (progress != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LinearProgressIndicator(
                                            progress = progress!!,
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .padding(end = 4.dp),
                                        )
                                        Text(
                                            "${(progress!! * 100).toInt()}%".padStart(4),
                                            maxLines = 1,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                    }
                                } else {
                                    LinearProgressIndicator()
                                }
                            }

                            State.Error -> {
                                Text(error)
                            }

                            State.Success -> {
                                Text(stringResource(R.string.save_success))
                            }
                        }
                    },
                )
            }
        }
    }

    private fun saveOrCopy(
        saveLauncher: ActivityResultLauncher<String>,
        sourceUri: Uri?,
    ) {
        // 文本类型
        if (intent.type == "text/plain") {
            viewModel.setState(State.Text)
        } else {
            viewModel.setState(State.File)
            if (sourceUri != null && sourceUri.path != null) {
                val filename =
                    contentResolver.query(sourceUri, null, null, null, null)
                        ?.use {
                            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            it.moveToFirst()
                            it.getString(index)
                        }
                XLog.d("启动文件选择器")
                saveLauncher.launch(filename)
            }
        }
    }

    /**
     * 复制文本到剪贴板
     */
    private fun copy(text: String) {
        val manager =
            App.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        manager.setPrimaryClip(clipData)
        Toast.makeText(
            App.context,
            getString(R.string.copy_success),
            Toast.LENGTH_SHORT,
        ).show()
    }

    /**
     * 将文本或文件保存到 URI 中
     */
    private fun save(
        uri: Uri,
        sourceUri: Uri?,
    ) {
        if (sourceUri == null) {
            saveText(viewModel.text.value ?: "", uri)
        } else {
            saveFile(sourceUri, uri)
        }
    }

    /**
     * 保存文本
     */
    private fun saveText(
        text: String,
        uri: Uri,
    ) {
        viewModel.setState(State.Saving)
        lifecycleScope.launch(Dispatchers.IO) {
            contentResolver.openOutputStream(uri).use {
                if (it != null) {
                    it.write(text.toByteArray())
                    viewModel.setState(State.Success)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@SaveActivity,
                            getString(R.string.save_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    finish()
                } else {
                    viewModel.setError(getString(R.string.unable_to_open_the_file))
                }
            }
            viewModel.setState(State.Success)
        }
    }

    /**
     * 保存文件
     */
    private fun saveFile(
        sourceUri: Uri,
        uri: Uri,
    ) {
        viewModel.setState(State.Saving)
        lifecycleScope.launch(Dispatchers.IO) {
            App.context.contentResolver.openInputStream(sourceUri).use { inputStream ->
                if (inputStream != null) {
                    App.context.contentResolver.openOutputStream(uri).use { outputStream ->
                        if (outputStream != null) {
                            val totalBytes =
                                App.context.contentResolver.query(sourceUri, null, null, null, null)
                                    ?.use {
                                        val index = it.getColumnIndex(OpenableColumns.SIZE)
                                        it.moveToFirst()
                                        it.getLong(index)
                                    }
                            XLog.d("file size: $totalBytes")
                            var bytesCopied: Long = 0
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var bytes = inputStream.read(buffer)
                            while (bytes >= 0) {
                                outputStream.write(buffer, 0, bytes)
                                bytesCopied += bytes
                                totalBytes?.let {
                                    viewModel.setProgress(bytesCopied.toFloat() / totalBytes)
                                }
                                bytes = inputStream.read(buffer)
                            }
                            viewModel.setState(State.Success)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SaveActivity,
                                    getString(R.string.save_success),
                                    Toast.LENGTH_SHORT,
                                )
                                    .show()
                            }
                            XLog.d("保存成功")
                            finish()
                        } else {
                            viewModel.setError(getString(R.string.unable_to_open_the_file))
                        }
                    }
                } else {
                    viewModel.setError(getString(R.string.unable_to_open_source_file))
                }
            }
        }
    }
}
