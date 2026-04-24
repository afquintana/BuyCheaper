package com.afquintana.buycheaper.domain.usecase

import com.afquintana.buycheaper.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = repository.login(email, password)
}

class RegisterUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(nick: String, email: String, password: String) =
        repository.register(nick, email, password)
}

class ObserveAuthStateUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.isLoggedIn
}
