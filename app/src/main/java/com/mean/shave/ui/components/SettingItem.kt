package com.mean.shave.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SettingItem(
    title: String,
    icon: ImageVector? = null,
    description: String? = null,
    onClick: () -> Unit = {},
) {
    if (icon != null) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { description?.let { Text(it) } },
            leadingContent = { Icon(icon, null) },
            modifier = Modifier.clickable { onClick() },
        )
    } else {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { description?.let { Text(it) } },
            modifier = Modifier.clickable { onClick() },
        )
    }
}
