package com.afquintana.buycheaper.data.repository

import com.afquintana.buycheaper.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String) {
        require(email.isNotBlank()) { "Introduce un email" }
        require(password.isNotBlank()) { "Introduce una password" }
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun register(nick: String, email: String, password: String) {
        require(email.isNotBlank()) { "Introduce un email" }
        require(password.isNotBlank()) { "Introduce una password" }

        val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = requireNotNull(result.user) { "No se pudo crear el usuario" }

        val displayName = email.trim().substringBefore("@").ifBlank { email.trim() }
        user.updateProfile(userProfileChangeRequest { this.displayName = displayName }).await()
        firestore.collection(USERS_COLLECTION).document(user.uid).set(
            mapOf(
                "nick" to displayName,
                "normalizedNick" to displayName.lowercase(),
                "email" to email.trim()
            )
        ).await()
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
