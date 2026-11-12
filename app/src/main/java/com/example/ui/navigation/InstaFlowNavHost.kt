package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InboxStatus
import com.example.ui.components.AccountHeaderBar
import com.example.ui.components.ConnectAccountDialog
import com.example.ui.components.InviteTeamDialog
import com.example.ui.components.PostDetailDialog
import com.example.ui.screens.*
import com.example.ui.theme.BrandPrimary
import com.example.ui.viewmodel.InstaFlowViewModel

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstaFlowMainScreen(
    viewModel: InstaFlowViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val currentAccount by viewModel.currentAccount.collectAsState()
    val inboxItems by viewModel.inboxItems.collectAsState()
    val unreadInboxCount = inboxItems.count { it.status == InboxStatus.UNREAD }

    val showCreateSheet by viewModel.showCreatePostSheet.collectAsState()
    val inspectedPost by viewModel.inspectedPost.collectAsState()
    val showConnectDialog by viewModel.showConnectAccountDialog.collectAsState()
    val showInviteDialog by viewModel.showInviteTeamDialog.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = selectedTab != 0 || inspectedPost != null) {
        if (inspectedPost != null) {
            viewModel.inspectPost(null)
        } else if (selectedTab != 0) {
            viewModel.selectTab(0)
        }
    }

    val navItems = listOf(
        NavItem("Dashboard", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_dashboard"),
        NavItem("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_calendar"),
        NavItem("Grid", Icons.Filled.GridView, Icons.Outlined.GridView, "nav_grid"),
        NavItem("Inbox", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "nav_inbox"),
        NavItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    Scaffold(
        topBar = {
            AccountHeaderBar(
                currentAccount = currentAccount,
                accounts = accounts,
                unreadCount = unreadInboxCount,
                onAccountSelected = { viewModel.switchAccount(it) },
                onConnectNewAccountClick = { viewModel.openConnectAccountDialog() },
                onCreatePostClick = { viewModel.openCreatePostSheet() },
                onNotificationClick = { viewModel.selectTab(3) }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (item.title == "Inbox" && unreadInboxCount > 0) {
                                        Badge(containerColor = BrandPrimary) {
                                            Text(text = unreadInboxCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPrimary,
                            selectedTextColor = BrandPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> CalendarScreen(viewModel = viewModel)
                2 -> VisualGridScreen(viewModel = viewModel)
                3 -> InboxScreen(viewModel = viewModel)
                4 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Composer Sheet
    if (showCreateSheet) {
        CreatePostDialog(
            mainViewModel = viewModel,
            onDismiss = { viewModel.closeCreatePostSheet() }
        )
    }

    // Post Detail / Feed Inspection Modal
    if (inspectedPost != null) {
        PostDetailDialog(
            post = inspectedPost,
            account = currentAccount,
            onDismiss = { viewModel.inspectPost(null) },
            onPublishNow = { viewModel.publishNow(it) },
            onDeletePost = { viewModel.deletePost(it) }
        )
    }

    // Connect New Account Dialog
    if (showConnectDialog) {
        ConnectAccountDialog(
            onDismiss = { viewModel.closeConnectAccountDialog() },
            onConnect = { username, name, category ->
                viewModel.connectNewAccount(username, name, category)
            }
        )
    }

    // Invite Team Member Dialog
    if (showInviteDialog) {
        InviteTeamDialog(
            onDismiss = { viewModel.closeInviteTeamDialog() },
            onInvite = { name, email, role ->
                viewModel.addTeamMember(name, email, role)
            }
        )
    }
}
