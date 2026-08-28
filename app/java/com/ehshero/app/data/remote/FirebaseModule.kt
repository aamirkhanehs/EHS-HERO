package com.ehshero.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Hand-rolled service locator instead of a DI framework (Hilt/Koin/etc).
 * This app deliberately skips annotation-processing-based DI to keep the
 * Gradle build graph as small and predictable as possible - see README
 * "Why no Hilt" for the reasoning. Every repository takes its Firebase
 * dependency as a constructor default pointing here, so swapping in a fake
 * for tests is still just one constructor argument.
 */
object FirebaseModule {

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            // Android's Firestore SDK persists to disk by default already;
            // this makes that explicit via the modern LocalCacheSettings
            // API so the OFFLINE MODE / SYNCING banner (spec section 21)
            // has real pending-writes metadata to read.
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
        }
    }

    val messaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }
}

/** Centralised Firestore collection names - see README "Data model" for the
 * full schema and firestore.rules for how access to each is restricted. */
object FirestoreCollections {
    const val USERS = "users"
    const val PROJECTS = "projects"
    const val ACTIVITIES = "activities"
    const val APPROVALS = "approvals"
    const val XP_TRANSACTIONS = "xpTransactions"
    const val BADGES = "badges"
    const val USER_BADGES = "userBadges"
    const val LEVELS = "levels"
    const val MISSIONS = "missions"
    const val USER_MISSIONS = "userMissions"
    const val CHALLENGES = "challenges"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
    const val USERNAME_INDEX = "usernameIndex"
}
