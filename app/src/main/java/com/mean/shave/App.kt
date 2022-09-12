package com.mean.shave

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shave")

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var isFirstLaunch: Boolean = true

        suspend fun setNotFirstLaunch() {
            isFirstLaunch = false
            context.dataStore.edit {
                it[booleanPreferencesKey("isFirstLaunch")] = false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        initXLog()
        runBlocking {
            context.dataStore.data.first().let {
                isFirstLaunch = it[booleanPreferencesKey("isFirstLaunch")] ?: true
            }
        }
    }

    private fun initXLog() {
        val config = LogConfiguration.Builder()
            .logLevel(if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE)
            // .enableThreadInfo() // 允许打印线程信息
            .enableStackTrace(2) // 允许打印深度为 2 的调用栈信息
            .enableBorder() // 允许打印日志边框
            .build()
        // 默认 TAG 为“X-LOG”
        XLog.init(config)
    }
}
