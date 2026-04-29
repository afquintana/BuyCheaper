package com.afquintana.buycheaper.presentation.login

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afquintana.buycheaper.domain.usecase.LoginUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveAuthStateUseCase
import com.afquintana.buycheaper.domain.usecase.RegisterUseCase
import com.afquintana.buycheaper.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sharedPreferences: SharedPreferences,
    observeAuthStateUseCase: ObserveAuthStateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        LoginUiState(
            rememberMe = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false)
        )
    )
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val authState = observeAuthStateUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    private val startupResolved = MutableStateFlow(false)
    private val allowCurrentSession = MutableStateFlow(false)

    val isLoggedIn = combine(
        authState,
        startupResolved,
        allowCurrentSession,
        state
    ) { auth, resolved, allowSession, uiState ->
        resolved && auth && (allowSession || uiState.rememberMe)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    init {
        viewModelScope.launch {
            authState.collect { loggedIn ->
                if (!startupResolved.value) {
                    if (loggedIn && !state.value.rememberMe) {
                        logoutUseCase()
                        allowCurrentSession.value = false
                    } else {
                        allowCurrentSession.value = loggedIn
                    }
                    startupResolved.value = true
                }
            }
        }
    }

    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun onNickChanged(value: String) {
        _state.value = _state.value.copy(nick = value, error = null)
    }

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    fun onConfirmPasswordChanged(value: String) {
        _state.value = _state.value.copy(confirmPassword = value, error = null)
    }

    fun onRememberMeChanged(value: Boolean) {
        sharedPreferences.edit().putBoolean(REMEMBER_ME_KEY, value).apply()
        _state.value = _state.value.copy(rememberMe = value)
    }

    fun login() {
        submit {
            loginUseCase(state.value.email, state.value.password)
            allowCurrentSession.value = true
        }
    }

    fun register() {
        val current = state.value
        if (current.password != current.confirmPassword) {
            _state.value = current.copy(error = "Las passwords no coinciden")
            return
        }
        submit {
            registerUseCase("", state.value.email, state.value.password)
            allowCurrentSession.value = true
        }
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

private const val REMEMBER_ME_KEY = "remember_me"

data class LoginUiState(
    val nick: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
