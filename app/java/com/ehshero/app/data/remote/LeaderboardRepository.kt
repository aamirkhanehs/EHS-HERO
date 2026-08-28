package com.ehshero.app.data.remote

import com.ehshero.app.data.model.LeaderboardPeriod
import com.ehshero.app.data.model.User
import com.ehshero.app.data.model.UserStatus
import com.ehshero.app.data.model.XpTransaction
import com.ehshero.app.domain.LeaderboardEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Date

/**
 * Leaderboard queries (spec section 9). The default all-time view is a live
 * listener on `users` ordered by totalXp, so it updates the instant an HSE
 * approval changes someone's score. Period filters (daily/weekly/monthly/
 * yearly) and project filters instead aggregate `xpTransactions` for that
 * window on demand - correct and simple, at the cost of not being a live
 * listener; see README "Scaling notes" if this ever needs to move to
 * denormalized rolling counters for a much larger organisation.
 */
class LeaderboardRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.firestore
) {
    private fun usersCollection() = firestore.collection(FirestoreCollections.USERS)

    fun observeAllTimeLeaderboard(limit: Long = 100): Flow<List<LeaderboardEntry>> = callbackFlow {
        val registration = usersCollection()
            .whereEqualTo("status", UserStatus.ACTIVE.name)
            .orderBy("totalXp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users.mapIndexed { index, u -> u.toEntry(u.totalXp, index + 1) })
            }
        awaitClose { registration.remove() }
    }

    suspend fun getFilteredLeaderboard(
        period: LeaderboardPeriod,
        projectId: String?
    ): List<LeaderboardEntry> {
        var userQuery: Query = usersCollection().whereEqualTo("status", UserStatus.ACTIVE.name)
        if (!projectId.isNullOrBlank()) userQuery = userQuery.whereEqualTo("projectId", projectId)
        val users = userQuery.get().await().toObjects(User::class.java)

        if (period == LeaderboardPeriod.ALL_TIME) {
            return users
                .sortedByDescending { it.totalXp }
                .mapIndexed { index, u -> u.toEntry(u.totalXp, index + 1) }
        }

        val periodStartMillis = startMillisFor(period)
        val transactions = firestore.collection(FirestoreCollections.XP_TRANSACTIONS)
            .whereGreaterThanOrEqualTo("date", Date(periodStartMillis))
            .get()
            .await()
            .toObjects(XpTransaction::class.java)

        val xpByUser: Map<String, Int> = transactions.groupBy { it.userId }
            .mapValues { (_, txs) -> txs.sumOf { it.xp } }

        return users
            .map { u -> u to (xpByUser[u.uid] ?: 0) }
            .sortedByDescending { it.second }
            .mapIndexed { index, (u, xp) -> u.toEntry(xp, index + 1) }
    }

    private fun User.toEntry(xp: Int, rank: Int) = LeaderboardEntry(
        uid = uid,
        name = name,
        designation = designation,
        projectName = projectName,
        level = level,
        xp = xp,
        avatarId = avatarId,
        rank = rank
    )

    /** Cheap all-time rank lookup for a single user (used on the Staff home
     * header) via a COUNT aggregation query rather than fetching every
     * user - "how many active users have more XP than me, plus one". */
    suspend fun getMyRank(totalXp: Int): Int {
        val higherCount = usersCollection()
            .whereEqualTo("status", UserStatus.ACTIVE.name)
            .whereGreaterThan("totalXp", totalXp)
            .count()
            .get(com.google.firebase.firestore.AggregateSource.SERVER)
            .await()
            .count
        return higherCount.toInt() + 1
    }

    private fun startMillisFor(period: LeaderboardPeriod): Long {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = when (period) {
            LeaderboardPeriod.DAILY -> today
            LeaderboardPeriod.WEEKLY -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            LeaderboardPeriod.MONTHLY -> today.withDayOfMonth(1)
            LeaderboardPeriod.YEARLY -> today.withDayOfYear(1)
            LeaderboardPeriod.ALL_TIME -> LocalDate.ofEpochDay(0)
        }
        return start.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
