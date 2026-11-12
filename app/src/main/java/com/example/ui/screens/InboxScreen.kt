package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.InstaFlowViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InboxScreen(
    viewModel: InstaFlowViewModel,
    modifier: Modifier = Modifier
) {
    val inboxItems by viewModel.inboxItems.collectAsState()
    val statusFilter by viewModel.inboxStatusFilter.collectAsState()
    val typeFilter by viewModel.inboxTypeFilter.collectAsState()
    val selectedItem by viewModel.selectedInboxItem.collectAsState()
    val quickReplies = viewModel.repository.quickReplies

    val filteredList = inboxItems.filter { item ->
        val matchStatus = statusFilter == null || item.status == statusFilter
        val matchType = typeFilter == null || item.type == typeFilter
        matchStatus && matchType
    }

    // If an item is selected on mobile, show conversation detail view
    if (selectedItem != null) {
        InboxThreadDetailView(
            item = selectedItem!!,
            quickReplies = quickReplies,
            onBack = { viewModel.selectInboxItem(null) },
            onSendReply = { text -> viewModel.sendInboxReply(selectedItem!!.id, text) },
            onToggleStatus = { viewModel.toggleInboxStatus(selectedItem!!.id) }
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("inbox_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Unified Inbox",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Meta Graph API webhook comments & direct messages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val unreadCount = inboxItems.count { it.status == InboxStatus.UNREAD }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$unreadCount Unread",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Channel Filter: All vs Direct Messages vs Comments
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val channels = listOf(
                        Triple(null, "All Activity", Icons.Outlined.Chat),
                        Triple(InboxType.DIRECT_MESSAGE, "Direct Messages", Icons.Outlined.Send),
                        Triple(InboxType.COMMENT, "Post Comments", Icons.Outlined.Comment)
                    )
                    channels.forEach { (type, label, icon) ->
                        val isSelected = typeFilter == type
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setInboxTypeFilter(type) }
                                .testTag("channel_${label.lowercase().replace(" ", "_")}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Status Filter Chips (All, Unread, Resolved, Flagged)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { viewModel.setInboxStatusFilter(null) },
                        label = { Text("All (${inboxItems.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = statusFilter == InboxStatus.UNREAD,
                        onClick = { viewModel.setInboxStatusFilter(InboxStatus.UNREAD) },
                        label = { Text("Unread (${inboxItems.count { it.status == InboxStatus.UNREAD }})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = statusFilter == InboxStatus.RESOLVED,
                        onClick = { viewModel.setInboxStatusFilter(InboxStatus.RESOLVED) },
                        label = { Text("Resolved (${inboxItems.count { it.status == InboxStatus.RESOLVED }})", fontSize = 11.sp) }
                    )
                }
            }

            // Inbox Messages List
            if (filteredList.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Inbox Zero! All caught up.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "New Instagram comments and DMs will appear here in real time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList) { item ->
                    InboxListItemCard(
                        item = item,
                        onClick = { viewModel.selectInboxItem(item) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun InboxListItemCard(
    item: InboxItem,
    onClick: () -> Unit
) {
    val isUnread = item.status == InboxStatus.UNREAD
    val timeAgo = formatTimeAgo(item.timestamp)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isUnread) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = if (isUnread) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isUnread) 1.5.dp else 1.dp,
                color = if (isUnread) BrandPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .testTag("inbox_item_${item.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = item.senderAvatarRes),
                    contentDescription = item.senderName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.senderName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "@${item.senderUsername}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tag & Channel Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tagBg = when (item.tag) {
                        SenderTag.VIP_CREATOR -> BrandSecondary.copy(alpha = 0.15f)
                        SenderTag.COLLAB_INQUIRY -> BrandPrimary.copy(alpha = 0.15f)
                        SenderTag.CUSTOMER -> InfoBlue.copy(alpha = 0.15f)
                        SenderTag.GENERAL -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val tagFg = when (item.tag) {
                        SenderTag.VIP_CREATOR -> BrandSecondary
                        SenderTag.COLLAB_INQUIRY -> BrandPrimary
                        SenderTag.CUSTOMER -> InfoBlue
                        SenderTag.GENERAL -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(shape = RoundedCornerShape(4.dp), color = tagBg) {
                        Text(
                            text = item.tag.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = tagFg,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = if (item.type == InboxType.DIRECT_MESSAGE) "Direct Message 💬" else "Comment on Post 📸",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = item.messageText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // If post comment, show thumbnail preview snippet
                if (item.postThumbnailRes != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = item.postThumbnailRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.postContextTitle ?: "Post context",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isUnread) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .background(BrandPrimary, CircleShape)
                )
            }
        }
    }
}

@Composable
fun InboxThreadDetailView(
    item: InboxItem,
    quickReplies: List<String>,
    onBack: () -> Unit,
    onSendReply: (String) -> Unit,
    onToggleStatus: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("inbox_thread_detail")
    ) {
        // Detail Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = item.senderAvatarRes),
                            contentDescription = item.senderName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.senderName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${item.senderUsername} • ${item.tag.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandPrimary
                        )
                    }
                }

                // Resolve / Unread Toggle
                TextButton(onClick = onToggleStatus) {
                    val isResolved = item.status == InboxStatus.RESOLVED
                    Text(
                        text = if (isResolved) "Mark Unread" else "Mark Resolved ✓",
                        fontWeight = FontWeight.Bold,
                        color = if (isResolved) BrandPrimary else SuccessGreen
                    )
                }
            }
        }

        // Conversation History
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Context header if comment
            if (item.postThumbnailRes != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = item.postThumbnailRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Commented on post:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = item.postContextTitle ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Sender's original message
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.messageText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatTimeAgo(item.timestamp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Thread replies
            items(item.replies) { reply ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (reply.isFromUs) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = if (reply.isFromUs) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = if (reply.isFromUs) 16.dp else 4.dp
                        ),
                        color = if (reply.isFromUs) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = reply.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (reply.isFromUs) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sent • Just now",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (reply.isFromUs) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Quick Reply Templates Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 8.dp)
        ) {
            Text(
                text = "⚡ Quick Reply Templates (Tap to insert)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickReplies) { template ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { replyText = template }
                    ) {
                        Text(
                            text = template,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Input bar with send button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Reply as @${item.senderUsername.substringBefore(".")}...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = false,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onSendReply(replyText)
                            replyText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(BrandPrimary, CircleShape)
                        .testTag("send_reply_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun formatTimeAgo(timeMillis: Long): String {
    val diff = System.currentTimeMillis() - timeMillis
    val minutes = diff / (60 * 1000)
    val hours = diff / (3600 * 1000)
    val days = diff / (24 * 3600 * 1000)

    return when {
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}
