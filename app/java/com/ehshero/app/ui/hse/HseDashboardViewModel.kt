package com.ehshero.app.ui.hse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.User
import com.ehshero.app.data.remote.ActivityRepository
import com.ehshero.app.data.remote.UserRepository
import com.ehshero.app.domain.LeaderboardEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HseDashboardUiState(
    val isLoading: Boolean = true,
    val totalEmployees: Int = 0,
    val activeEmployees: Int = 0,
    val totalXp: Int = 0,
    val pendingApprovals: Int = 0,
    val tbtCount: Int = 0,
    val observationCount: Int = 0,
    val goodPracticeCount: Int = 0,
    val nearMissCount: Int = 0,
    val trainingCount: Int = 0,
    val hazardCount: Int = 0,
    val topPerformer: User? = null,
    val topTen: List<LeaderboardEntry> = emptyList(),
    val activityDistribution: List<Pair<String, Int>> = emptyList(),
    val projectPerformance: List<Pair<String, Int>> = emptyList()
)

/**
 * Powers the EHS COMMAND CENTER (spec section 13). All KPIs here are
 * derived from the `users` collection's denormalized approved-activity
 * counters (see User.kt) rather than scanning the whole `activities`
 * collection - cheap enough to recompute live on every change, and exactly
 * matches spec section 27's "avoid unnecessary database reads".
 */
class HseDashboardViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val activityRepository: ActivityRepository = ActivityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HseDashboardUiState())
    val uiState: StateFlow<HseDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userRepository.observeAllUsers(),
                activityRepository.observePendingApprovals()
            ) { users, pending -> users to pending.size }
                .collect { (users, pendingCount) ->
                    val active = users.filter { it.isActive }
                    val topTen = active.sortedByDescending { it.totalXp }.take(10)
                        .mapIndexed { index, u ->
                            LeaderboardEntry(u.uid, u.name, u.designation, u.projectName, u.level, u.totalXp, u.avatarId, index + 1)
                        }
                    val distribution = listOf(
                        "TBT" to active.sumOf { it.approvedTbtCount },
                        "Observation" to active.sumOf { it.approvedObservationCount },
                        "Good Practice" to active.sumOf { it.approvedGoodPracticeCount },
                        "Near Miss" to active.sumOf { it.approvedNearMissCount },
                        "Training" to active.sumOf { it.approvedTrainingCount }
                    ).filter { it.second > 0 }
                    val projectPerformance = active
                        .groupBy { it.projectName.ifBlank { "Unassigned" } }
                        .mapValues { (_, list) -> list.sumOf { it.totalXp } }
                        .toList()
                        .sortedByDescending { it.second }
                        .take(6)

                    _uiState.value = HseDashboardUiState(
                        isLoading = false,
                        totalEmployees = users.size,
                        activeEmployees = active.size,
                        totalXp = users.sumOf { it.totalXp },
                        pendingApprovals = pendingCount,
                        tbtCount = active.sumOf { it.approvedTbtCount },
                        observationCount = active.sumOf { it.approvedObservationCount },
                        goodPracticeCount = active.sumOf { it.approvedGoodPracticeCount },
                        nearMissCount = active.sumOf { it.approvedNearMissCount },
                        trainingCount = active.sumOf { it.approvedTrainingCount },
                        hazardCount = active.sumOf { it.approvedHazardCount },
                        topPerformer = active.maxByOrNull { it.totalXp },
                        topTen = topTen,
                        activityDistribution = distribution,
                        projectPerformance = projectPerformance
                    )
                }
        }
    }
}
