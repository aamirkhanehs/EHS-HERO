package com.ehshero.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.Badge
import com.ehshero.app.data.model.LevelDef
import com.ehshero.app.data.model.User
import com.ehshero.app.data.remote.AuthRepository
import com.ehshero.app.data.remote.GamificationRepository
import com.ehshero.app.data.remote.UserRepository
import com.ehshero.app.domain.GamificationEngine
import com.ehshero.app.domain.LevelProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val levelProgress: LevelProgress? = null,
    val unlockedBadges: List<Badge> = emptyList(),
    val totalBadges: Int = 0
)

class ProfileViewModel(
    private val uid: String,
    private val userRepository: UserRepository = UserRepository(),
    private val gamificationRepository: GamificationRepository = GamificationRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var levels: List<LevelDef> = emptyList()
    private var allBadges: List<Badge> = emptyList()

    init {
        viewModelScope.launch {
            levels = gamificationRepository.getLevels()
            allBadges = gamificationRepository.getBadges()
            combine(
                userRepository.observeUser(uid),
                gamificationRepository.observeUserBadgeIds(uid)
            ) { user, badgeIds -> user to badgeIds }
                .collect { (user, badgeIds) ->
                    if (user == null) return@collect
                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        user = user,
                        levelProgress = GamificationEngine.progress(user.totalXp, levels),
                        unlockedBadges = allBadges.filter { it.badgeId in badgeIds },
                        totalBadges = allBadges.size
                    )
                }
        }
    }

    fun setAvatar(avatarId: String) {
        viewModelScope.launch { userRepository.updateAvatar(uid, avatarId) }
    }

    fun logout() {
        authRepository.logout()
    }
}
