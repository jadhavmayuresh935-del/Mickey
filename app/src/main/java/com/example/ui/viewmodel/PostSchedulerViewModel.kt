package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.MediaType
import com.example.data.model.PostStatus
import com.example.data.repository.InstaFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SchedulePreset(val label: String, val hourOfDay: Int, val minute: Int, val isOptimal: Boolean = false) {
    TOMORROW_MORNING("Tomorrow 9:00 AM", 9, 0),
    TOMORROW_EVENING("Tomorrow 6:00 PM", 18, 0),
    OPTIMAL_AUDIENCE_PEAK("Optimal: Wed 6:00 PM", 18, 0, isOptimal = true),
    CUSTOM("Custom Date & Time", -1, -1)
}

data class PostSchedulerUiState(
    val selectedMediaType: MediaType = MediaType.SINGLE_IMAGE,
    val selectedMediaResIds: List<Int> = emptyList(),
    val caption: String = "",
    val maxCharacters: Int = 2200,
    val hashtags: List<String> = emptyList(),
    val maxHashtags: Int = 30,
    val schedulePreset: SchedulePreset = SchedulePreset.TOMORROW_MORNING,
    val scheduledTimestamp: Long = 0L,
    val formattedScheduleDate: String = "",
    val isAiAssistantExpanded: Boolean = false,
    val aiTopic: String = "",
    val selectedAiTone: String = "Engaging & Hook",
    val isGeneratingAi: Boolean = false,
    val availableToneOptions: List<String> = emptyList(),
    val availablePresetMedia: List<Int> = emptyList(),
    val suggestedHashtags: List<String> = emptyList(),
    val showCustomDatePicker: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false
) {
    val characterCount: Int get() = caption.length
    val remainingCharacters: Int get() = maxCharacters - characterCount
    val isCharacterLimitExceeded: Boolean get() = characterCount > maxCharacters
    val isHashtagLimitExceeded: Boolean get() = hashtags.size > maxHashtags
    val isMediaValid: Boolean get() = selectedMediaResIds.isNotEmpty()
    val canSchedule: Boolean get() = isMediaValid && !isCharacterLimitExceeded && !isHashtagLimitExceeded && !isSubmitting
}

class PostSchedulerViewModel(
    private val repository: InstaFlowRepository = InstaFlowRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PostSchedulerUiState(
            availableToneOptions = repository.toneOptions,
            availablePresetMedia = repository.presetMediaList,
            suggestedHashtags = listOf(
                "#contentcreator", "#visualsoflife", "#creativestudio",
                "#minimalsetup", "#travelaesthetic", "#artisan", "#slowliving",
                "#deskaesthetic", "#cinematictravel", "#brandcuration"
            )
        )
    )
    val uiState: StateFlow<PostSchedulerUiState> = _uiState.asStateFlow()

    init {
        // Initialize default media and schedule time
        val defaultMedia = repository.presetMediaList.firstOrNull()?.let { listOf(it) } ?: emptyList()
        val defaultScheduleTime = calculateTimestampForPreset(SchedulePreset.TOMORROW_MORNING)
        _uiState.update {
            it.copy(
                selectedMediaResIds = defaultMedia,
                scheduledTimestamp = defaultScheduleTime,
                formattedScheduleDate = formatTimestamp(defaultScheduleTime)
            )
        }
    }

    // Media Type Selection
    fun setMediaType(type: MediaType) {
        _uiState.update { current ->
            val mediaList = current.selectedMediaResIds.toMutableList()
            val available = current.availablePresetMedia

            val adjustedMedia = when (type) {
                MediaType.SINGLE_IMAGE, MediaType.REEL -> {
                    if (mediaList.isNotEmpty()) listOf(mediaList.first())
                    else if (available.isNotEmpty()) listOf(available.first())
                    else emptyList()
                }
                MediaType.CAROUSEL -> {
                    if (mediaList.size < 2 && available.size >= 2) {
                        listOf(available[0], available[1])
                    } else if (mediaList.isNotEmpty()) {
                        mediaList
                    } else {
                        available.take(2)
                    }
                }
            }

            current.copy(
                selectedMediaType = type,
                selectedMediaResIds = adjustedMedia,
                errorMessage = null
            )
        }
    }

    // Image/Media Selection
    fun toggleMediaSelection(resId: Int) {
        _uiState.update { current ->
            val list = current.selectedMediaResIds.toMutableList()
            val isCarousel = current.selectedMediaType == MediaType.CAROUSEL

            val updatedList = if (isCarousel) {
                if (list.contains(resId)) {
                    // Keep at least 1 image
                    if (list.size > 1) list.filter { it != resId } else list
                } else {
                    list + resId
                }
            } else {
                // Single image or reel: single selection
                listOf(resId)
            }

            current.copy(
                selectedMediaResIds = updatedList,
                errorMessage = null
            )
        }
    }

    fun removeMedia(resId: Int) {
        _uiState.update { current ->
            if (current.selectedMediaResIds.size > 1) {
                current.copy(selectedMediaResIds = current.selectedMediaResIds.filter { it != resId })
            } else {
                current
            }
        }
    }

    // Caption Management
    fun updateCaption(newCaption: String) {
        _uiState.update { current ->
            current.copy(
                caption = newCaption,
                errorMessage = if (newCaption.length > current.maxCharacters) "Caption exceeds 2,200 character limit" else null
            )
        }
    }

    // Hashtag Management
    fun toggleHashtag(tag: String) {
        _uiState.update { current ->
            val normalizedTag = if (tag.startsWith("#")) tag else "#$tag"
            val currentTags = current.hashtags
            val updatedTags = if (currentTags.contains(normalizedTag)) {
                currentTags - normalizedTag
            } else {
                if (currentTags.size < current.maxHashtags) currentTags + normalizedTag
                else currentTags
            }

            // Sync with caption text if newly added
            var newCaption = current.caption
            if (!currentTags.contains(normalizedTag) && !newCaption.contains(normalizedTag)) {
                newCaption = if (newCaption.isBlank()) normalizedTag else "$newCaption $normalizedTag".trim()
            }

            current.copy(
                hashtags = updatedTags,
                caption = newCaption
            )
        }
    }

    fun removeHashtag(tag: String) {
        _uiState.update { current ->
            current.copy(hashtags = current.hashtags - tag)
        }
    }

    // Scheduling Time Management
    fun setSchedulePreset(preset: SchedulePreset) {
        if (preset == SchedulePreset.CUSTOM) {
            _uiState.update { it.copy(schedulePreset = preset, showCustomDatePicker = true) }
        } else {
            val ts = calculateTimestampForPreset(preset)
            _uiState.update {
                it.copy(
                    schedulePreset = preset,
                    scheduledTimestamp = ts,
                    formattedScheduleDate = formatTimestamp(ts),
                    showCustomDatePicker = false
                )
            }
        }
    }

    fun setCustomScheduleTimestamp(timestampMillis: Long) {
        _uiState.update {
            it.copy(
                schedulePreset = SchedulePreset.CUSTOM,
                scheduledTimestamp = timestampMillis,
                formattedScheduleDate = formatTimestamp(timestampMillis),
                showCustomDatePicker = false
            )
        }
    }

    fun dismissCustomDatePicker() {
        _uiState.update { it.copy(showCustomDatePicker = false) }
    }

    // AI Caption Assistant
    fun toggleAiAssistant(expanded: Boolean? = null) {
        _uiState.update {
            it.copy(isAiAssistantExpanded = expanded ?: !it.isAiAssistantExpanded)
        }
    }

    fun setAiTopic(topic: String) {
        _uiState.update { it.copy(aiTopic = topic) }
    }

    fun setAiTone(tone: String) {
        _uiState.update { it.copy(selectedAiTone = tone) }
    }

    fun generateAiCaption() {
        val topic = _uiState.value.aiTopic.ifBlank { "creative studio updates & rituals" }
        val tone = _uiState.value.selectedAiTone

        _uiState.update { it.copy(isGeneratingAi = true) }

        val generated = repository.generateCaption(topic = topic, tone = tone)

        _uiState.update {
            it.copy(
                caption = generated,
                isGeneratingAi = false
            )
        }
    }

    // Submission Actions
    fun schedulePost(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.canSchedule) {
            _uiState.update { it.copy(errorMessage = "Please select at least one media asset") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true) }

        repository.addPost(
            caption = state.caption.ifBlank { "Curated update via InstaFlow Studio." },
            mediaType = state.selectedMediaType,
            mediaResIds = state.selectedMediaResIds,
            scheduledTimestamp = state.scheduledTimestamp,
            isDraft = false,
            hashtags = state.hashtags
        )

        _uiState.update { it.copy(isSubmitting = false) }
        onSuccess()
    }

    fun saveDraft(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isSubmitting = true) }

        repository.addPost(
            caption = state.caption.ifBlank { "Draft update" },
            mediaType = state.selectedMediaType,
            mediaResIds = state.selectedMediaResIds.ifEmpty { listOf(repository.presetMediaList.first()) },
            scheduledTimestamp = System.currentTimeMillis() + (24 * 3600 * 1000L),
            isDraft = true,
            hashtags = state.hashtags
        )

        _uiState.update { it.copy(isSubmitting = false) }
        onSuccess()
    }

    fun publishImmediately(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isSubmitting = true) }

        repository.addPost(
            caption = state.caption.ifBlank { "Published via InstaFlow Studio" },
            mediaType = state.selectedMediaType,
            mediaResIds = state.selectedMediaResIds.ifEmpty { listOf(repository.presetMediaList.first()) },
            scheduledTimestamp = System.currentTimeMillis(),
            isDraft = false,
            hashtags = state.hashtags
        )

        _uiState.update { it.copy(isSubmitting = false) }
        onSuccess()
    }

    fun resetForm() {
        val defaultMedia = repository.presetMediaList.firstOrNull()?.let { listOf(it) } ?: emptyList()
        val defaultScheduleTime = calculateTimestampForPreset(SchedulePreset.TOMORROW_MORNING)
        _uiState.value = PostSchedulerUiState(
            availableToneOptions = repository.toneOptions,
            availablePresetMedia = repository.presetMediaList,
            selectedMediaResIds = defaultMedia,
            scheduledTimestamp = defaultScheduleTime,
            formattedScheduleDate = formatTimestamp(defaultScheduleTime)
        )
    }

    private fun calculateTimestampForPreset(preset: SchedulePreset): Long {
        val cal = Calendar.getInstance()
        when (preset) {
            SchedulePreset.TOMORROW_MORNING -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
            }
            SchedulePreset.TOMORROW_EVENING -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 18)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
            }
            SchedulePreset.OPTIMAL_AUDIENCE_PEAK -> {
                // Find next Wednesday at 6 PM
                var daysUntilWed = (Calendar.WEDNESDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7
                if (daysUntilWed == 0 && cal.get(Calendar.HOUR_OF_DAY) >= 18) {
                    daysUntilWed = 7
                }
                cal.add(Calendar.DAY_OF_YEAR, daysUntilWed)
                cal.set(Calendar.HOUR_OF_DAY, 18)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
            }
            SchedulePreset.CUSTOM -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }

    private fun formatTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    class Factory(private val repository: InstaFlowRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostSchedulerViewModel::class.java)) {
                return PostSchedulerViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
