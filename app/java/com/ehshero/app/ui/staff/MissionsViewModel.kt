package com.ehshero.app.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.Mission
import com.ehshero.app.data.model.UserMission
import com.ehshero.app.data.remote.MissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MissionsUiState(
    val isLoading: Boolean = true,
    val missions: List<Mission> = emptyList(),
    val progressByMissionId: Map<String, UserMission> = emptyMap()
)

class MissionsViewModel(
    private val uid: String,
    private val missionRepository: MissionRepository = MissionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionsUiState())
    val uiState: StateFlow<MissionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                missionRepository.observeActiveMissions(),
                missionRepository.observeUserMissions(uid)
            ) { missions, userMissions -> missions to userMissions }
                .collect { (missions, userMissions) ->
                    _uiState.value = MissionsUiState(
                        isLoading = false,
                        missions = missions,
                        progressByMissionId = userMissions.associateBy { it.missionId }
                    )
                }
        }
    }

    fun startMission(missionId: String) {
        viewModelScope.launch { missionRepository.startMission(uid, missionId) }
    }
}
