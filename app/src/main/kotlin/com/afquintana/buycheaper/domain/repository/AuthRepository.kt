package com.afquintana.buycheaper.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(email: String, password: String)
    suspend fun register(nick: String, email: String, password: String)
    fun logout()
}
