package com.ehshero.app.ui.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.Project
import com.ehshero.app.data.model.User
import com.ehshero.app.data.model.UserRole
import com.ehshero.app.data.remote.ProjectRepository
import com.ehshero.app.data.remote.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class UserManagementUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val projects: List<Project> = emptyList(),
    val searchQuery: String = "",
    val isCreatingUser: Boolean = false,
    val message: String? = null
)

class UserManagementViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val projectRepository: ProjectRepository = ProjectRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    private var allUsers: List<User> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                userRepository.observeAllUsers(),
                projectRepository.observeProjects()
            ) { users, projects -> users to projects }
                .collect { (users, projects) ->
                    allUsers = users
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        users = filterUsers(users, _uiState.value.searchQuery),
                        projects = projects
                    )
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, users = filterUsers(allUsers, query))
    }

    private fun filterUsers(users: List<User>, query: String): List<User> {
        if (query.isBlank()) return users.sortedByDescending { it.totalXp }
        val q = query.trim().lowercase()
        return users.filter {
            it.name.lowercase().contains(q) || it.employeeId.lowercase().contains(q) || it.email.lowercase().contains(q)
        }.sortedByDescending { it.totalXp }
    }

    fun setRole(uid: String, role: UserRole) {
        viewModelScope.launch { userRepository.setUserRole(uid, role) }
    }

    fun setActive(uid: String, active: Boolean) {
        viewModelScope.launch { userRepository.setUserStatus(uid, active) }
    }

    fun createUser(
        context: Context,
        employeeId: String,
        name: String,
        email: String,
        designation: String,
        projectId: String,
        projectName: String,
        role: UserRole
    ) {
        if (employeeId.isBlank() || name.isBlank() || email.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Employee ID, name and email are required.")
            return
        }
        _uiState.value = _uiState.value.copy(isCreatingUser = true)
        viewModelScope.launch {
            val result = userRepository.createStaffAccount(
                context = context,
                employeeId = employeeId,
                name = name,
                email = email,
                temporaryPassword = "EhsHero@123",
                designation = designation,
                projectId = projectId,
                projectName = projectName,
                role = role
            )
            _uiState.value = _uiState.value.copy(
                isCreatingUser = false,
                message = result.fold(
                    onSuccess = { "Created $name - temporary password: EhsHero@123" },
                    onFailure = { it.message ?: "Could not create user." }
                )
            )
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
