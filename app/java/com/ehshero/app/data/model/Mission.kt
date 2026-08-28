package com.ehshero.app.data.model

/** Firestore collection: `missions/{missionId}`. HSE/Admin-created daily
 * missions (spec section 15). */
data class Mission(
    var missionId: String = "",
    var title: String = "",
    var description: String = "",
    var activityType: String = ActivityType.OTHER.name,
    var xpReward: Int = 0,
    var startDateMillis: Long = 0L,
    var endDateMillis: Long = 0L,
    var createdByUid: String = "",
    var active: Boolean = true
)
