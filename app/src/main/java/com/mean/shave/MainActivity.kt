package com.mean.shave

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mean.shave.ui.components.AgreementDialog
import com.mean.shave.ui.theme.ShaveTheme

val HORIZONTAL_MARGIN = 16.dp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ShaveTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                SideEffect {
                    systemUiController.setSystemBarsColor(Color.Transparent, useDarkIcons)
                    systemUiController.setNavigationBarColor(Color.Transparent)
                }
                val decayAnimationSpec = rememberSplineBasedDecay<Float>()
                val state = rememberTopAppBarScrollState()
                val scrollBehavior = remember(decayAnimationSpec) {
                    TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec, state)
                }
                var showAgreement by remember { mutableStateOf(App.isFirstLaunch) }
                Scaffold(
                    topBar = { TopBar(scrollBehavior) },
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) { contentPadding ->
                    if (showAgreement) {
                        AgreementDialog(
                            context = this,
                            onAgree = { showAgreement = false },
                            onDisagree = { finish() })
                    } else {
                        Column(
                            Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(contentPadding)
                                .padding(WindowInsets.navigationBars.asPaddingValues())
                                .fillMaxSize()
                        ) {
                            //--------------------------
                            SettingGroupTitle("开发者")
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { openURL(getString(R.string.github_page)) }
                                    .padding(HORIZONTAL_MARGIN, 12.dp)
                                    .fillMaxWidth()
                            ) {
                                Image(
                                    painterResource(R.drawable.avatar),
                                    "开发者头像",
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .height(42.dp)
                                        .clip(CircleShape)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Mean", style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        stringResource(R.string.developer_introduction),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            SettingItem(ImageVector.vectorResource(R.drawable.github),
                                title = "Github 仓库",
                                description = stringResource(
                                    R.string.github_repo
                                ),
                                onClick = { openURL(getString(R.string.github_repo)) })
                            //--------------------------
                            MenuDefaults.Divider()
                            SettingGroupTitle("帮助与反馈")
                            SettingItem(
                                icon = Icons.Outlined.Feedback,
                                title = "反馈",
                                onClick = {
                                    val uri = Uri.parse(getString(R.string.feedback_url))
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    startActivity(intent)
                                }
                            )
                            //--------------------------
                            MenuDefaults.Divider()
                            SettingGroupTitle("隐私")
                            SettingItem(Icons.Outlined.Description, "服务协议",
                                onClick = { openURL(getString(R.string.agreement)) })
                            SettingItem(Icons.Outlined.Verified, "隐私政策",
                                onClick = { openURL(getString(R.string.privacy)) })
                            //--------------------------
                            MenuDefaults.Divider()
                            SettingGroupTitle("开放源代码许可")
                            getLicenses().forEach {
                                LicenseItem(context = this@MainActivity, license = it)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior
) {
    val topBarColors = TopAppBarDefaults.largeTopAppBarColors()
    val scrollFraction = scrollBehavior.scrollFraction
    val statusBarColor by topBarColors.containerColor(scrollFraction)
    Column {
        Spacer(
            modifier = Modifier
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .fillMaxWidth()
                .background(statusBarColor)
        )
        LargeTopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            navigationIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_launcher_foreground),
                    stringResource(R.string.app_name),
                    Modifier.size(60.dp)
                )
            },
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
fun SettingGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(
                horizontal = HORIZONTAL_MARGIN
            )
            .padding(top = 28.dp, bottom = 12.dp)
    )
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = HORIZONTAL_MARGIN, vertical = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier
                .padding(start = 4.dp, end = 20.dp)
                .size(28.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            description?.let {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingItemWinthoutIcon(
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = HORIZONTAL_MARGIN, vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LicenseItem(context: Context?, license: License) {
    Column(
        Modifier
            .clickable {
                context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(license.url)))
            }
            .fillMaxWidth()
            .padding(horizontal = HORIZONTAL_MARGIN, 12.dp)
    ) {
        Text(
            license.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            license.url,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            license.license,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getLicenses(): MutableList<License> {
    val licenses = mutableListOf<License>()
    licenses.add(
        License(
            "Android Jetpack",
            "https://github.com/androidx/androidx",
            "Apache License 2.0"
        )
    )
    licenses.add(
        License(
            "Accompanist",
            "https://github.com/google/accompanist",
            "Apache License 2.0"
        )
    )
    licenses.add(
        License(
            "Kotlin",
            "https://github.com/JetBrains/kotlin",
            "Apache License 2.0"
        )
    )
    licenses.sortBy { it.name }
    return licenses
}

data class License(val name: String, val url: String, val license: String)
