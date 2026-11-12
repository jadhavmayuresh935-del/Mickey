package com.example.data.repository

import com.example.R
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.UUID

class InstaFlowRepository {

    // Preset sample images for quick post creation
    val presetMediaList = listOf(
        R.drawable.post_coffee_art_1794474590473,
        R.drawable.post_workspace_1794474602952,
        R.drawable.post_travel_1794474616792,
        R.drawable.post_food_art_1794474634651
    )

    // Accounts
    private val initialAccounts = listOf(
        InstagramAccount(
            id = "acc_1",
            username = "lumina.studio",
            displayName = "Lumina Creative Studio",
            bio = "Minimalist brand design, spatial architecture & visual direction. Curating modern spaces ✨",
            website = "https://lumina.studio",
            category = "Design & Creative Studio",
            followersCount = 142850,
            followingCount = 412,
            postsCount = 186,
            accountType = "Business",
            avatarResId = R.drawable.post_workspace_1794474602952,
            engagementRate = 4.62
        ),
        InstagramAccount(
            id = "acc_2",
            username = "urban.eats.co",
            displayName = "Urban Artisan Bakery",
            bio = "Wild sourdough, slow pastries & single-origin coffee roasted daily in downtown ☕🥐",
            website = "https://urbaneats.co",
            category = "Food & Beverage",
            followersCount = 86420,
            followingCount = 289,
            postsCount = 94,
            accountType = "Creator",
            avatarResId = R.drawable.post_food_art_1794474634651,
            engagementRate = 5.81
        ),
        InstagramAccount(
            id = "acc_3",
            username = "nomad_gear",
            displayName = "Nomad Expedition Gear",
            bio = "Ultralight field gear designed for coastal trails and alpine summits. Built to explore 🏔️",
            website = "https://nomadgear.io",
            category = "Outdoor & Gear",
            followersCount = 54200,
            followingCount = 195,
            postsCount = 72,
            accountType = "Business",
            avatarResId = R.drawable.post_travel_1794474616792,
            engagementRate = 3.94
        )
    )

    private val _accounts = MutableStateFlow(initialAccounts)
    val accounts: StateFlow<List<InstagramAccount>> = _accounts.asStateFlow()

    private val _currentAccountId = MutableStateFlow("acc_1")
    val currentAccountId: StateFlow<String> = _currentAccountId.asStateFlow()

    // Posts across accounts
    private val now = System.currentTimeMillis()
    private val oneHour = 3600 * 1000L
    private val oneDay = 24 * oneHour

    private val initialPosts = mutableListOf(
        // Scheduled Posts (top of grid / upcoming)
        Post(
            id = "p_sched_1",
            accountId = "acc_1",
            caption = "Tomorrow morning drop: New studio journal editions bound in natural linen. How do you plan your creative workflow? ☕✍️",
            mediaType = MediaType.CAROUSEL,
            mediaResIds = listOf(
                R.drawable.post_coffee_art_1794474590473,
                R.drawable.post_workspace_1794474602952
            ),
            scheduledTimestamp = now + (oneDay * 1) + (3 * oneHour),
            status = PostStatus.SCHEDULED,
            gridIndex = 0,
            hashtags = listOf("#workspacegoals", "#creativemorning", "#studiodesign", "#minimalistsetup")
        ),
        Post(
            id = "p_sched_2",
            accountId = "acc_1",
            caption = "Quiet moments in the studio. Sunlight catching the cedar desk before the morning design sprint kicks off.",
            mediaType = MediaType.SINGLE_IMAGE,
            mediaResIds = listOf(R.drawable.post_workspace_1794474602952),
            scheduledTimestamp = now + (oneDay * 2) + (6 * oneHour),
            status = PostStatus.SCHEDULED,
            gridIndex = 1,
            hashtags = listOf("#studiodesk", "#interiordesign", "#minimalmood", "#creativespace")
        ),
        Post(
            id = "p_draft_1",
            accountId = "acc_1",
            caption = "Behind the scenes on our Madeira field trip. Ocean winds, volcanic cliffs, and endless palette inspiration 🌊",
            mediaType = MediaType.REEL,
            mediaResIds = listOf(R.drawable.post_travel_1794474616792),
            scheduledTimestamp = now + (oneDay * 4),
            status = PostStatus.DRAFT,
            gridIndex = 2,
            hashtags = listOf("#travelreels", "#madeiraisland", "#visualinspiration", "#filmmaker")
        ),
        // Published Posts (already on profile)
        Post(
            id = "p_pub_1",
            accountId = "acc_1",
            caption = "Saturday rituals: warm cardamom bun and pour-over on marble. Our favorite neighborhood sanctuary.",
            mediaType = MediaType.SINGLE_IMAGE,
            mediaResIds = listOf(R.drawable.post_food_art_1794474634651),
            scheduledTimestamp = now - (oneDay * 1),
            status = PostStatus.PUBLISHED,
            gridIndex = 3,
            likesCount = 3840,
            commentsCount = 142,
            reachCount = 28400,
            savedCount = 612,
            hashtags = listOf("#weekendvibes", "#coffeeandpastry", "#slowliving", "#artisanbread")
        ),
        Post(
            id = "p_pub_2",
            accountId = "acc_1",
            caption = "Golden hour from Ponta de São Lourenço. Nature's contrast between warm golden light and deep cyan currents is unmatched.",
            mediaType = MediaType.CAROUSEL,
            mediaResIds = listOf(
                R.drawable.post_travel_1794474616792,
                R.drawable.post_workspace_1794474602952
            ),
            scheduledTimestamp = now - (oneDay * 3),
            status = PostStatus.PUBLISHED,
            gridIndex = 4,
            likesCount = 5910,
            commentsCount = 288,
            reachCount = 49200,
            savedCount = 1204,
            hashtags = listOf("#cliffside", "#cinematictones", "#visualsoflife", "#travelaesthetic")
        ),
        Post(
            id = "p_pub_3",
            accountId = "acc_1",
            caption = "Desk setup iteration 04: switched to a neutral wool felt desk pad and mechanical switches. Full breakdown on stories.",
            mediaType = MediaType.SINGLE_IMAGE,
            mediaResIds = listOf(R.drawable.post_workspace_1794474602952),
            scheduledTimestamp = now - (oneDay * 5),
            status = PostStatus.PUBLISHED,
            gridIndex = 5,
            likesCount = 4120,
            commentsCount = 167,
            reachCount = 34900,
            savedCount = 890,
            hashtags = listOf("#desktour", "#minimalworkspace", "#cleansetup", "#creativehardware")
        ),
        Post(
            id = "p_pub_4",
            accountId = "acc_1",
            caption = "Latte art practice & morning notebook journaling. 3 principles we stick to when scoping client identity systems.",
            mediaType = MediaType.CAROUSEL,
            mediaResIds = listOf(
                R.drawable.post_coffee_art_1794474590473,
                R.drawable.post_food_art_1794474634651
            ),
            scheduledTimestamp = now - (oneDay * 8),
            status = PostStatus.PUBLISHED,
            gridIndex = 6,
            likesCount = 2940,
            commentsCount = 98,
            reachCount = 21500,
            savedCount = 530,
            hashtags = listOf("#graphicdesign", "#brandidentity", "#typographylove", "#designstudio")
        ),
        Post(
            id = "p_pub_5",
            accountId = "acc_1",
            caption = "Fresh berries, flaky laminated crust, and quiet focus. Craft in every detail.",
            mediaType = MediaType.SINGLE_IMAGE,
            mediaResIds = listOf(R.drawable.post_food_art_1794474634651),
            scheduledTimestamp = now - (oneDay * 12),
            status = PostStatus.PUBLISHED,
            gridIndex = 7,
            likesCount = 3450,
            commentsCount = 114,
            reachCount = 26700,
            savedCount = 740,
            hashtags = listOf("#artisanfood", "#pastrychef", "#morningroutine", "#aestheticfood")
        ),
        Post(
            id = "p_pub_6",
            accountId = "acc_1",
            caption = "The ridge hike at sunrise. Packing the essential gear only. What trail is next on your bucket list?",
            mediaType = MediaType.REEL,
            mediaResIds = listOf(R.drawable.post_travel_1794474616792),
            scheduledTimestamp = now - (oneDay * 15),
            status = PostStatus.PUBLISHED,
            gridIndex = 8,
            likesCount = 7210,
            commentsCount = 340,
            reachCount = 68400,
            savedCount = 2100,
            hashtags = listOf("#trailrun", "#adventuregear", "#wanderlust", "#optoutside")
        )
    )

    private val _posts = MutableStateFlow<List<Post>>(initialPosts)
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Inbox Items
    private val initialInbox = listOf(
        InboxItem(
            id = "inbox_1",
            accountId = "acc_1",
            senderName = "Elena Vance",
            senderUsername = "elena.creative",
            senderAvatarRes = R.drawable.post_workspace_1794474602952,
            type = InboxType.DIRECT_MESSAGE,
            status = InboxStatus.UNREAD,
            tag = SenderTag.COLLAB_INQUIRY,
            postContextTitle = null,
            postThumbnailRes = null,
            messageText = "Hey Lumina team! Loving your new studio branding layout. Are you currently taking on new brand identity clients for Q1?",
            timestamp = now - (15 * 60 * 1000L),
            replies = emptyList()
        ),
        InboxItem(
            id = "inbox_2",
            accountId = "acc_1",
            senderName = "Marcus Brody",
            senderUsername = "marcus_arch",
            senderAvatarRes = R.drawable.post_travel_1794474616792,
            type = InboxType.COMMENT,
            status = InboxStatus.UNREAD,
            tag = SenderTag.VIP_CREATOR,
            postContextTitle = "Saturday rituals: warm cardamom bun...",
            postThumbnailRes = R.drawable.post_food_art_1794474634651,
            messageText = "Where is that marble table from? The texture and natural veining is extraordinary! 🙌",
            timestamp = now - (45 * 60 * 1000L),
            replies = emptyList()
        ),
        InboxItem(
            id = "inbox_3",
            accountId = "acc_1",
            senderName = "Sophia Lin",
            senderUsername = "sophia_crafts",
            senderAvatarRes = R.drawable.post_coffee_art_1794474590473,
            type = InboxType.DIRECT_MESSAGE,
            status = InboxStatus.RESOLVED,
            tag = SenderTag.CUSTOMER,
            postContextTitle = null,
            postThumbnailRes = null,
            messageText = "Can I order 3 copies of the linen creative journal to ship internationally to London?",
            timestamp = now - (3 * oneHour),
            replies = listOf(
                InboxReply(
                    id = "r_1",
                    senderName = "Lumina Studio",
                    text = "Hi Sophia! Yes, worldwide shipping is enabled via our bio link with DHL Express. ✈️",
                    timestamp = now - (2 * oneHour),
                    isFromUs = true
                )
            )
        ),
        InboxItem(
            id = "inbox_4",
            accountId = "acc_1",
            senderName = "Liam Gallagher",
            senderUsername = "liam_visuals",
            senderAvatarRes = R.drawable.post_workspace_1794474602952,
            type = InboxType.COMMENT,
            status = InboxStatus.UNREAD,
            tag = SenderTag.GENERAL,
            postContextTitle = "Golden hour from Ponta de São Lourenço...",
            postThumbnailRes = R.drawable.post_travel_1794474616792,
            messageText = "What color grade preset was used on this shot? The teal and orange separation is super clean.",
            timestamp = now - (6 * oneHour),
            replies = emptyList()
        )
    )

    private val _inboxItems = MutableStateFlow(initialInbox)
    val inboxItems: StateFlow<List<InboxItem>> = _inboxItems.asStateFlow()

    // Team Members
    private val initialTeam = listOf(
        TeamMember(
            id = "tm_1",
            name = "Alex Mercer",
            email = "alex@lumina.studio",
            role = TeamRole.OWNER,
            initials = "AM",
            colorHex = 0xFFC13584
        ),
        TeamMember(
            id = "tm_2",
            name = "Maya Chen",
            email = "maya@lumina.studio",
            role = TeamRole.CONTENT_EDITOR,
            initials = "MC",
            colorHex = 0xFFF56040
        ),
        TeamMember(
            id = "tm_3",
            name = "Jordan Blake",
            email = "jordan@lumina.studio",
            role = TeamRole.COMMUNITY_MANAGER,
            initials = "JB",
            colorHex = 0xFF833AB4
        ),
        TeamMember(
            id = "tm_4",
            name = "Sarah Miller",
            email = "sarah.m@lumina.studio",
            role = TeamRole.ANALYST,
            initials = "SM",
            colorHex = 0xFF2979FF
        )
    )

    private val _teamMembers = MutableStateFlow(initialTeam)
    val teamMembers: StateFlow<List<TeamMember>> = _teamMembers.asStateFlow()

    // Meta Graph API Configuration
    private val _metaApiConfig = MutableStateFlow(
        MetaApiConfig(
            connectedAccountHandle = "@lumina.studio",
            pageId = "109823471029384",
            tokenStatus = "Long-Lived Access Token (Valid)",
            expiresDaysRemaining = 58,
            permissionsGranted = listOf(
                "instagram_basic",
                "instagram_content_publish",
                "instagram_manage_comments",
                "instagram_manage_messages",
                "instagram_manage_insights",
                "pages_show_list",
                "pages_read_engagement"
            ),
            webhookActive = true,
            mediaUploadEndpoint = "https://graph.facebook.com/v21.0/{ig-user-id}/media"
        )
    )
    val metaApiConfig: StateFlow<MetaApiConfig> = _metaApiConfig.asStateFlow()

    // Quick Reply Templates
    val quickReplies = listOf(
        "Thanks so much for reaching out! 🙌 Let us know if you need anything else.",
        "Sent you a DM with the complete details & links! ✨",
        "We love this! Tag @lumina.studio to be featured on our weekly story 📸",
        "Our Q1 client roster is currently open! Please fill out the inquiry form in our bio.",
        "Thanks for the love! All preset details are linked in our bio highlights."
    )

    // AI Caption Tone Presets
    val toneOptions = listOf("Engaging & Hook", "Minimalist & Aesthetic", "Storytelling", "Promotional / CTA", "Professional")

    // Trending Hashtag Suggestions by Category
    val hashtagLibrary = mapOf(
        "Aesthetic & Studio" to listOf("#creativestudio", "#minimalistsetup", "#studiodesign", "#visualidentity", "#brandcuration", "#deskaesthetic"),
        "Travel & Landscape" to listOf("#visualsoflife", "#artofvisuals", "#earthfocus", "#cinematictravel", "#roamtheplanet", "#wildernessculture"),
        "Coffee & Food" to listOf("#artisanbakery", "#specialtycoffee", "#coffeeculture", "#slowliving", "#morningslikethese", "#latteart"),
        "Community & Engagement" to listOf("#contentcreator", "#igtips", "#instagramgrowth", "#creatorstudio", "#dailyinspiration")
    )

    // Switch active account
    fun switchAccount(accountId: String) {
        val found = _accounts.value.find { it.id == accountId } ?: return
        _currentAccountId.value = accountId
        _metaApiConfig.update {
            it.copy(
                connectedAccountHandle = "@${found.username}",
                pageId = "109" + found.username.hashCode().toLong().coerceAtLeast(10000000L)
            )
        }
    }

    // Add / Schedule Post
    fun addPost(
        caption: String,
        mediaType: MediaType,
        mediaResIds: List<Int>,
        scheduledTimestamp: Long,
        isDraft: Boolean,
        hashtags: List<String>
    ) {
        val accId = _currentAccountId.value
        val newStatus = if (isDraft) PostStatus.DRAFT else PostStatus.SCHEDULED
        val currentList = _posts.value

        val newPost = Post(
            id = "post_${UUID.randomUUID().toString().take(8)}",
            accountId = accId,
            caption = caption,
            mediaType = mediaType,
            mediaResIds = if (mediaResIds.isNotEmpty()) mediaResIds else listOf(presetMediaList.first()),
            scheduledTimestamp = scheduledTimestamp,
            status = newStatus,
            gridIndex = 0, // placed at top of grid
            hashtags = hashtags
        )

        // Increment gridIndex for all other posts of this account
        val updated = mutableListOf(newPost)
        currentList.forEach { p ->
            if (p.accountId == accId) {
                updated.add(p.copy(gridIndex = p.gridIndex + 1))
            } else {
                updated.add(p)
            }
        }
        _posts.value = updated
    }

    // Move / Swap posts in Visual Grid to curate aesthetics
    fun swapGridPositions(postIdA: String, postIdB: String) {
        val list = _posts.value.toMutableList()
        val indexA = list.indexOfFirst { it.id == postIdA }
        val indexB = list.indexOfFirst { it.id == postIdB }
        if (indexA != -1 && indexB != -1) {
            val gridA = list[indexA].gridIndex
            val gridB = list[indexB].gridIndex
            list[indexA] = list[indexA].copy(gridIndex = gridB)
            list[indexB] = list[indexB].copy(gridIndex = gridA)
            _posts.value = list
        }
    }

    // Delete post
    fun deletePost(postId: String) {
        _posts.update { list -> list.filterNot { it.id == postId } }
    }

    // Publish Post immediately
    fun publishNow(postId: String) {
        _posts.update { list ->
            list.map {
                if (it.id == postId) {
                    it.copy(
                        status = PostStatus.PUBLISHED,
                        scheduledTimestamp = System.currentTimeMillis(),
                        likesCount = 12,
                        reachCount = 140
                    )
                } else it
            }
        }
    }

    // Send reply to Inbox Item
    fun sendReply(inboxItemId: String, replyText: String) {
        val newReply = InboxReply(
            id = "r_${UUID.randomUUID().toString().take(6)}",
            senderName = "You",
            text = replyText,
            timestamp = System.currentTimeMillis(),
            isFromUs = true
        )
        _inboxItems.update { list ->
            list.map { item ->
                if (item.id == inboxItemId) {
                    item.copy(
                        replies = item.replies + newReply,
                        status = InboxStatus.RESOLVED
                    )
                } else item
            }
        }
    }

    // Toggle Inbox item status
    fun toggleInboxStatus(inboxItemId: String) {
        _inboxItems.update { list ->
            list.map { item ->
                if (item.id == inboxItemId) {
                    val nextStatus = when (item.status) {
                        InboxStatus.UNREAD -> InboxStatus.RESOLVED
                        InboxStatus.RESOLVED -> InboxStatus.UNREAD
                        InboxStatus.FLAGGED -> InboxStatus.RESOLVED
                    }
                    item.copy(status = nextStatus)
                } else item
            }
        }
    }

    // Add team member
    fun addTeamMember(name: String, email: String, role: TeamRole) {
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "TM" }

        val colors = listOf(0xFFC13584, 0xFFF56040, 0xFF833AB4, 0xFF2979FF, 0xFF00C853)
        val randomColor = colors.random()

        val member = TeamMember(
            id = "tm_${UUID.randomUUID().toString().take(6)}",
            name = name,
            email = email,
            role = role,
            initials = initials,
            colorHex = randomColor
        )
        _teamMembers.update { it + member }
    }

    // Refresh Meta Graph API token
    fun refreshMetaToken() {
        _metaApiConfig.update {
            it.copy(
                expiresDaysRemaining = 60,
                tokenStatus = "Long-Lived Access Token (Refreshed Just Now)"
            )
        }
    }

    // Connect new Instagram Account (simulated Meta OAuth)
    fun connectNewAccount(username: String, displayName: String, category: String) {
        val cleanUser = username.removePrefix("@").trim()
        val newAcc = InstagramAccount(
            id = "acc_${UUID.randomUUID().toString().take(6)}",
            username = cleanUser,
            displayName = displayName,
            bio = "Official Instagram page for $displayName. Connected via Meta Graph API.",
            website = "https://instagram.com/$cleanUser",
            category = category,
            followersCount = 1240,
            followingCount = 180,
            postsCount = 12,
            accountType = "Business",
            avatarResId = presetMediaList.random(),
            engagementRate = 4.20
        )
        _accounts.update { it + newAcc }
        switchAccount(newAcc.id)
    }

    // Generate AI Caption Rewrite
    fun generateCaption(topic: String, tone: String): String {
        return when (tone) {
            "Engaging & Hook" ->
                "Stop scrolling if you care about $topic! 👇\n\nHere is the one rule we live by: quality will always outperform noise. Tell us in the comments: what is your #1 focus this week?\n\nSave this post for later reference! 🔖"
            "Minimalist & Aesthetic" ->
                "Slow mornings & $topic.\n\nSimplicity is the ultimate sophistication.\nDetails tagged in story."
            "Storytelling" ->
                "Three months ago, we decided to rethink how we approach $topic.\n\nIt wasn't easy, and there were plenty of late studio nights. But seeing the final outcome come together reminded us why we started.\n\nSlide through to see the full transformation ➡️"
            "Promotional / CTA" ->
                "✨ EXCLUSIVE ANNOUNCEMENT ✨\n\nOur latest $topic collection is officially live. Limited spots available for this release.\n\n👉 Tap the link in our bio to claim yours before it sells out!"
            else ->
                "Curating excellence: exploring $topic with a focus on modern craftsmanship and timeless design. What are your thoughts on this direction?"
        }
    }
}
