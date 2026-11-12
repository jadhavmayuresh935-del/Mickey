package com.example.data.model

enum class MediaType(val label: String, val iconEmoji: String) {
    SINGLE_IMAGE("Single Image", "📸"),
    CAROUSEL("Carousel (Multi)", "🎠"),
    REEL("Reel Video", "🎬")
}

enum class PostStatus(val label: String) {
    DRAFT("Draft"),
    SCHEDULED("Scheduled"),
    PUBLISHED("Published")
}

data class InstagramAccount(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val website: String,
    val category: String,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val accountType: String, // "Business" or "Creator"
    val avatarResId: Int,
    val engagementRate: Double,
    val isConnected: Boolean = true
)

data class Post(
    val id: String,
    val accountId: String,
    val caption: String,
    val mediaType: MediaType,
    val mediaResIds: List<Int>,
    val scheduledTimestamp: Long, // unix millis
    val status: PostStatus,
    val gridIndex: Int,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val reachCount: Int = 0,
    val savedCount: Int = 0,
    val hashtags: List<String> = emptyList()
)

data class AnalyticsData(
    val totalFollowers: Int,
    val followerGrowthRate: Double,
    val engagementRate: Double,
    val totalReach: Int,
    val reachGrowthRate: Double,
    val scheduledPostsCount: Int,
    val impressions: Int,
    val followerTrend: List<Pair<String, Int>>, // e.g. "Mon" to 141200
    val dailyReach: List<Pair<String, Int>>,    // e.g. "Mon" to 42000
    val bestTimeToPost: List<String>,          // e.g. "Wed 6:00 PM", "Fri 12:30 PM", "Sun 8:00 PM"
    val topPostTypes: List<Pair<String, Float>> // "Reels" to 0.48f, "Carousels" to 0.34f, "Images" to 0.18f
)

enum class InboxType {
    COMMENT,
    DIRECT_MESSAGE
}

enum class InboxStatus {
    UNREAD,
    RESOLVED,
    FLAGGED
}

enum class SenderTag(val label: String) {
    VIP_CREATOR("VIP Creator"),
    CUSTOMER("Customer"),
    COLLAB_INQUIRY("Collab Inquiry"),
    GENERAL("General")
}

data class InboxReply(
    val id: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isFromUs: Boolean
)

data class InboxItem(
    val id: String,
    val accountId: String,
    val senderName: String,
    val senderUsername: String,
    val senderAvatarRes: Int,
    val type: InboxType,
    val status: InboxStatus,
    val tag: SenderTag,
    val postContextTitle: String?,
    val postThumbnailRes: Int?,
    val messageText: String,
    val timestamp: Long,
    val replies: List<InboxReply> = emptyList()
)

enum class TeamRole(val label: String, val description: String) {
    OWNER("Owner", "Full access to all accounts, publishing, and team billing"),
    CONTENT_EDITOR("Content Editor", "Can compose, schedule, edit, and rearrange grid"),
    COMMUNITY_MANAGER("Community Manager", "Can reply to inbox DMs & comments, view calendar"),
    ANALYST("Analyst", "Read-only access to analytics and performance reports")
}

data class TeamMember(
    val id: String,
    val name: String,
    val email: String,
    val role: TeamRole,
    val initials: String,
    val colorHex: Long
)

data class MetaApiConfig(
    val connectedAccountHandle: String,
    val pageId: String,
    val tokenStatus: String,
    val expiresDaysRemaining: Int,
    val permissionsGranted: List<String>,
    val webhookActive: Boolean,
    val mediaUploadEndpoint: String
)
