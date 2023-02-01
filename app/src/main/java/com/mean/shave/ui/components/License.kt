package com.mean.shave.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import com.mean.shave.openURL

data class License(val name: String, val url: String, val license: String)

@Composable
fun LicenseItem(context: Context?, license: License) {
    SettingItem(
        title = license.name,
        description = license.license,
        onClick = {
            context?.openURL(license.url)
        }
    )
}
