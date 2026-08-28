package com.ehshero.app.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore collection: `users/{uid}`.
 *
 * All Firestore model classes in this app follow the same shape on purpose:
 * every property is a `var` with a default value, which gives Kotlin a
 * public no-arg constructor for free. Firestore's toObject()/POJO mapping
 * needs exactly that - without defaults, deserialization silently fails
 * and every screen just shows empty data.
 */
data class User(
    var uid: String = "",
    var employeeId: String = "",
    var name: String = "",
    var email: String = "",
    var designation: String = "",
    var projectId: String = "",
    var projectName: String = "",
    var role: String = UserRole.STAFF.name,
    var avatarId: String = "hero_01",
    var level: Int = 1,
    var totalXp: Int = 0,
    var monthlyXp: Int = 0,
    var weeklyXp: Int = 0,
    var currentStreak: Int = 0,
    var longestStreak: Int = 0,
    var lastActivityDateMillis: Long = 0L,
    var status: String = UserStatus.ACTIVE.name,
    var fcmToken: String = "",
    // Denormalized approved-activity counters, updated in the same write as
    // totalXp whenever HSE approves an activity (see
    // GamificationRepository.approveActivity). Kept on the user doc rather
    // than recomputed with count() queries on every approval - one extra
    // write is cheaper than up to 8 extra reads, and these are exactly the
    // numbers GamificationEngine.newlyUnlockedBadges needs (spec section 8).
    var approvedObservationCount: Int = 0,
    var approvedTbtCount: Int = 0,
    var approvedHazardCount: Int = 0,
    var approvedTotalCount: Int = 0,
    var approvedNearMissCount: Int = 0,
    var approvedTrainingCount: Int = 0,
    var approvedGoodPracticeCount: Int = 0,
    var approvedRescueCount: Int = 0,
    @ServerTimestamp
    var createdAt: Date? = null
) {
    @get:Exclude
    val roleEnum: UserRole
        get() = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.STAFF)

    @get:Exclude
    val statusEnum: UserStatus
        get() = runCatching { UserStatus.valueOf(status) }.getOrDefault(UserStatus.ACTIVE)

    @get:Exclude
    val isActive: Boolean
        get() = statusEnum == UserStatus.ACTIVE
}
