package com.ehshero.app.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.ActivityStatus
import com.ehshero.app.data.model.SafetyActivity
import com.ehshero.app.data.remote.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivityHistoryUiState(
    val isLoading: Boolean = true,
    val activities: List<SafetyActivity> = emptyList(),
    val statusFilter: ActivityStatus? = null
)

/** Backs the Staff "Activities" tab (spec section 17: activity history) and
 * the HSE/Admin org-wide Activities tab, depending on which repository call
 * the caller wires up. */
class ActivityHistoryViewModel(
    private val uid: String? = null,
    private val activityRepository: ActivityRepository = ActivityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityHistoryUiState())
    val uiState: StateFlow<ActivityHistoryUiState> = _uiState.asStateFlow()

    private var allLoaded: List<SafetyActivity> = emptyList()

    init {
        viewModelScope.launch {
            val flow = if (uid != null) {
                activityRepository.observeUserActivities(uid)
            } else {
                activityRepository.observeAllActivities()
            }
            flow.collect { activities ->
                allLoaded = activities
                applyFilter()
            }
        }
    }

    fun setStatusFilter(status: ActivityStatus?) {
        _uiState.value = _uiState.value.copy(statusFilter = status)
        applyFilter()
    }

    private fun applyFilter() {
        val filter = _uiState.value.statusFilter
        val filtered = if (filter == null) allLoaded else allLoaded.filter { it.statusEnum == filter }
        _uiState.value = _uiState.value.copy(isLoading = false, activities = filtered)
    }
}
