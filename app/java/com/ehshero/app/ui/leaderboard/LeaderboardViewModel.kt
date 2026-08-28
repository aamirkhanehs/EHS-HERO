package com.ehshero.app.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.LeaderboardPeriod
import com.ehshero.app.data.model.Project
import com.ehshero.app.data.remote.LeaderboardRepository
import com.ehshero.app.data.remote.ProjectRepository
import com.ehshero.app.domain.LeaderboardEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val entries: List<LeaderboardEntry> = emptyList(),
    val period: LeaderboardPeriod = LeaderboardPeriod.ALL_TIME,
    val projectFilter: String? = null,
    val projects: List<Project> = emptyList()
)

class LeaderboardViewModel(
    private val currentUid: String,
    private val leaderboardRepository: LeaderboardRepository = LeaderboardRepository(),
    private val projectRepository: ProjectRepository = ProjectRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private var liveJob: Job? = null

    init {
        viewModelScope.launch {
            projectRepository.observeProjects().collect { projects ->
                _uiState.value = _uiState.value.copy(projects = projects)
            }
        }
        refresh()
    }

    fun currentUserId() = currentUid

    fun setPeriod(period: LeaderboardPeriod) {
        _uiState.value = _uiState.value.copy(period = period)
        refresh()
    }

    fun setProjectFilter(projectId: String?) {
        _uiState.value = _uiState.value.copy(projectFilter = projectId)
        refresh()
    }

    private fun refresh() {
        liveJob?.cancel()
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true)

        if (state.period == LeaderboardPeriod.ALL_TIME && state.projectFilter == null) {
            liveJob = viewModelScope.launch {
                leaderboardRepository.observeAllTimeLeaderboard().collect { entries ->
                    _uiState.value = _uiState.value.copy(isLoading = false, entries = entries)
                }
            }
        } else {
            viewModelScope.launch {
                val entries = leaderboardRepository.getFilteredLeaderboard(state.period, state.projectFilter)
                _uiState.value = _uiState.value.copy(isLoading = false, entries = entries)
            }
        }
    }
}
