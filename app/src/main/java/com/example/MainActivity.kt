package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.SmsViewModel
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.SentScreen
import com.example.ui.theme.AppWhite
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeContainer
import com.example.ui.theme.PrimaryOrangeGradient
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: SmsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure edge-to-edge with transparent navigation bar and light icon contrast
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Disable OS dark scrim enforcement on 3-button navigation devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        
        // Ensure seamless background blending on all device configurations
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: SmsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val filteredMessages by viewModel.filteredMessages.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val latestOtp by viewModel.latestOtp.collectAsStateWithLifecycle()
    val sentMessages by viewModel.sentMessages.collectAsStateWithLifecycle()

    var hasSmsPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_SMS] == true ||
                permissions[Manifest.permission.RECEIVE_SMS] == true
        hasSmsPermissions = granted
        if (granted) {
            viewModel.refreshMessages()
        }
    }

    fun requestPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    LaunchedEffect(Unit) {
        viewModel.snackBarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAF7F5),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(
                color = AppWhite,
                shadowElevation = 1.dp
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryOrangeContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedTab == AppTab.INBOX) {
                                    Icon(
                                        imageVector = Icons.Outlined.Inbox,
                                        contentDescription = "Inbox",
                                        tint = PrimaryOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_sent_message),
                                        contentDescription = "Sent",
                                        tint = PrimaryOrange,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (selectedTab) {
                                    AppTab.INBOX -> stringResource(R.string.inbox_tab)
                                    AppTab.SENT -> stringResource(R.string.sent_tab)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp,
                                color = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = AppWhite
                    )
                )
            }
        },
        bottomBar = {
            ModernFloatingNavBar(
                selectedTab = selectedTab,
                onTabSelect = viewModel::selectTab,
                inboxOtpCount = filteredMessages.count { it.otpDetails != null }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "TabTransition") { tab ->
                when (tab) {
                    AppTab.INBOX -> InboxScreen(
                        messages = filteredMessages,
                        latestOtp = latestOtp,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        hasPermission = hasSmsPermissions,
                        onRequestPermission = { requestPermissions() },
                        onSearchChange = viewModel::setSearchQuery,
                        onCategorySelect = viewModel::selectCategory,
                        onRefresh = viewModel::refreshMessages,
                        onCopyOtp = { code -> viewModel.copyToClipboard(context, code) },
                        onDeleteMessage = viewModel::deleteMessage
                    )
                    AppTab.SENT -> SentScreen(
                        sentMessages = sentMessages,
                        onSendSms = { recipient, body -> viewModel.sendRealSms(recipient, body) },
                        onDeleteSentMessage = viewModel::deleteSentMessage,
                        onCopyText = { text -> viewModel.copyToClipboard(context, text, "Message Text") }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernFloatingNavBar(
    selectedTab: AppTab,
    onTabSelect: (AppTab) -> Unit,
    inboxOtpCount: Int,
    modifier: Modifier = Modifier
) {
    // Seamless floating bar container that avoids hard edges above 3-button or gesture navigation
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 36.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, Color(0xFFE8E0D9)),
            shadowElevation = 0.dp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inbox Tab Item
                ModernNavBarItem(
                    title = "Inbox",
                    isSelected = selectedTab == AppTab.INBOX,
                    onClick = { onTabSelect(AppTab.INBOX) },
                    badgeCount = inboxOtpCount,
                    icon = { isSelected ->
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.Inbox else Icons.Outlined.Inbox,
                            contentDescription = "Inbox",
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) Color.White else TextSecondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_inbox")
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Sent Tab Item
                ModernNavBarItem(
                    title = "Sent",
                    isSelected = selectedTab == AppTab.SENT,
                    onClick = { onTabSelect(AppTab.SENT) },
                    badgeCount = 0,
                    icon = { isSelected ->
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sent_message),
                            contentDescription = "Sent",
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) Color.White else TextSecondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_sent")
                )
            }
        }
    }
}

@Composable
fun ModernNavBarItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int,
    icon: @Composable (isSelected: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isSelected) PrimaryOrangeGradient
                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 7.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = if (isSelected) Color.White else PrimaryOrange,
                            contentColor = if (isSelected) PrimaryOrange else Color.White
                        ) {
                            Text(
                                text = "$badgeCount",
                                fontWeight = FontWeight.Black,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            ) {
                icon(isSelected)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}
