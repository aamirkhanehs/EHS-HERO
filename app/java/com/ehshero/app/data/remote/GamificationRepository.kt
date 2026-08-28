package com.ehshero.app.data.remote

import com.ehshero.app.data.model.ActivityStatus
import com.ehshero.app.data.model.ActivityType
import com.ehshero.app.data.model.Badge
import com.ehshero.app.data.model.LevelDef
import com.ehshero.app.data.model.PointRulesSettings
import com.ehshero.app.data.model.SafetyActivity
import com.ehshero.app.data.model.User
import com.ehshero.app.data.model.UserBadge
import com.ehshero.app.data.model.XpTransaction
import com.ehshero.app.data.seed.DefaultConfig
import com.ehshero.app.domain.GamificationEngine
import com.ehshero.app.domain.UserActivityCounts
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/** What actually happened as a result of one approval - used for the HSE
 * side's own confirmation UI. The staff member's own celebration overlays
 * are triggered separately, client-side, by StaffHomeViewModel comparing
 * consecutive live user snapshots - approvals can happen from a different
 * device entirely, so nothing here should assume it's on-screen together
 * with the affected user's UI. */
data class ApprovalOutcome(
    val activityXp: Int,
    val streakBonusXp: Int,
    val newStreak: Int,
    val newTotalXp: Int,
    val leveledUpTo: LevelDef?,
    val newlyUnlockedBadges: List<Badge>
)

class GamificationRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.firestore
) {
    private fun col(name: String) = firestore.collection(name)

    // ---- Config reads (levels / badges / point rules), each falling back
    // ---- to DefaultConfig if the project hasn't been seeded yet, so the
    // ---- app never shows a blank screen on a fresh Firebase project.

    suspend fun getLevels(): List<LevelDef> {
        val snapshot = col(FirestoreCollections.LEVELS).get().await()
        val levels = snapshot.toObjects(LevelDef::class.java)
        return levels.ifEmpty { DefaultConfig.DEFAULT_LEVELS }.sortedBy { it.levelNumber }
    }

    suspend fun getBadges(): List<Badge> {
        val snapshot = col(FirestoreCollections.BADGES).get().await()
        val badges = snapshot.toObjects(Badge::class.java)
        return badges.ifEmpty { DefaultConfig.DEFAULT_BADGES }
    }

    suspend fun getPointRules(): Map<String, Int> {
        val doc = col(FirestoreCollections.SETTINGS).document("pointRules").get().await()
        val settings = doc.toObject(PointRulesSettings::class.java)
        val map = settings?.xpByActivityType?.mapValues { it.value.toInt() }
        return if (map.isNullOrEmpty()) DefaultConfig.DEFAULT_POINT_RULES else map
    }

    fun observeUserBadgeIds(uid: String): Flow<Set<String>> = callbackFlow {
        val registration = col(FirestoreCollections.USER_BADGES)
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val ids = snapshot?.toObjects(UserBadge::class.java)?.map { it.badgeId }?.toSet() ?: emptySet()
                trySend(ids)
            }
        awaitClose { registration.remove() }
    }

    /**
     * The heart of the app: SAFETY ACTIVITY -> APPROVAL -> XP -> LEVEL ->
     * BADGE -> RANK -> REWARD (spec section 31), run as a single Firestore
     * transaction so an activity can never end up "approved" without its
     * XP, or vice versa.
     *
     * Levels/badges/already-unlocked-badge-ids are read once *before* the
     * transaction (Firestore transactions can only read specific document
     * references, not run collection queries) - they change rarely enough
     * that using a value that's a few seconds stale is an acceptable
     * trade-off for keeping the transaction itself simple and fast.
     */
    suspend fun approveActivity(
        activityId: String,
        reviewerUid: String,
        reviewerName: String
    ): Result<ApprovalOutcome> = runCatching {
        val activityRef = col(FirestoreCollections.ACTIVITIES).document(activityId)

        // Firestore transactions can only read specific document references,
        // not run whereEqualTo/collection queries - so anything that needs a
        // query (levels, badges, this user's already-unlocked badge ids) is
        // fetched once, just before the transaction starts. All of these
        // change rarely enough that a value that's a few seconds stale is an
        // acceptable trade-off for keeping the transaction itself simple.
        val preActivitySnap = activityRef.get().await()
        val preActivityUserId = preActivitySnap.getString("userId")
            ?: throw IllegalStateException("Activity not found.")

        val levels = getLevels()
        val badges = getBadges()
        val alreadyUnlockedIds = col(FirestoreCollections.USER_BADGES)
            .whereEqualTo("userId", preActivityUserId)
            .get()
            .await()
            .toObjects(UserBadge::class.java)
            .map { it.badgeId }
            .toSet()

        val outcome = firestore.runTransaction { transaction ->
            val activitySnap = transaction.get(activityRef)
            val activity = activitySnap.toObject(SafetyActivity::class.java)
                ?: throw IllegalStateException("Activity not found.")
            if (activity.statusEnum != ActivityStatus.PENDING) {
                throw IllegalStateException("This activity has already been reviewed.")
            }

            val userRef = col(FirestoreCollections.USERS).document(activity.userId)
            val userSnap = transaction.get(userRef)
            val user = userSnap.toObject(User::class.java)
                ?: throw IllegalStateException("Submitting user no longer exists.")

            val now = System.currentTimeMillis()
            val newStreak = GamificationEngine.nextStreakCount(user.currentStreak, user.lastActivityDateMillis, now)
            val streakBonus = GamificationEngine.streakBonusXp(newStreak)
            val activityXp = activity.xpValue
            val totalDelta = activityXp + streakBonus
            val newTotalXp = user.totalXp + totalDelta
            val newLevel = GamificationEngine.currentLevel(newTotalXp, levels)
            val leveledUpTo = GamificationEngine.levelUpIfAny(user.totalXp, newTotalXp, levels)

            val activityType = activity.activityTypeEnum
            val newCounts = UserActivityCounts(
                approvedObservationCount = user.approvedObservationCount + if (activityType == ActivityType.SAFETY_OBSERVATION) 1 else 0,
                tbtCount = user.approvedTbtCount + if (activityType == ActivityType.TBT) 1 else 0,
                hazardCount = user.approvedHazardCount + if (activityType == ActivityType.HAZARD_IDENTIFICATION) 1 else 0,
                approvedTotalCount = user.approvedTotalCount + 1,
                nearMissCount = user.approvedNearMissCount + if (activityType == ActivityType.NEAR_MISS) 1 else 0,
                trainingCount = user.approvedTrainingCount + if (activityType == ActivityType.TRAINING) 1 else 0,
                goodPracticeCount = user.approvedGoodPracticeCount + if (activityType == ActivityType.GOOD_PRACTICE) 1 else 0,
                rescueCount = user.approvedRescueCount + if (activityType == ActivityType.RESCUE_TRAINING) 1 else 0,
                totalXp = newTotalXp
            )
            val newBadges = GamificationEngine.newlyUnlockedBadges(newCounts, badges, alreadyUnlockedIds)

            // --- writes ---
            transaction.update(
                activityRef,
                mapOf(
                    "status" to ActivityStatus.APPROVED.name,
                    "reviewedByUid" to reviewerUid,
                    "reviewedByName" to reviewerName
                )
            )
            transaction.set(
                col(FirestoreCollections.APPROVALS).document(UUID.randomUUID().toString()),
                mapOf(
                    "approvalId" to UUID.randomUUID().toString(),
                    "activityId" to activityId,
                    "userId" to activity.userId,
                    "reviewedByUid" to reviewerUid,
                    "reviewedByName" to reviewerName,
                    "decision" to ActivityStatus.APPROVED.name,
                    "xpAwarded" to totalDelta,
                    "rejectionReason" to ""
                )
            )
            transaction.set(
                col(FirestoreCollections.XP_TRANSACTIONS).document(UUID.randomUUID().toString()),
                XpTransaction(
                    userId = activity.userId,
                    activityId = activityId,
                    xp = activityXp,
                    reason = "${activityType.displayName} approved",
                    approvedByUid = reviewerUid,
                    approvedByName = reviewerName
                )
            )
            if (streakBonus > 0) {
                transaction.set(
                    col(FirestoreCollections.XP_TRANSACTIONS).document(UUID.randomUUID().toString()),
                    XpTransaction(
                        userId = activity.userId,
                        activityId = activityId,
                        xp = streakBonus,
                        reason = "$newStreak-day safety streak bonus",
                        approvedByUid = reviewerUid,
                        approvedByName = reviewerName
                    )
                )
            }
            transaction.update(
                userRef,
                mapOf(
                    "totalXp" to newTotalXp,
                    "level" to newLevel.levelNumber,
                    "currentStreak" to newStreak,
                    "longestStreak" to maxOf(user.longestStreak, newStreak),
                    "lastActivityDateMillis" to now,
                    "approvedObservationCount" to newCounts.approvedObservationCount,
                    "approvedTbtCount" to newCounts.tbtCount,
                    "approvedHazardCount" to newCounts.hazardCount,
                    "approvedTotalCount" to newCounts.approvedTotalCount,
                    "approvedNearMissCount" to newCounts.nearMissCount,
                    "approvedTrainingCount" to newCounts.trainingCount,
                    "approvedGoodPracticeCount" to newCounts.goodPracticeCount,
                    "approvedRescueCount" to newCounts.rescueCount
                )
            )
            newBadges.forEach { badge ->
                transaction.set(
                    col(FirestoreCollections.USER_BADGES).document("${activity.userId}_${badge.badgeId}"),
                    UserBadge(userId = activity.userId, badgeId = badge.badgeId)
                )
            }
            if (activity.missionId.isNotBlank()) {
                transaction.set(
                    col(FirestoreCollections.USER_MISSIONS).document("${activity.userId}_${activity.missionId}"),
                    mapOf("status" to com.ehshero.app.data.model.MissionStatus.APPROVED.name),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }
            transaction.set(
                col(FirestoreCollections.NOTIFICATIONS).document(UUID.randomUUID().toString()),
                mapOf(
                    "userId" to activity.userId,
                    "type" to "ACTIVITY_APPROVED",
                    "title" to "${activityType.displayName} approved",
                    "body" to "+$activityXp XP earned" + if (streakBonus > 0) " (+$streakBonus streak bonus)" else "",
                    "read" to false,
                    "relatedId" to activityId
                )
            )
            if (leveledUpTo != null) {
                transaction.set(
                    col(FirestoreCollections.NOTIFICATIONS).document(UUID.randomUUID().toString()),
                    mapOf(
                        "userId" to activity.userId,
                        "type" to "LEVEL_UP",
                        "title" to "Level up!",
                        "body" to "You reached Level ${leveledUpTo.levelNumber}: ${leveledUpTo.title}",
                        "read" to false,
                        "relatedId" to leveledUpTo.levelNumber.toString()
                    )
                )
            }
            newBadges.forEach { badge ->
                transaction.set(
                    col(FirestoreCollections.NOTIFICATIONS).document(UUID.randomUUID().toString()),
                    mapOf(
                        "userId" to activity.userId,
                        "type" to "BADGE_UNLOCKED",
                        "title" to "Badge unlocked: ${badge.name}",
                        "body" to badge.description,
                        "read" to false,
                        "relatedId" to badge.badgeId
                    )
                )
            }

            ApprovalOutcome(
                activityXp = activityXp,
                streakBonusXp = streakBonus,
                newStreak = newStreak,
                newTotalXp = newTotalXp,
                leveledUpTo = leveledUpTo,
                newlyUnlockedBadges = newBadges
            )
        }.await()

        outcome
    }

    suspend fun rejectActivity(
        activityId: String,
        reviewerUid: String,
        reviewerName: String,
        reason: String
    ): Result<Unit> = runCatching {
        val activityRef = col(FirestoreCollections.ACTIVITIES).document(activityId)
        firestore.runTransaction { transaction ->
            val activitySnap = transaction.get(activityRef)
            val activity = activitySnap.toObject(SafetyActivity::class.java)
                ?: throw IllegalStateException("Activity not found.")
            if (activity.statusEnum != ActivityStatus.PENDING) {
                throw IllegalStateException("This activity has already been reviewed.")
            }
            transaction.update(
                activityRef,
                mapOf(
                    "status" to ActivityStatus.REJECTED.name,
                    "reviewedByUid" to reviewerUid,
                    "reviewedByName" to reviewerName,
                    "rejectionReason" to reason
                )
            )
            transaction.set(
                col(FirestoreCollections.APPROVALS).document(UUID.randomUUID().toString()),
                mapOf(
                    "activityId" to activityId,
                    "userId" to activity.userId,
                    "reviewedByUid" to reviewerUid,
                    "reviewedByName" to reviewerName,
                    "decision" to ActivityStatus.REJECTED.name,
                    "xpAwarded" to 0,
                    "rejectionReason" to reason
                )
            )
            transaction.set(
                col(FirestoreCollections.NOTIFICATIONS).document(UUID.randomUUID().toString()),
                mapOf(
                    "userId" to activity.userId,
                    "type" to "ACTIVITY_REJECTED",
                    "title" to "${activity.activityTypeEnum.displayName} not approved",
                    "body" to reason.ifBlank { "See your activity history for details." },
                    "read" to false,
                    "relatedId" to activityId
                )
            )
            Unit
        }.await()
    }

    /** HSE/Admin manual XP award or deduction outside the normal approval
     * flow (spec section 4: "Award XP / Deduct XP when required"). */
    suspend fun adjustXpManually(
        uid: String,
        delta: Int,
        reason: String,
        adjustedByUid: String,
        adjustedByName: String
    ): Result<Unit> = runCatching {
        val userRef = col(FirestoreCollections.USERS).document(uid)
        val levels = getLevels()
        firestore.runTransaction { transaction ->
            val userSnap = transaction.get(userRef)
            val user = userSnap.toObject(User::class.java) ?: throw IllegalStateException("User not found.")
            val newTotalXp = (user.totalXp + delta).coerceAtLeast(0)
            val newLevel = GamificationEngine.currentLevel(newTotalXp, levels)
            transaction.update(userRef, mapOf("totalXp" to newTotalXp, "level" to newLevel.levelNumber))
            transaction.set(
                col(FirestoreCollections.XP_TRANSACTIONS).document(UUID.randomUUID().toString()),
                XpTransaction(
                    userId = uid,
                    xp = delta,
                    reason = reason,
                    approvedByUid = adjustedByUid,
                    approvedByName = adjustedByName
                )
            )
            transaction.set(
                col(FirestoreCollections.NOTIFICATIONS).document(UUID.randomUUID().toString()),
                mapOf(
                    "userId" to uid,
                    "type" to "XP_RECEIVED",
                    "title" to if (delta >= 0) "XP awarded" else "XP adjusted",
                    "body" to "$reason (${if (delta >= 0) "+" else ""}$delta XP)",
                    "read" to false,
                    "relatedId" to ""
                )
            )
            Unit
        }.await()
    }
}
