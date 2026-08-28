package com.ehshero.app.ui.hse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.SafetyActivity
import com.ehshero.app.data.remote.ActivityRepository
import com.ehshero.app.data.remote.GamificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ApprovalsUiState(
    val isLoading: Boolean = true,
    val pending: List<SafetyActivity> = emptyList(),
    val actionInProgressId: String? = null,
    val message: String? = null
)

/** Backs the HSE Approvals queue and detail screen (spec section 14). */
class ApprovalsViewModel(
    private val reviewerUid: String,
    private val reviewerName: String,
    private val activityRepository: ActivityRepository = ActivityRepository(),
    private val gamificationRepository: GamificationRepository = GamificationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApprovalsUiState())
    val uiState: StateFlow<ApprovalsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            activityRepository.observePendingApprovals().collect { list ->
                _uiState.value = _uiState.value.copy(isLoading = false, pending = list)
            }
        }
    }

    fun approve(activityId: String) {
        _uiState.value = _uiState.value.copy(actionInProgressId = activityId)
        viewModelScope.launch {
            val result = gamificationRepository.approveActivity(activityId, reviewerUid, reviewerName)
            _uiState.value = _uiState.value.copy(
                actionInProgressId = null,
                message = result.fold(
                    onSuccess = { outcome ->
                        buildString {
                            append("Approved \u2013 +${outcome.activityXp} XP awarded")
                            if (outcome.streakBonusXp > 0) append(" (+${outcome.streakBonusXp} streak bonus)")
                            if (outcome.leveledUpTo != null) append(". They leveled up to ${outcome.leveledUpTo.title}!")
                            if (outcome.newlyUnlockedBadges.isNotEmpty()) {
                                append(" New badge: ${outcome.newlyUnlockedBadges.joinToString { it.name }}.")
                            }
                        }
                    },
                    onFailure = { it.message ?: "Could not approve this activity." }
                )
            )
        }
    }

    fun reject(activityId: String, reason: String) {
        _uiState.value = _uiState.value.copy(actionInProgressId = activityId)
        viewModelScope.launch {
            val result = gamificationRepository.rejectActivity(activityId, reviewerUid, reviewerName, reason)
            _uiState.value = _uiState.value.copy(
                actionInProgressId = null,
                message = result.fold(onSuccess = { "Activity rejected." }, onFailure = { it.message ?: "Could not reject this activity." })
            )
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
