package com.ehshero.app.data.model

/** Firestore collection: `challenges/{challengeId}` - the Monthly Safety
 * Challenge (spec section 16). */
data class Challenge(
    var challengeId: String = "",
    var title: String = "",
    var description: String = "",
    var targetXp: Int = 0,
    var startDateMillis: Long = 0L,
    var endDateMillis: Long = 0L,
    var rewardBadgeId: String = "",
    var active: Boolean = true
)
