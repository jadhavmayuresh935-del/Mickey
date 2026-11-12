package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.InstagramAccount
import com.example.data.model.MediaType
import com.example.data.model.Post
import com.example.data.model.PostStatus
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.DraftGray
import com.example.ui.theme.InstagramGradientBrush
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.InstaFlowViewModel

@Composable
fun VisualGridScreen(
    viewModel: InstaFlowViewModel,
    modifier: Modifier = Modifier
) {
    val currentAccount by viewModel.currentAccount.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val isRearrangeMode by viewModel.isGridRearrangeMode.collectAsState()
    val selectedRearrangePostId by viewModel.selectedRearrangePostId.collectAsState()
    val gridFilter by viewModel.gridFilter.collectAsState()

    val accountPosts = allPosts
        .filter { it.accountId == currentAccount?.id }
        .sortedBy { it.gridIndex }

    val displayPosts = when (gridFilter) {
        "Scheduled" -> accountPosts.filter { it.status == PostStatus.SCHEDULED || it.status == PostStatus.DRAFT }
        "Live" -> accountPosts.filter { it.status == PostStatus.PUBLISHED }
        else -> accountPosts
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .testTag("visual_grid_screen"),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Top Profile Simulation Header
        item(span = { GridItemSpan(3) }) {
            ProfileSimulatedHeader(
                account = currentAccount,
                isRearrangeMode = isRearrangeMode,
                gridFilter = gridFilter,
                onToggleRearrange = { viewModel.toggleGridRearrangeMode() },
                onSelectFilter = { viewModel.setGridFilter(it) }
            )
        }

        // Rearrange prompt banner when active
        if (isRearrangeMode) {
            item(span = { GridItemSpan(3) }) {
                Surface(
                    color = BrandSecondary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandSecondary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = BrandSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedRearrangePostId == null)
                                "Tap 1st post to select, then tap 2nd post to swap position"
                            else
                                "Selected 1st post! Now tap another post to swap and rearrange feed aesthetic",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3x3 Grid Items
        items(displayPosts, key = { it.id }) { post ->
            val isSelectedForSwap = selectedRearrangePostId == post.id

            GridPostTile(
                post = post,
                isRearrangeMode = isRearrangeMode,
                isSelectedForSwap = isSelectedForSwap,
                onClick = { viewModel.onGridPostClicked(post) }
            )
        }
    }
}

@Composable
fun ProfileSimulatedHeader(
    account: InstagramAccount?,
    isRearrangeMode: Boolean,
    gridFilter: String,
    onToggleRearrange: () -> Unit,
    onSelectFilter: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Profile Avatar & Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar with Instagram Story Gradient Ring
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(InstagramGradientBrush, CircleShape)
                    .padding(3.dp)
            ) {
                if (account != null && account.avatarResId != 0) {
                    Image(
                        painter = painterResource(id = account.avatarResId),
                        contentDescription = account.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }

            // Stats Columns (Posts, Followers, Following)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatColumn(count = account?.postsCount ?: 0, label = "Posts")
                ProfileStatColumn(count = account?.followersCount ?: 0, label = "Followers")
                ProfileStatColumn(count = account?.followingCount ?: 0, label = "Following")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Name, Category & Bio
        Text(
            text = account?.displayName ?: "Lumina Creative Studio",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = account?.category ?: "Design Studio",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = account?.bio ?: "Curating modern spaces ✨",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
        )
        Text(
            text = account?.website ?: "https://lumina.studio",
            style = MaterialTheme.typography.labelSmall,
            color = BrandPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Story Highlights Simulation
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val highlights = listOf(
                "✨ Drops" to (account?.avatarResId ?: 0),
                "🌿 Studio" to (account?.avatarResId ?: 0),
                "☕ Rituals" to (account?.avatarResId ?: 0),
                "📸 Presets" to (account?.avatarResId ?: 0),
                "🏔️ Field" to (account?.avatarResId ?: 0)
            )
            highlights.forEach { (title, resId) ->
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                                .padding(2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = if (resId != 0) resId else com.example.R.drawable.ic_launcher_background),
                                contentDescription = title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid Controls: Rearrange Toggle & Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rearrange Aesthetics Button
            FilledTonalButton(
                onClick = onToggleRearrange,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isRearrangeMode) BrandSecondary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isRearrangeMode) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("rearrange_grid_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isRearrangeMode) "Done Rearranging" else "Rearrange Grid",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Filter Tabs (All, Scheduled, Live)
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(2.dp)
            ) {
                listOf("All", "Scheduled", "Live").forEach { tab ->
                    val isSelected = gridFilter == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) BrandPrimary else Color.Transparent)
                            .clickable { onSelectFilter(tab) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
fun GridPostTile(
    post: Post,
    isRearrangeMode: Boolean,
    isSelectedForSwap: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isSelectedForSwap) 3.dp else if (isRearrangeMode) 1.dp else 0.dp,
                color = if (isSelectedForSwap) BrandSecondary else if (isRearrangeMode) BrandPrimary.copy(alpha = 0.5f) else Color.Transparent
            )
            .clickable { onClick() }
            .testTag("grid_tile_${post.id}")
    ) {
        Image(
            painter = painterResource(id = post.mediaResIds.first()),
            contentDescription = "Post Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Status badge: Scheduled clock ⏰, Draft 📝
        if (post.status != PostStatus.PUBLISHED) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .background(
                        color = if (post.status == PostStatus.SCHEDULED) WarningAmber else DraftGray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (post.status == PostStatus.SCHEDULED) "⏰" else "📝",
                    fontSize = 10.sp
                )
            }
        }

        // Media type badge (Carousel / Reel)
        if (post.mediaType != MediaType.SINGLE_IMAGE) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (post.mediaType == MediaType.CAROUSEL) "⧉" else "▶",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Swap mode indicator overlay
        if (isRearrangeMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .background(
                        if (isSelectedForSwap) BrandSecondary else Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileStatColumn(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatStats(count),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatStats(count: Int): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
        count >= 1_000 -> "%.1fK".format(count / 1_000.0)
        else -> count.toString()
    }
}
