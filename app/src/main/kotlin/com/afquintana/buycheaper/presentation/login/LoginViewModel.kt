package com.afquintana.buycheaper.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afquintana.buycheaper.domain.usecase.LoginUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveAuthStateUseCase
import com.afquintana.buycheaper.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    observeAuthStateUseCase: ObserveAuthStateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    val isLoggedIn = observeAuthStateUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun login() {
        submit { loginUseCase(state.value.email, state.value.password) }
    }

    fun register() {
        submit { registerUseCase(state.value.email, state.value.password) }
    }

    private fun submit(action: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching { action() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
            _state.value = _state.value.copy(isLoading = false)
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
