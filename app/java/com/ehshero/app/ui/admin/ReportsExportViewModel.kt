package com.ehshero.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.ActivityStatus
import com.ehshero.app.data.model.Project
import com.ehshero.app.data.remote.ActivityRepository
import com.ehshero.app.data.remote.ProjectRepository
import com.ehshero.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportsUiState(
    val projects: List<Project> = emptyList(),
    val rangeDays: Int? = 30,
    val projectFilter: String? = null,
    val statusFilter: ActivityStatus? = null,
    val isExporting: Boolean = false,
    val lastExportRowCount: Int? = null,
    val message: String? = null
)

/** Backs the Export flow (spec section 25). CSV generation happens here;
 * the actual "where to save it" prompt is handled by the screen via
 * Android's Storage Access Framework (CreateDocument contract), which
 * needs no FileProvider/manifest configuration. */
class ReportsExportViewModel(
    private val activityRepository: ActivityRepository = ActivityRepository(),
    private val projectRepository: ProjectRepository = ProjectRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            projectRepository.observeProjects().collect { projects ->
                _uiState.value = _uiState.value.copy(projects = projects)
            }
        }
    }

    fun setRangeDays(days: Int?) { _uiState.value = _uiState.value.copy(rangeDays = days) }
    fun setProjectFilter(projectId: String?) { _uiState.value = _uiState.value.copy(projectFilter = projectId) }
    fun setStatusFilter(status: ActivityStatus?) { _uiState.value = _uiState.value.copy(statusFilter = status) }

    /** Builds the CSV as a single string; the caller writes it to whatever
     * URI the user picked. Returns null (and sets an error message) if
     * nothing matched the current filters. */
    suspend fun buildCsv(): String? {
        _uiState.value = _uiState.value.copy(isExporting = true)
        val state = _uiState.value
        val fromMillis = state.rangeDays?.let { System.currentTimeMillis() - it * 24L * 60L * 60L * 1000L }

        val activities = activityRepository.getActivitiesForExport(
            fromMillis = fromMillis,
            toMillis = null,
            projectId = state.projectFilter,
            status = state.statusFilter
        )

        if (activities.isEmpty()) {
            _uiState.value = _uiState.value.copy(isExporting = false, message = "No activities match these filters.")
            return null
        }

        val csv = buildString {
            appendLine(
                listOf(
                    "Date", "Employee", "Employee ID", "Project", "Activity Type",
                    "Description", "Status", "XP", "Approved By", "Approval Date"
                ).joinToString(",")
            )
            activities.forEach { activity ->
                appendLine(
                    listOf(
                        DateUtils.formatDate(activity.submittedAt),
                        csvField(activity.employeeName),
                        csvField(activity.employeeIdText),
                        csvField(activity.projectName),
                        csvField(activity.activityTypeEnum.displayName),
                        csvField(activity.description),
                        activity.status,
                        activity.xpValue.toString(),
                        csvField(activity.reviewedByName),
                        DateUtils.formatDate(activity.reviewedAt)
                    ).joinToString(",")
                )
            }
        }

        _uiState.value = _uiState.value.copy(isExporting = false, lastExportRowCount = activities.size)
        return csv
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun csvField(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
