package com.mean.shave

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shave")

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var isFirstLaunch: Boolean = true

        fun setNotFirstLaunch() {
            isFirstLaunch = false
            runBlocking {
                context.dataStore.edit {
                    it[booleanPreferencesKey("isFirstLaunch")] = false
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        runBlocking {
            context.dataStore.data.first().let {
                isFirstLaunch = it[booleanPreferencesKey("isFirstLaunch")] ?: true
            }
        }
    }
}