@file:Suppress("ktlint:standard:filename")

package com.mean.shave

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun Context.openURL(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    startActivity(intent)
}
