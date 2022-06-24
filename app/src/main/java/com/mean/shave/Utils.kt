package com.mean.shave

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openURL(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}