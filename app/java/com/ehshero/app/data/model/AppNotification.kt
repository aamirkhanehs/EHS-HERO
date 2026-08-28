package com.ehshero.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** Firestore collection: `notifications/{notificationId}`. Named
 * "AppNotification" to avoid clashing with android.app.Notification. */
data class AppNotification(
    var notificationId: String = "",
    var userId: String = "",
    var type: String = NotificationType.XP_RECEIVED.name,
    var title: String = "",
    var body: String = "",
    var read: Boolean = false,
    var relatedId: String = "",
    @ServerTimestamp
    var createdAt: Date? = null
)
