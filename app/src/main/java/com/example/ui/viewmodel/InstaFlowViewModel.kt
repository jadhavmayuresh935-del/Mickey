package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.InstaFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InstaFlowViewModel(
    val repository: InstaFlowRepository = InstaFlowRepository()
) : ViewModel() {

    val accounts = repository.accounts
    val currentAccountId = repository.currentAccountId
    val posts = repository.posts
    val inboxItems = repository.inboxItems
    val teamMembers = repository.teamMembers
    val metaApiConfig = repository.metaApiConfig

    val currentAccount: StateFlow<InstagramAccount?> = combine(
        accounts,
        currentAccountId
    ) { accList, activeId ->
        accList.find { it.id == activeId } ?: accList.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Current navigation tab: 0=Dashboard, 1=Calendar, 2=Grid, 3=Inbox, 4=Settings
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Analytics Period: "7D", "30D", "90D"
    private val _analyticsPeriod = MutableStateFlow("30D")
    val analyticsPeriod: StateFlow<String> = _analyticsPeriod.asStateFlow()

    // Calendar view state
    private val _calendarMode = MutableStateFlow("Weekly") // "Weekly" or "Monthly"
    val calendarMode: StateFlow<String> = _calendarMode.asStateFlow()

    private val _selectedCalendarDay = MutableStateFlow(System.currentTimeMillis())
    val selectedCalendarDay: StateFlow<Long> = _selectedCalendarDay.asStateFlow()

    // Visual Grid filter: "All", "Scheduled", "Live"
    private val _gridFilter = MutableStateFlow("All")
    val gridFilter: StateFlow<String> = _gridFilter.asStateFlow()

    // Selected post to inspect
    private val _inspectedPost = MutableStateFlow<Post?>(null)
    val inspectedPost: StateFlow<Post?> = _inspectedPost.asStateFlow()

    // Grid rearrangement mode
    private val _isGridRearrangeMode = MutableStateFlow(false)
    val isGridRearrangeMode: StateFlow<Boolean> = _isGridRearrangeMode.asStateFlow()

    // Selected post in rearrange mode
    private val _selectedRearrangePostId = MutableStateFlow<String?>(null)
    val selectedRearrangePostId: StateFlow<String?> = _selectedRearrangePostId.asStateFlow()

    // Inbox Filters
    private val _inboxStatusFilter = MutableStateFlow<InboxStatus?>(null) // null = all
    val inboxStatusFilter: StateFlow<InboxStatus?> = _inboxStatusFilter.asStateFlow()

    private val _inboxTypeFilter = MutableStateFlow<InboxType?>(null) // null = all
    val inboxTypeFilter: StateFlow<InboxType?> = _inboxTypeFilter.asStateFlow()

    private val _selectedInboxItem = MutableStateFlow<InboxItem?>(null)
    val selectedInboxItem: StateFlow<InboxItem?> = _selectedInboxItem.asStateFlow()

    // Dialog & Sheet States
    private val _showCreatePostSheet = MutableStateFlow(false)
    val showCreatePostSheet: StateFlow<Boolean> = _showCreatePostSheet.asStateFlow()

    private val _showConnectAccountDialog = MutableStateFlow(false)
    val showConnectAccountDialog: StateFlow<Boolean> = _showConnectAccountDialog.asStateFlow()

    private val _showInviteTeamDialog = MutableStateFlow(false)
    val showInviteTeamDialog: StateFlow<Boolean> = _showInviteTeamDialog.asStateFlow()

    // Dynamic Analytics derived from active account and posts
    val analyticsData: StateFlow<AnalyticsData> = combine(
        currentAccount,
        posts,
        analyticsPeriod
    ) { account, allPosts, period ->
        val acc = account ?: return@combine AnalyticsData(
            totalFollowers = 0, followerGrowthRate = 0.0,
            engagementRate = 0.0, totalReach = 0, reachGrowthRate = 0.0,
            scheduledPostsCount = 0, impressions = 0, followerTrend = emptyList(),
            dailyReach = emptyList(), bestTimeToPost = emptyList(), topPostTypes = emptyList()
        )
        val accPosts = allPosts.filter { it.accountId == acc.id }
        val scheduledCount = accPosts.count { it.status == PostStatus.SCHEDULED }

        val multiplier = when (period) {
            "7D" -> 0.25f
            "90D" -> 2.8f
            else -> 1.0f
        }

        val totalReach = (acc.followersCount * 5.8 * multiplier).toInt()
        val impressions = (totalReach * 1.6).toInt()

        val trend = listOf(
            "Mon" to (acc.followersCount - (480 * multiplier).toInt()),
            "Tue" to (acc.followersCount - (350 * multiplier).toInt()),
            "Wed" to (acc.followersCount - (190 * multiplier).toInt()),
            "Thu" to (acc.followersCount - (80 * multiplier).toInt()),
            "Fri" to (acc.followersCount + (110 * multiplier).toInt()),
            "Sat" to (acc.followersCount + (280 * multiplier).toInt()),
            "Sun" to acc.followersCount
        )

        val reachBars = listOf(
            "Mon" to (34000 * multiplier).toInt(),
            "Tue" to (42000 * multiplier).toInt(),
            "Wed" to (58000 * multiplier).toInt(),
            "Thu" to (49000 * multiplier).toInt(),
            "Fri" to (67000 * multiplier).toInt(),
            "Sat" to (82000 * multiplier).toInt(),
            "Sun" to (74000 * multiplier).toInt()
        )

        AnalyticsData(
            totalFollowers = acc.followersCount,
            followerGrowthRate = if (period == "7D") +1.2 else if (period == "90D") +14.8 else +4.8,
            engagementRate = acc.engagementRate,
            totalReach = totalReach,
            reachGrowthRate = +12.4,
            scheduledPostsCount = scheduledCount,
            impressions = impressions,
            followerTrend = trend,
            dailyReach = reachBars,
            bestTimeToPost = listOf("Wed 6:00 PM", "Fri 12:30 PM", "Sun 8:15 PM"),
            topPostTypes = listOf("Reels" to 0.48f, "Carousels" to 0.34f, "Single Image" to 0.18f)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AnalyticsData(
            totalFollowers = 142850,
            followerGrowthRate = 4.8,
            engagementRate = 4.62,
            totalReach = 828500,
            reachGrowthRate = 12.4,
            scheduledPostsCount = 2,
            impressions = 1325600,
            followerTrend = emptyList(),
            dailyReach = emptyList(),
            bestTimeToPost = listOf("Wed 6:00 PM", "Fri 12:30 PM", "Sun 8:15 PM"),
            topPostTypes = listOf("Reels" to 0.48f, "Carousels" to 0.34f, "Single Image" to 0.18f)
        )
    )

    // Navigation and tab switching
    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setAnalyticsPeriod(period: String) {
        _analyticsPeriod.value = period
    }

    fun setCalendarMode(mode: String) {
        _calendarMode.value = mode
    }

    fun setSelectedCalendarDay(timestamp: Long) {
        _selectedCalendarDay.value = timestamp
    }

    fun setGridFilter(filter: String) {
        _gridFilter.value = filter
    }

    fun inspectPost(post: Post?) {
        _inspectedPost.value = post
    }

    fun toggleGridRearrangeMode() {
        val next = !_isGridRearrangeMode.value
        _isGridRearrangeMode.value = next
        if (!next) {
            _selectedRearrangePostId.value = null
        }
    }

    fun onGridPostClicked(post: Post) {
        if (_isGridRearrangeMode.value) {
            val selected = _selectedRearrangePostId.value
            if (selected == null) {
                _selectedRearrangePostId.value = post.id
            } else if (selected == post.id) {
                _selectedRearrangePostId.value = null
            } else {
                repository.swapGridPositions(selected, post.id)
                _selectedRearrangePostId.value = null
            }
        } else {
            inspectPost(post)
        }
    }

    fun switchAccount(accountId: String) {
        repository.switchAccount(accountId)
        _inspectedPost.value = null
        _selectedInboxItem.value = null
    }

    fun openCreatePostSheet() {
        _showCreatePostSheet.value = true
    }

    fun closeCreatePostSheet() {
        _showCreatePostSheet.value = false
    }

    fun openConnectAccountDialog() {
        _showConnectAccountDialog.value = true
    }

    fun closeConnectAccountDialog() {
        _showConnectAccountDialog.value = false
    }

    fun openInviteTeamDialog() {
        _showInviteTeamDialog.value = true
    }

    fun closeInviteTeamDialog() {
        _showInviteTeamDialog.value = false
    }

    fun setInboxStatusFilter(status: InboxStatus?) {
        _inboxStatusFilter.value = status
    }

    fun setInboxTypeFilter(type: InboxType?) {
        _inboxTypeFilter.value = type
    }

    fun selectInboxItem(item: InboxItem?) {
        _selectedInboxItem.value = item
    }

    fun createPost(
        caption: String,
        mediaType: MediaType,
        mediaResIds: List<Int>,
        scheduledTimestamp: Long,
        isDraft: Boolean,
        hashtags: List<String>
    ) {
        repository.addPost(
            caption = caption,
            mediaType = mediaType,
            mediaResIds = mediaResIds,
            scheduledTimestamp = scheduledTimestamp,
            isDraft = isDraft,
            hashtags = hashtags
        )
        _showCreatePostSheet.value = false
    }

    fun publishNow(postId: String) {
        repository.publishNow(postId)
        _inspectedPost.value = _inspectedPost.value?.copy(status = PostStatus.PUBLISHED)
    }

    fun deletePost(postId: String) {
        repository.deletePost(postId)
        if (_inspectedPost.value?.id == postId) {
            _inspectedPost.value = null
        }
    }

    fun sendInboxReply(itemId: String, text: String) {
        repository.sendReply(itemId, text)
        // update selected item in detail view
        val updated = repository.inboxItems.value.find { it.id == itemId }
        _selectedInboxItem.value = updated
    }

    fun toggleInboxStatus(itemId: String) {
        repository.toggleInboxStatus(itemId)
        val updated = repository.inboxItems.value.find { it.id == itemId }
        _selectedInboxItem.value = updated
    }

    fun addTeamMember(name: String, email: String, role: TeamRole) {
        repository.addTeamMember(name, email, role)
        _showInviteTeamDialog.value = false
    }

    fun connectNewAccount(username: String, displayName: String, category: String) {
        repository.connectNewAccount(username, displayName, category)
        _showConnectAccountDialog.value = false
    }

    fun refreshMetaToken() {
        repository.refreshMetaToken()
    }

    fun generateAiCaption(topic: String, tone: String): String {
        return repository.generateCaption(topic, tone)
    }
}
