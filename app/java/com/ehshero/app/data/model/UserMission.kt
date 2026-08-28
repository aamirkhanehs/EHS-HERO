package com.ehshero.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** Firestore collection: `userMissions/{uid}_{missionId}` - one user's
 * progress against one mission. */
data class UserMission(
    var userId: String = "",
    var missionId: String = "",
    var status: String = MissionStatus.NOT_STARTED.name,
    var activityId: String = "",
    @ServerTimestamp
    var updatedAt: Date? = null
)
