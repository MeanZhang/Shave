package com.mean.shave

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SaveViewModel(intent: Intent) : ViewModel() {
    private val _state = MutableStateFlow(State.Launching)
    private val _text = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow("")
    private val _progress = MutableStateFlow<Float?>(null)

    val state: StateFlow<State>
        get() = _state
    val text: StateFlow<String?>
        get() = _text
    val error: StateFlow<String>
        get() = _error
    val progress: StateFlow<Float?>
        get() = _progress

    val sourceUri = if (Build.VERSION.SDK_INT >= 33) {
        intent.getParcelableExtra(Intent.EXTRA_STREAM, Parcelable::class.java)
    } else {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra(Intent.EXTRA_STREAM)
    } as? Uri ?: intent.data

    /**
     * 保存文件
     */
    fun save(sourceUri: Uri, uri: Uri) {
        setState(State.Saving)
        viewModelScope.launch(Dispatchers.IO) {
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
                            XLog.d("file size: %d", totalBytes)
                            var bytesCopied: Long = 0
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var bytes = inputStream.read(buffer)
                            while (bytes >= 0) {
                                outputStream.write(buffer, 0, bytes)
                                bytesCopied += bytes
                                totalBytes?.let {
                                    _progress.value = bytesCopied.toFloat() / totalBytes
                                }
                                bytes = inputStream.read(buffer)
                            }
                            setState(State.Success)
                            XLog.d("文件保存成功")
                        } else {
                            setError("无法打开文件")
                        }
                    }
                } else {
                    setError("无法打开源文件")
                }
            }
        }
    }

    /**
     * 保存文本
     */
    fun save(text: String, uri: Uri) {
        setState(State.Saving)
        viewModelScope.launch(Dispatchers.IO) {
            App.context.contentResolver.openOutputStream(uri).use {
                if (it != null) {
                    it.write(text.toByteArray())
                    setState(State.Success)
                } else {
                    setError("无法打开文件")
                }
            }
            setState(State.Success)
        }
    }

    /**
     * 复制文本到剪贴板
     */
    fun copy(text: String) {
        val manager =
            App.context.getSystemService(ComponentActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        manager.setPrimaryClip(clipData)
        Toast.makeText(
            App.context,
            "已复制到剪贴板",
            Toast.LENGTH_SHORT,
        ).show()
    }

    fun setText(value: String?) {
        _text.value = value
    }

    fun setState(value: State) {
        _state.value = value
    }

    fun setError(value: String) {
        setState(State.Error)
        _error.value = value
    }
}
