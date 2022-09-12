package com.mean.shave

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import kotlinx.coroutines.launch

class SaveActivity : ComponentActivity() {
    private val viewModel by viewModels<SaveViewModel> { SaveViewModelFactory(intent) }
    private var saveLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XLog.d("action: %s", intent.action)
        XLog.d("intent type: %s", intent.type)
        saveLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument(intent.type ?: "*/*")
        ) { save(it) }

        if (!App.isFirstLaunch && viewModel.state.value == State.Launching) {
            save()
        }

        lifecycleScope.launch {
            viewModel.state.collect {
                if (it == State.Success) {
                    Toast.makeText(this@SaveActivity, "文件保存成功", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setContent {
            val state by viewModel.state.collectAsState()
            val text by viewModel.text.collectAsState()
            val error by viewModel.error.collectAsState()
            var showAgreement by remember { mutableStateOf(App.isFirstLaunch) }

            ShaveTheme {
                if (showAgreement) {
                    AgreementDialog(
                        context = this,
                        onAgree = {
                            showAgreement = false
                            save()
                        },
                        onDisagree = { finish() }
                    )
                } else {
                    AlertDialog(
                        properties = DialogProperties(dismissOnClickOutside = false),
                        modifier = Modifier.wrapContentSize(),
                        title = {
                            Text(
                                when (state) {
                                    State.Saving -> "保存中"
                                    State.Error -> "错误"
                                    else -> stringResource(R.string.app_name)
                                }
                            )
                        },
                        onDismissRequest = { finish() },
                        confirmButton = {
                            when (state) {
                                State.Text -> {
                                    TextButton(onClick = {
                                        saveLauncher?.launch((text ?: "").take(10) + ".txt")
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
                            when (state) {
                                State.Text -> {
                                    TextButton(onClick = {
                                        viewModel.copy(text ?: "")
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
                            when (state) {
                                State.Text -> {
                                    OutlinedTextField(
                                        label = { Text("文本") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(HORIZONTAL_MARGIN),
                                        value = text ?: "",
                                        onValueChange = { viewModel.setText(it) }
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
                                    Text(error)
                                }

                                else -> {
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun save() {
        when (intent.action) {
            Intent.ACTION_SEND, Intent.ACTION_VIEW -> {
                if ("text/plain" == intent.type) {
                    viewModel.setState(State.Text)
                    if (viewModel.text.value == null) {
                        viewModel.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
                    }
                } else {
                    viewModel.setState(State.Others)
                    if (viewModel.sourceUri != null && viewModel.sourceUri!!.path != null) {
                        val filename =
                            contentResolver.query(viewModel.sourceUri!!, null, null, null, null)
                                ?.use {
                                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                    it.moveToFirst()
                                    it.getString(index)
                                }
                        saveLauncher?.launch(filename)
                    }
                }
            }

            else -> {
                viewModel.setError("不支持的类型")
            }
        }
    }

    /**
     * 将文件保存到 URI 中
     */
    private fun save(uri: Uri?) {
        if (uri != null) {
            lifecycleScope.launch {
                if (viewModel.state.value == State.Text) {
                    viewModel.save(viewModel.text.value ?: "", uri)
                } else {
                    viewModel.sourceUri?.let { sourceUri -> viewModel.save(sourceUri, uri) }
                }
                finish()
            }
        } else {
            viewModel.setError("选择文件失败")
        }
    }
}
