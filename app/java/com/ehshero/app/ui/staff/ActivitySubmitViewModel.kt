package com.ehshero.app.ui.staff

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.ActivityType
import com.ehshero.app.data.model.HseCategory
import com.ehshero.app.data.model.ObservationType
import com.ehshero.app.data.model.SafetyActivity
import com.ehshero.app.data.model.User
import com.ehshero.app.data.remote.ActivityRepository
import com.ehshero.app.data.remote.GamificationRepository
import com.ehshero.app.data.remote.MissionRepository
import com.ehshero.app.data.remote.UserRepository
import com.ehshero.app.util.PhotoCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ActivitySubmitUiState(
    val activityType: ActivityType = ActivityType.TBT,
    val observationType: ObservationType? = null,
    val category: HseCategory? = null,
    val location: String = "",
    val description: String = "",
    val immediateAction: String = "",
    val correctiveAction: String = "",
    val remarks: String = "",
    val photoUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val awardedXpPreview: Int = ActivityType.TBT.defaultXp,
    val errorMessage: String? = null,
    val pointRules: Map<String, Int> = emptyMap(),
    val lockActivityType: Boolean = false
)

/**
 * Backs the universal "ADD SAFETY ACTIVITY" flow (spec section 11) and the
 * specialised Safety Observation fields (spec section 12) in one screen -
 * [ActivitySubmitUiState.activityType] decides which extra fields render.
 * If launched from a mission's "START MISSION" button, [presetMissionId]
 * and the mission's target type are pre-filled and the type is locked.
 */
class ActivitySubmitViewModel(
    private val uid: String,
    private val presetMissionId: String? = null,
    presetActivityType: String? = null,
    private val userRepository: UserRepository = UserRepository(),
    private val activityRepository: ActivityRepository = ActivityRepository(),
    private val gamificationRepository: GamificationRepository = GamificationRepository(),
    private val missionRepository: MissionRepository = MissionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ActivitySubmitUiState(
            activityType = ActivityType.fromNameOrNull(presetActivityType) ?: ActivityType.TBT,
            lockActivityType = !presetActivityType.isNullOrBlank()
        )
    )
    val uiState: StateFlow<ActivitySubmitUiState> = _uiState.asStateFlow()

    private var cachedUser: User? = null

    init {
        viewModelScope.launch {
            val rules = gamificationRepository.getPointRules()
            cachedUser = userRepository.getUser(uid)
            val type = _uiState.value.activityType
            _uiState.value = _uiState.value.copy(
                pointRules = rules,
                awardedXpPreview = rules[type.name] ?: type.defaultXp
            )
        }
    }

    fun onActivityTypeChange(type: ActivityType) {
        if (_uiState.value.lockActivityType) return
        val xp = _uiState.value.pointRules[type.name] ?: type.defaultXp
        _uiState.value = _uiState.value.copy(activityType = type, awardedXpPreview = xp)
    }

    fun onObservationTypeChange(type: ObservationType) {
        _uiState.value = _uiState.value.copy(observationType = type)
    }

    fun onCategoryChange(category: HseCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun onLocationChange(value: String) { _uiState.value = _uiState.value.copy(location = value, errorMessage = null) }
    fun onDescriptionChange(value: String) { _uiState.value = _uiState.value.copy(description = value, errorMessage = null) }
    fun onImmediateActionChange(value: String) { _uiState.value = _uiState.value.copy(immediateAction = value) }
    fun onCorrectiveActionChange(value: String) { _uiState.value = _uiState.value.copy(correctiveAction = value) }
    fun onRemarksChange(value: String) { _uiState.value = _uiState.value.copy(remarks = value) }
    fun onPhotoPicked(uri: Uri?) { _uiState.value = _uiState.value.copy(photoUri = uri) }

    fun submit(context: Context) {
        val state = _uiState.value
        if (state.location.isBlank() || state.description.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Location and description are required.")
            return
        }
        val user = cachedUser
        if (user == null) {
            _uiState.value = state.copy(errorMessage = "Could not load your profile - please try again.")
            return
        }

        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            val photoBase64 = state.photoUri?.let { uri ->
                withContext(Dispatchers.Default) { PhotoCompressor.compressToBase64(context, uri) }
            } ?: ""

            val activity = SafetyActivity(
                userId = uid,
                employeeName = user.name,
                employeeIdText = user.employeeId,
                projectId = user.projectId,
                projectName = user.projectName,
                activityType = state.activityType.name,
                observationType = state.observationType?.name.orEmpty(),
                category = state.category?.name.orEmpty(),
                location = state.location,
                description = state.description,
                immediateAction = state.immediateAction,
                correctiveAction = state.correctiveAction,
                remarks = state.remarks,
                photoBase64 = photoBase64.orEmpty(),
                xpValue = state.awardedXpPreview,
                missionId = presetMissionId.orEmpty()
            )

            activityRepository.submitActivity(activity)
                .onSuccess { activityId ->
                    if (!presetMissionId.isNullOrBlank()) {
                        missionRepository.markMissionCompleted(uid, presetMissionId, activityId)
                    }
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitted = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Could not submit. Please try again."
                    )
                }
        }
    }
}
