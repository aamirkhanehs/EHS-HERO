package com.ehshero.app.domain

/** A single ranked row, produced by LeaderboardRepository from User docs (or
 * from an xpTransactions aggregation for period-filtered views) and
 * consumed by the leaderboard/podium UI components. Not a Firestore
 * document itself. */
data class LeaderboardEntry(
    val uid: String,
    val name: String,
    val designation: String = "",
    val projectName: String = "",
    val level: Int,
    val xp: Int,
    val avatarId: String,
    val rank: Int
)
