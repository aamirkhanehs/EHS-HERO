package com.ehshero.app.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.Badge
import com.ehshero.app.data.model.LevelDef
import com.ehshero.app.data.model.Mission
import com.ehshero.app.data.model.User
import com.ehshero.app.data.model.UserMission
import com.ehshero.app.data.remote.GamificationRepository
import com.ehshero.app.data.remote.LeaderboardRepository
import com.ehshero.app.data.remote.MissionRepository
import com.ehshero.app.data.remote.UserRepository
import com.ehshero.app.domain.GamificationEngine
import com.ehshero.app.domain.LevelProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class StaffHomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val levelProgress: LevelProgress? = null,
    val allBadges: List<Badge> = emptyList(),
    val unlockedBadgeIds: Set<String> = emptySet(),
    val missions: List<Mission> = emptyList(),
    val userMissionsByMissionId: Map<String, UserMission> = emptyMap(),
    val myRank: Int? = null,
    val pendingLevelUp: LevelDef? = null,
    val badgeUnlockQueue: List<Badge> = emptyList()
)

/** A raw combination of the four live streams this screen needs, before any
 * transition-detection processing. Not shown to the UI directly. */
private data class Snapshot(
    val user: User?,
    val badgeIds: Set<String>,
    val missions: List<Mission>,
    val userMissions: List<UserMission>
)

class StaffHomeViewModel(
    private val uid: String,
    private val userRepository: UserRepository = UserRepository(),
    private val gamificationRepository: GamificationRepository = GamificationRepository(),
    private val missionRepository: MissionRepository = MissionRepository(),
    private val leaderboardRepository: LeaderboardRepository = LeaderboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffHomeUiState())
    val uiState: StateFlow<StaffHomeUiState> = _uiState.asStateFlow()

    private var levels: List<LevelDef> = emptyList()
    private var previousLevelNumber: Int? = null
    private var previousBadgeIds: Set<String>? = null
    private var lastRankedXp: Int? = null

    init {
        viewModelScope.launch {
            levels = gamificationRepository.getLevels()
            val badges = gamificationRepository.getBadges()
            _uiState.value = _uiState.value.copy(allBadges = badges)
        }
        observeEverything()
    }

    private fun observeEverything() {
        viewModelScope.launch {
            combine(
                userRepository.observeUser(uid),
                gamificationRepository.observeUserBadgeIds(uid),
                missionRepository.observeActiveMissions(),
                missionRepository.observeUserMissions(uid)
            ) { user, badgeIds, missions, userMissions ->
                Snapshot(user, badgeIds, missions, userMissions)
            }.collect { snapshot ->
                val user = snapshot.user ?: return@collect

                // Level-up detection: only fires once we've actually seen a
                // previous value (so opening the app doesn't show a level-up
                // for XP earned while it was closed - that's what the
                // notification center is for) and the level increased.
                val leveledUpTo: LevelDef? = previousLevelNumber?.let { prev ->
                    if (user.level > prev) levels.firstOrNull { it.levelNumber == user.level } else null
                }
                previousLevelNumber = user.level

                val newlyUnlocked: List<Badge> = previousBadgeIds?.let { prev ->
                    val newIds = snapshot.badgeIds - prev
                    if (newIds.isEmpty()) emptyList() else _uiState.value.allBadges.filter { it.badgeId in newIds }
                } ?: emptyList()
                previousBadgeIds = snapshot.badgeIds

                val progress = GamificationEngine.progress(user.totalXp, levels)
                val missionsMap = snapshot.userMissions.associateBy { it.missionId }

                val current = _uiState.value
                _uiState.value = current.copy(
                    isLoading = false,
                    user = user,
                    levelProgress = progress,
                    unlockedBadgeIds = snapshot.badgeIds,
                    missions = snapshot.missions,
                    userMissionsByMissionId = missionsMap,
                    pendingLevelUp = leveledUpTo ?: current.pendingLevelUp,
                    badgeUnlockQueue = current.badgeUnlockQueue + newlyUnlocked
                )

                if (lastRankedXp != user.totalXp) {
                    lastRankedXp = user.totalXp
                    viewModelScope.launch {
                        val rank = runCatching { leaderboardRepository.getMyRank(user.totalXp) }.getOrNull()
                        _uiState.value = _uiState.value.copy(myRank = rank)
                    }
                }
            }
        }
    }

    fun dismissLevelUp() {
        _uiState.value = _uiState.value.copy(pendingLevelUp = null)
    }

    fun dismissNextBadgeUnlock() {
        val current = _uiState.value
        if (current.badgeUnlockQueue.isEmpty()) return
        _uiState.value = current.copy(badgeUnlockQueue = current.badgeUnlockQueue.drop(1))
    }

    fun startMission(missionId: String) {
        viewModelScope.launch { missionRepository.startMission(uid, missionId) }
    }
}
