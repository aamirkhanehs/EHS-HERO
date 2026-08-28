package com.ehshero.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** Firestore collection: `userBadges/{uid}_{badgeId}` - the unlock record
 * created the moment GamificationEngine decides a badge has been earned. */
data class UserBadge(
    var userId: String = "",
    var badgeId: String = "",
    @ServerTimestamp
    var unlockedAt: Date? = null
)
