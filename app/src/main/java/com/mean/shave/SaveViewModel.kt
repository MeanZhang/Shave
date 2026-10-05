package com.mean.shave

import android.content.Intent
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SaveViewModel(intent: Intent) : ViewModel() {
    val state: StateFlow<State>
        field = MutableStateFlow(State.File)
    val text: StateFlow<String?>
        field = MutableStateFlow<String?>(null)
    val error: StateFlow<String>
        field = MutableStateFlow("")
    val progress: StateFlow<Float?>
        field = MutableStateFlow<Float?>(null)

    init {
        if (intent.type == "text/plain") {
            state.value = State.Text
            text.value = intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            state.value = State.File
        }
    }

    fun setText(value: String?) {
        text.value = value
    }

    fun setState(value: State) {
        state.value = value
        if (value == State.Success) {
            text.value = "保存成功"
        }
    }

    fun setError(value: String) {
        setState(State.Error)
        error.value = value
    }

    fun setProgress(progress: Float) {
        this.progress.value = progress
    }
}
