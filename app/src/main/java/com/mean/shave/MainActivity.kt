package com.mean.shave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.mean.shave.ui.components.License
import com.mean.shave.ui.components.LicenseItem
import com.mean.shave.ui.components.SettingGroupTitle
import com.mean.shave.ui.components.SettingItem
import com.mean.shave.ui.theme.ShaveTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ShaveTheme {
                val state = rememberTopAppBarState()
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)
                Scaffold(
                    topBar = {
                        LargeTopAppBar(
                            title = { Text(text = stringResource(id = R.string.app_name)) },
                            navigationIcon = {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.ic_launcher_foreground),
                                    null,
                                    Modifier.size(60.dp),
                                )
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    },
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                ) { contentPadding ->
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(contentPadding)
                            .fillMaxSize(),
                    ) {
                        // --------------------------
                        SettingGroupTitle(stringResource(R.string.about))
                        SettingItem(
                            icon = Icons.Outlined.NewReleases,
                            title = stringResource(R.string.version),
                            description = BuildConfig.VERSION_NAME,
                        )
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.developer_name)) },
                            supportingContent = { Text(stringResource(R.string.developer_introduction)) },
                            leadingContent = {
                                Image(
                                    painterResource(R.drawable.avatar),
                                    null,
                                    modifier =
                                        Modifier
                                            .height(24.dp)
                                            .clip(CircleShape),
                                )
                            },
                            modifier = Modifier.clickable { openURL(getString(R.string.url_github_page)) },
                        )
                        ListItem(
                            headlineContent = { Text("Github " + stringResource(R.string.repository)) },
                            supportingContent = { Text(stringResource(R.string.url_github_repo)) },
                            leadingContent = {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.github),
                                    null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            modifier = Modifier.clickable { openURL(getString(R.string.url_github_repo)) },
                        )
                        // --------------------------
                        Divider()
                        SettingGroupTitle(stringResource(R.string.help_and_feedback))
                        SettingItem(
                            icon = Icons.Outlined.Help,
                            title = stringResource(R.string.help),
                            onClick = {
                                openURL(getString(R.string.url_website))
                            },
                        )
                        SettingItem(
                            icon = Icons.Outlined.Feedback,
                            title = stringResource(R.string.feedback),
                            onClick = {
                                openURL(getString(R.string.url_feedback))
                            },
                        )
                        // --------------------------
                        Divider()
                        SettingGroupTitle(stringResource(R.string.open_source_licenses))
                        licenses.forEach {
                            LicenseItem(context = this@MainActivity, license = it)
                        }
                    }
                }
            }
        }
    }

    private val licenses =
        listOf(
            License(
                "Android Jetpack",
                "https://github.com/androidx/androidx",
                "Apache License 2.0",
            ),
            License(
                "Kotlin",
                "https://github.com/JetBrains/kotlin",
                "Apache License 2.0",
            ),
            License(
                "Material Components for Android",
                "https://github.com/material-components/material-components-android",
                "Apache License 2.0",
            ),
            License(
                "XLog",
                "https://github.com/elvishew/xLog",
                "Apache License 2.0",
            ),
            License(
                "Spotless",
                "https://github.com/diffplug/spotless",
                "Apache License 2.0",
            ),
            License("ktlint", "https://github.com/pinterest/ktlint", "MIT License"),
        ).sortedBy { it.name }
}
