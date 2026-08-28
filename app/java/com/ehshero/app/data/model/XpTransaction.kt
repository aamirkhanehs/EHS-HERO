package com.ehshero.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore collection: `xpTransactions/{txId}`.
 *
 * The single source of truth for every XP change - spec section 19 requires
 * this be auditable, so nothing ever mutates User.totalXp directly without
 * writing one of these first. [xp] can be negative (HSE point deductions).
 */
data class XpTransaction(
    var txId: String = "",
    var userId: String = "",
    var activityId: String = "",
    var xp: Int = 0,
    var reason: String = "",
    var approvedByUid: String = "",
    var approvedByName: String = "",
    var status: String = "CONFIRMED",
    @ServerTimestamp
    var date: Date? = null
)
