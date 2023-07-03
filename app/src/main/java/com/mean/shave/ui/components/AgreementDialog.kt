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
    onDisagree: () -> Unit,
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
                Text(stringResource(R.string.agree))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDisagree() }) {
                Text(stringResource(R.string.exit))
            }
        },
        title = { Text(stringResource(R.string.service_agreement_and_privacy_policy)) },
        text = {
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(stringResource(R.string.welcome1))
                }
                // TODO: 将来用withAnnotation替代
                pushStringAnnotation("AGREEMENT", stringResource(R.string.url_website) + "/agreement")
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    ),
                ) {
                    append(stringResource(R.string.title_service_agreement))
                }
                pop()
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(stringResource(R.string.sign_caesura))
                }
                pushStringAnnotation("PRIVACY", stringResource(R.string.url_website) + "/privacy")
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    ),
                ) {
                    append(stringResource(R.string.title_privacy_policy))
                }
                pop()
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(stringResource(R.string.welcome2))
                }
            }
            ClickableText(
                text = annotatedString,
                onClick = { position ->
                    annotatedString.getStringAnnotations(
                        "AGREEMENT",
                        start = position,
                        end = position,
                    ).firstOrNull()?.let { context.openURL(it.item) }
                    annotatedString.getStringAnnotations(
                        "PRIVACY",
                        start = position,
                        end = position,
                    ).firstOrNull()?.let { context.openURL(it.item) }
                },
            )
        },
    )
}
