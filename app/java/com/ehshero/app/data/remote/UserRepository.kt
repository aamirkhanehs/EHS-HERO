package com.ehshero.app.data.remote

import android.content.Context
import com.ehshero.app.data.model.User
import com.ehshero.app.data.model.UserRole
import com.ehshero.app.data.model.UserStatus
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.firestore
) {
    private fun usersCollection() = firestore.collection(FirestoreCollections.USERS)

    suspend fun getUser(uid: String): User? =
        usersCollection().document(uid).get().await().toObject(User::class.java)

    /** Live profile updates - drives the XP bar/level/streak everywhere they
     * appear, so an HSE approval on another device reflects immediately. */
    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val registration = usersCollection().document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(User::class.java))
        }
        awaitClose { registration.remove() }
    }

    /** All users - HSE/Admin only per firestore.rules. Backs the leaderboard
     * and Admin > Users screen. */
    fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val registration = usersCollection().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(User::class.java) ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateAvatar(uid: String, avatarId: String) {
        usersCollection().document(uid).update("avatarId", avatarId).await()
    }

    suspend fun setUserStatus(uid: String, active: Boolean) {
        usersCollection().document(uid)
            .update("status", if (active) UserStatus.ACTIVE.name else UserStatus.DISABLED.name)
            .await()
    }

    suspend fun setUserRole(uid: String, role: UserRole) {
        usersCollection().document(uid).update("role", role.name).await()
    }

    suspend fun updateProfileFields(
        uid: String,
        name: String,
        designation: String,
        projectId: String,
        projectName: String
    ) {
        usersCollection().document(uid).update(
            mapOf(
                "name" to name,
                "designation" to designation,
                "projectId" to projectId,
                "projectName" to projectName
            )
        ).await()
    }

    /**
     * Admin-only (spec section 4): creates a brand-new Firebase Auth login
     * plus its `users/{uid}` profile, without signing the admin out of
     * their own session.
     *
     * Firebase Auth's client SDK only exposes createUserWithEmailAndPassword
     * on whichever app instance is *currently signed in*, and that call
     * also switches the active session to the new account - there's no
     * client-side "create a user but stay logged in as me" call (that's
     * normally a job for the server-side Admin SDK). This works around it
     * with a short-lived secondary [FirebaseApp] instance used only for the
     * signup call, then torn down immediately.
     *
     * If you'd rather not depend on that trick, the simpler path is to
     * create the login directly in the Firebase Console's Authentication
     * tab and then use [getUser]/Admin > Users to set up their profile
     * fields for that uid - see README "Creating users".
     */
    suspend fun createStaffAccount(
        context: Context,
        employeeId: String,
        name: String,
        email: String,
        temporaryPassword: String,
        designation: String,
        projectId: String,
        projectName: String,
        role: UserRole
    ): Result<String> = runCatching {
        val secondaryApp = FirebaseApp.initializeApp(
            context.applicationContext,
            FirebaseApp.getInstance().options,
            "EHSHeroUserCreation-${System.currentTimeMillis()}"
        ) ?: throw IllegalStateException("Could not start a secondary Firebase session for account creation.")

        try {
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
            val authResult = secondaryAuth.createUserWithEmailAndPassword(email, temporaryPassword).await()
            val uid = authResult.user?.uid
                ?: throw IllegalStateException("Account creation did not return a user id.")

            val newUser = User(
                uid = uid,
                employeeId = employeeId,
                name = name,
                email = email,
                designation = designation,
                projectId = projectId,
                projectName = projectName,
                role = role.name
            )
            usersCollection().document(uid).set(newUser).await()
            firestore.collection(FirestoreCollections.USERNAME_INDEX)
                .document(employeeId)
                .set(mapOf("email" to email))
                .await()

            secondaryAuth.signOut()
            uid
        } finally {
            secondaryApp.delete()
        }
    }
}
