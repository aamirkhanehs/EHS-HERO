package com.ehshero.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.remote.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val rememberMe: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSucceeded: Boolean = false,
    val resetEmailSent: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(value: String) {
        _uiState.value = _uiState.value.copy(identifier = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onRememberMeChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = value)
    }

    fun login() {
        val state = _uiState.value
        if (state.identifier.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Enter your Employee ID/Email and password.")
            return
        }
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            authRepository.login(state.identifier, state.password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSucceeded = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = friendlyMessage(error))
                }
        }
    }

    fun sendPasswordReset() {
        val state = _uiState.value
        if (state.identifier.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Enter your Employee ID/Email first, then tap Forgot Password.")
            return
        }
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            authRepository.sendPasswordReset(state.identifier)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, resetEmailSent = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = friendlyMessage(error))
                }
        }
    }

    fun consumeResetEmailSent() {
        _uiState.value = _uiState.value.copy(resetEmailSent = false)
    }

    private fun friendlyMessage(error: Throwable): String =
        error.message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
