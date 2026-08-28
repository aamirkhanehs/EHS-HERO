package com.ehshero.app.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.Badge
import com.ehshero.app.data.remote.GamificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class BadgesUiState(
    val isLoading: Boolean = true,
    val allBadges: List<Badge> = emptyList(),
    val unlockedIds: Set<String> = emptySet()
)

class BadgesViewModel(
    private val uid: String,
    private val gamificationRepository: GamificationRepository = GamificationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BadgesUiState())
    val uiState: StateFlow<BadgesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val badges = gamificationRepository.getBadges()
            gamificationRepository.observeUserBadgeIds(uid).collect { ids ->
                _uiState.value = BadgesUiState(isLoading = false, allBadges = badges, unlockedIds = ids)
            }
        }
    }
}
