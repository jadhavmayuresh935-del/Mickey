package com.example

import com.example.data.model.MediaType
import com.example.data.model.PostStatus
import com.example.data.repository.InstaFlowRepository
import com.example.ui.viewmodel.PostSchedulerViewModel
import com.example.ui.viewmodel.SchedulePreset
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testInitialAccountsAndSwitching() {
        val repo = InstaFlowRepository()
        val accounts = repo.accounts.value
        assertTrue(accounts.isNotEmpty())
        assertEquals("acc_1", repo.currentAccountId.value)

        repo.switchAccount("acc_2")
        assertEquals("acc_2", repo.currentAccountId.value)
    }

    @Test
    fun testAddAndSchedulePost() {
        val repo = InstaFlowRepository()
        val initialCount = repo.posts.value.size

        repo.addPost(
            caption = "Test scheduled post",
            mediaType = MediaType.CAROUSEL,
            mediaResIds = listOf(repo.presetMediaList[0], repo.presetMediaList[1]),
            scheduledTimestamp = System.currentTimeMillis() + 86400000,
            isDraft = false,
            hashtags = listOf("#test", "#studio")
        )

        val updatedPosts = repo.posts.value
        assertEquals(initialCount + 1, updatedPosts.size)
        val newPost = updatedPosts.first()
        assertEquals(PostStatus.SCHEDULED, newPost.status)
        assertEquals("Test scheduled post", newPost.caption)
    }

    @Test
    fun testSwapGridAesthetics() {
        val repo = InstaFlowRepository()
        val posts = repo.posts.value
        assertTrue(posts.size >= 2)
        val postA = posts[0]
        val postB = posts[1]
        val indexA = postA.gridIndex
        val indexB = postB.gridIndex

        repo.swapGridPositions(postA.id, postB.id)

        val updatedA = repo.posts.value.first { it.id == postA.id }
        val updatedB = repo.posts.value.first { it.id == postB.id }
        assertEquals(indexB, updatedA.gridIndex)
        assertEquals(indexA, updatedB.gridIndex)
    }

    @Test
    fun testSendInboxReply() {
        val repo = InstaFlowRepository()
        val inbox = repo.inboxItems.value
        assertTrue(inbox.isNotEmpty())
        val firstItem = inbox.first()

        repo.sendReply(firstItem.id, "Thanks for checking in! 🙌")

        val updatedItem = repo.inboxItems.value.first { it.id == firstItem.id }
        assertTrue(updatedItem.replies.isNotEmpty())
        assertEquals("Thanks for checking in! 🙌", updatedItem.replies.last().text)
    }

    @Test
    fun testPostSchedulerViewModelImageSelectionAndCaption() {
        val repo = InstaFlowRepository()
        val schedulerVm = PostSchedulerViewModel(repo)

        val initialState = schedulerVm.uiState.value
        assertTrue(initialState.isMediaValid)
        assertEquals(MediaType.SINGLE_IMAGE, initialState.selectedMediaType)

        // Switch to Carousel
        schedulerVm.setMediaType(MediaType.CAROUSEL)
        val carouselState = schedulerVm.uiState.value
        assertEquals(MediaType.CAROUSEL, carouselState.selectedMediaType)
        assertTrue(carouselState.selectedMediaResIds.size >= 2)

        // Toggle media selection
        val extraAsset = repo.presetMediaList.last()
        schedulerVm.toggleMediaSelection(extraAsset)
        assertTrue(schedulerVm.uiState.value.selectedMediaResIds.contains(extraAsset))

        // Update Caption & check character counter
        val caption = "Morning design session at the studio. Crafting clean interfaces."
        schedulerVm.updateCaption(caption)
        assertEquals(caption, schedulerVm.uiState.value.caption)
        assertEquals(caption.length, schedulerVm.uiState.value.characterCount)
        assertFalse(schedulerVm.uiState.value.isCharacterLimitExceeded)

        // Add hashtag
        schedulerVm.toggleHashtag("#designsystem")
        assertTrue(schedulerVm.uiState.value.hashtags.contains("#designsystem"))
        assertTrue(schedulerVm.uiState.value.caption.contains("#designsystem"))

        // Change schedule preset
        schedulerVm.setSchedulePreset(SchedulePreset.TOMORROW_EVENING)
        assertEquals(SchedulePreset.TOMORROW_EVENING, schedulerVm.uiState.value.schedulePreset)
        assertTrue(schedulerVm.uiState.value.scheduledTimestamp > System.currentTimeMillis())

        // Schedule Post
        var scheduleCallbackCalled = false
        val initialPostsCount = repo.posts.value.size
        schedulerVm.schedulePost {
            scheduleCallbackCalled = true
        }

        assertTrue(scheduleCallbackCalled)
        assertEquals(initialPostsCount + 1, repo.posts.value.size)
        val latestPost = repo.posts.value.first()
        assertEquals(PostStatus.SCHEDULED, latestPost.status)
        assertTrue(latestPost.caption.contains(caption))
    }
}
