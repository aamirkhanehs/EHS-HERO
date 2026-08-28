package com.ehshero.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles sign-in, sign-out and password reset. The login screen (spec
 * section 3) asks for "Employee ID / Email" in a single field - Firebase
 * Auth itself only understands email/password, so a plain Employee ID is
 * resolved to its email first via the public-read `usernameIndex/{id}` doc
 * (see firestore.rules: that collection only ever exposes an email, never a
 * password or anything else).
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseModule.auth,
    private val firestore: FirebaseFirestore = FirebaseModule.firestore
) {
    val currentUid: String? get() = auth.currentUser?.uid
    val isSignedIn: Boolean get() = auth.currentUser != null

    suspend fun login(identifier: String, password: String): Result<Unit> = runCatching {
        val email = resolveEmail(identifier)
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    suspend fun sendPasswordReset(identifier: String): Result<Unit> = runCatching {
        val email = resolveEmail(identifier)
        auth.sendPasswordResetEmail(email).await()
        Unit
    }

    fun logout() {
        auth.signOut()
    }

    private suspend fun resolveEmail(identifier: String): String {
        val trimmed = identifier.trim()
        if (trimmed.contains("@")) return trimmed
        val doc = firestore.collection(FirestoreCollections.USERNAME_INDEX)
            .document(trimmed)
            .get()
            .await()
        return doc.getString("email")
            ?: throw IllegalArgumentException("No account found for Employee ID \"$trimmed\".")
    }
}
