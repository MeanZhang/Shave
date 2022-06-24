package com.mean.shave.ui.components

import android.content.Context
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.DialogProperties
import com.mean.shave.App
import com.mean.shave.R
import com.mean.shave.openURL
import kotlinx.coroutines.launch

@Composable
fun AgreementDialog(
    context: Context,
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = { onDisagree() },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    App.setNotFirstLaunch()
                }
                onAgree()
            }) {
                Text("同意")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDisagree() }) {
                Text("退出")
            }
        },
        title = { Text("服务协议和隐私政策") },
        text = {
            val annotatedString = buildAnnotatedString {
                append("欢迎使用享存。\n\n我们将通过")
                //TODO: 将来用withAnnotation替代
                pushStringAnnotation("AGREEMENT", stringResource(R.string.agreement))
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("《享存软件许可及服务协议》")
                }
                pop()
                append("、")
                pushStringAnnotation("PRIVACY", stringResource(R.string.privacy))
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("《享存隐私政策》")
                }
                pop()
                append(
                    "帮助您了解我们处理个人信息的方式及您享有的权利。\n\n" +
                            "保护用户信息是我们的一项基本原则，我们不会收集、使用、储存和分享您的任何相关信息。\n\n" +
                            "点击“同意”按钮，即表示您同意上述协议和政策，若不同意，请点击“退出”按钮退出享存。"
                )
            }
            ClickableText(
                text = annotatedString,
                onClick = { position ->
                    annotatedString.getStringAnnotations(
                        "AGREEMENT",
                        start = position,
                        end = position
                    ).firstOrNull()?.let { context.openURL(it.item) }
                    annotatedString.getStringAnnotations(
                        "PRIVACY",
                        start = position,
                        end = position
                    ).firstOrNull()?.let { context.openURL(it.item) }
                })
        })
}