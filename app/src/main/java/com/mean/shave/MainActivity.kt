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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ListItem
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
                            ListItem(
                                headlineText = { Text("Mean") },
                                supportingText = { Text(stringResource(R.string.developer_introduction)) },
                                leadingContent = {
                                    Image(
                                        painterResource(R.drawable.avatar),
                                        "开发者头像",
                                        modifier = Modifier
                                            .height(24.dp)
                                            .clip(CircleShape)
                                    )
                                },
                                modifier = Modifier.clickable { openURL(getString(R.string.github_page)) }
                            )
                            ListItem(
                                headlineText = { Text("Github 仓库") },
                                supportingText = { Text(stringResource(R.string.github_repo)) },
                                leadingContent = {
                                    Icon(
                                        ImageVector.vectorResource(R.drawable.github),
                                        "Github 仓库",
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                },
                                modifier = Modifier.clickable { openURL(getString(R.string.github_repo)) }
                            )
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
                            LICENSES.forEach {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit = {}
) {
    ListItem(
        headlineText = { Text(title) },
        supportingText = { description?.let { Text(it) } },
        leadingContent = { Icon(icon, title) },
        modifier = Modifier.clickable { onClick() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseItem(context: Context?, license: License) {
    ListItem(
        headlineText = { Text(license.name) },
        supportingText = { Text(license.url + "\n" + license.license) },
        modifier = Modifier.clickable { context?.openURL(license.url) }
    )
}

private val LICENSES = listOf(
    License(
        "Android Jetpack",
        "https://github.com/androidx/androidx",
        "Apache License 2.0"
    ),
    License(
        "Accompanist",
        "https://github.com/google/accompanist",
        "Apache License 2.0"
    ),
    License(
        "Kotlin",
        "https://github.com/JetBrains/kotlin",
        "Apache License 2.0"
    )
).sortedBy { it.name }

data class License(val name: String, val url: String, val license: String)
