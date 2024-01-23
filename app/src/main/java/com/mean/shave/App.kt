package com.mean.shave

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        initXLog()
    }

    private fun initXLog() {
        val config =
            LogConfiguration.Builder()
                .logLevel(if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE)
                // .enableThreadInfo() // 允许打印线程信息
                .enableStackTrace(2) // 允许打印深度为 2 的调用栈信息
                .enableBorder() // 允许打印日志边框
                .build()
        // 默认 TAG 为“X-LOG”
        XLog.init(config)
    }
}
