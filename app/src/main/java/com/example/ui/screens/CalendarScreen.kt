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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
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
import com.example.data.model.Post
import com.example.data.model.PostStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.InstaFlowViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: InstaFlowViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val currentAccount by viewModel.currentAccount.collectAsState()
    val calendarMode by viewModel.calendarMode.collectAsState()
    val selectedDayMillis by viewModel.selectedCalendarDay.collectAsState()

    var statusFilter by remember { mutableStateOf<PostStatus?>(null) } // null = all

    val accountPosts = posts.filter { it.accountId == currentAccount?.id }

    // Generate days for week/month selector
    val cal = Calendar.getInstance()
    val daysList = remember(calendarMode) {
        val list = mutableListOf<CalendarDayItem>()
        val count = if (calendarMode == "Weekly") 14 else 31
        val startCal = Calendar.getInstance()
        startCal.add(Calendar.DAY_OF_YEAR, -2)

        for (i in 0 until count) {
            val d = Calendar.getInstance().apply {
                timeInMillis = startCal.timeInMillis
                add(Calendar.DAY_OF_YEAR, i)
            }
            list.add(
                CalendarDayItem(
                    timeMillis = d.timeInMillis,
                    dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(d.time),
                    dayNumber = SimpleDateFormat("d", Locale.getDefault()).format(d.time),
                    isToday = isSameDay(d.timeInMillis, System.currentTimeMillis())
                )
            )
        }
        list
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Row with Mode Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Content Calendar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Timeline scheduler for Images, Carousels & Reels",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // View Mode Toggle (Weekly / Monthly)
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                        .padding(3.dp)
                ) {
                    listOf("Weekly", "Monthly").forEach { mode ->
                        val isSelected = calendarMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) BrandPrimary else Color.Transparent)
                                .clickable { viewModel.setCalendarMode(mode) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("calendar_mode_$mode")
                        ) {
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Calendar Day Picker Strip
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)) {
                    val currentMonthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(selectedDayMillis))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentMonthName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(BrandPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Has scheduled items",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(daysList) { dayItem ->
                            val isSelected = isSameDay(dayItem.timeMillis, selectedDayMillis)
                            val hasPostsOnDay = accountPosts.any { isSameDay(it.scheduledTimestamp, dayItem.timeMillis) }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) BrandPrimary
                                        else if (dayItem.isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = if (dayItem.isToday && !isSelected) 1.5.dp else 0.dp,
                                        color = if (dayItem.isToday && !isSelected) BrandPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setSelectedCalendarDay(dayItem.timeMillis) }
                                    .padding(vertical = 8.dp)
                                    .testTag("day_item_${dayItem.dayNumber}")
                            ) {
                                Text(
                                    text = dayItem.dayOfWeek,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dayItem.dayNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (hasPostsOnDay) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (isSelected) Color.White else BrandPrimary,
                                                CircleShape
                                            )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All (${accountPosts.size})") }
                )
                FilterChip(
                    selected = statusFilter == PostStatus.SCHEDULED,
                    onClick = { statusFilter = PostStatus.SCHEDULED },
                    label = { Text("Scheduled (${accountPosts.count { it.status == PostStatus.SCHEDULED }})") }
                )
                FilterChip(
                    selected = statusFilter == PostStatus.PUBLISHED,
                    onClick = { statusFilter = PostStatus.PUBLISHED },
                    label = { Text("Published (${accountPosts.count { it.status == PostStatus.PUBLISHED }})") }
                )
                FilterChip(
                    selected = statusFilter == PostStatus.DRAFT,
                    onClick = { statusFilter = PostStatus.DRAFT },
                    label = { Text("Drafts (${accountPosts.count { it.status == PostStatus.DRAFT }})") }
                )
            }
        }

        // Selected Day's Scheduled List
        val filteredPosts = accountPosts.filter { post ->
            val matchDay = isSameDay(post.scheduledTimestamp, selectedDayMillis)
            val matchStatus = statusFilter == null || post.status == statusFilter
            matchDay && matchStatus
        }

        val allFilteredForContext = if (filteredPosts.isEmpty()) {
            // If no posts on selected day, show nearby posts matching filter
            accountPosts.filter { statusFilter == null || it.status == statusFilter }
        } else {
            filteredPosts
        }

        item {
            val selectedDateStr = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(selectedDayMillis))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (filteredPosts.isNotEmpty()) "Posts for $selectedDateStr" else "Scheduled Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { viewModel.openCreatePostSheet() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule", fontSize = 12.sp)
                }
            }
        }

        if (allFilteredForContext.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No posts found for this selection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Schedule' above to add a Single Image, Carousel or Reel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(allFilteredForContext) { post ->
                CalendarPostCard(
                    post = post,
                    onClick = { viewModel.inspectPost(post) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun CalendarPostCard(
    post: Post,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("h:mm a • EEE, MMM d", Locale.getDefault())
    val timeStr = dateFormat.format(Date(post.scheduledTimestamp))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .testTag("calendar_post_${post.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(id = post.mediaResIds.first()),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(topStart = 6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(text = post.mediaType.iconEmoji, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (statusBg, statusFg, statusText) = when (post.status) {
                        PostStatus.SCHEDULED -> Triple(WarningAmber.copy(alpha = 0.15f), WarningAmber, "Scheduled ⏰")
                        PostStatus.PUBLISHED -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "Published ✓")
                        PostStatus.DRAFT -> Triple(DraftGray.copy(alpha = 0.15f), DraftGray, "Draft 📝")
                    }

                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = statusFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (post.hashtags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.hashtags.take(3).joinToString(" "),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = BrandPrimary
                    )
                }
            }
        }
    }
}

private data class CalendarDayItem(
    val timeMillis: Long,
    val dayOfWeek: String,
    val dayNumber: String,
    val isToday: Boolean
)

private fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
