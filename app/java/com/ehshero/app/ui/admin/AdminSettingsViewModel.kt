package com.ehshero.app.ui.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.ActivityType
import com.ehshero.app.data.model.Mission
import com.ehshero.app.data.model.Project
import com.ehshero.app.data.remote.FirebaseModule
import com.ehshero.app.data.remote.FirestoreCollections
import com.ehshero.app.data.remote.GamificationRepository
import com.ehshero.app.data.remote.MissionRepository
import com.ehshero.app.data.remote.ProjectRepository
import com.ehshero.app.data.seed.DemoDataSeeder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminSettingsUiState(
    val isLoading: Boolean = true,
    val pointRules: Map<ActivityType, Int> = emptyMap(),
    val projects: List<Project> = emptyList(),
    val isBusy: Boolean = false,
    val message: String? = null
)

class AdminSettingsViewModel(
    private val gamificationRepository: GamificationRepository = GamificationRepository(),
    private val projectRepository: ProjectRepository = ProjectRepository(),
    private val missionRepository: MissionRepository = MissionRepository(),
    private val demoDataSeeder: DemoDataSeeder = DemoDataSeeder()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSettingsUiState())
    val uiState: StateFlow<AdminSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val rules = gamificationRepository.getPointRules()
            val mapped = ActivityType.entries.associateWith { rules[it.name] ?: it.defaultXp }
            _uiState.value = _uiState.value.copy(isLoading = false, pointRules = mapped)
        }
        viewModelScope.launch {
            projectRepository.observeProjects().collect { projects ->
                _uiState.value = _uiState.value.copy(projects = projects)
            }
        }
    }

    fun updatePointRuleLocally(type: ActivityType, xp: Int) {
        _uiState.value = _uiState.value.copy(
            pointRules = _uiState.value.pointRules.toMutableMap().apply { put(type, xp) }
        )
    }

    fun savePointRules() {
        _uiState.value = _uiState.value.copy(isBusy = true)
        viewModelScope.launch {
            val asLongMap = _uiState.value.pointRules.mapKeys { it.key.name }.mapValues { it.value.toLong() }
            FirebaseModule.firestore.collection(FirestoreCollections.SETTINGS).document("pointRules")
                .set(mapOf("xpByActivityType" to asLongMap))
                .await()
            _uiState.value = _uiState.value.copy(isBusy = false, message = "Point rules saved.")
        }
    }

    fun createProject(name: String, location: String, type: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            projectRepository.createOrUpdateProject(Project(name = name, location = location, type = type))
            _uiState.value = _uiState.value.copy(message = "Project \"$name\" created.")
        }
    }

    fun createMission(title: String, description: String, activityType: ActivityType, xpReward: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            missionRepository.createMission(
                Mission(
                    title = title,
                    description = description,
                    activityType = activityType.name,
                    xpReward = xpReward,
                    active = true
                )
            )
            _uiState.value = _uiState.value.copy(message = "Mission \"$title\" created.")
        }
    }

    fun seedDemoData(context: Context, includeDemoUsers: Boolean) {
        _uiState.value = _uiState.value.copy(isBusy = true)
        viewModelScope.launch {
            val result = if (includeDemoUsers) {
                demoDataSeeder.seedEverything(context)
            } else {
                demoDataSeeder.seedConfigOnly()
            }
            _uiState.value = _uiState.value.copy(
                isBusy = false,
                message = result.fold(
                    onSuccess = { it.log.joinToString("\n") },
                    onFailure = { it.message ?: "Seeding failed." }
                )
            )
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
