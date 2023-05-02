package com.mean.shave

import android.content.Intent
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SaveViewModel(intent: Intent) : ViewModel() {
    private val _state = MutableStateFlow(State.File)
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

    init {
        if (intent.type == "text/plain") {
            _state.value = State.Text
            _text.value = intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            _state.value = State.File
        }
    }

    fun setText(value: String?) {
        _text.value = value
    }

    fun setState(value: State) {
        _state.value = value
        if (value == State.Success) {
            _text.value = "保存成功"
        }
    }

    fun setError(value: String) {
        setState(State.Error)
        _error.value = value
    }

    fun setProgress(progress: Float) {
        _progress.value = progress
    }
}
