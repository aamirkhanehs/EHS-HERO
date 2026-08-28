package com.ehshero.app.service

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.ehshero.app.R
import com.ehshero.app.data.remote.FirebaseModule
import com.ehshero.app.data.remote.FirestoreCollections
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles FCM token refresh and any incoming push messages. A push that
 * arrives while the app is fully closed needs a server-side trigger (a
 * Cloud Function on a Firestore write, or a manual send from the Firebase
 * Console) - see README "Push notifications" for why that's an optional
 * Blaze-plan upgrade rather than wired in by default. This service still
 * registers the device's token regardless, so it's ready the moment that's
 * turned on; the in-app notification center (see NotificationRepository)
 * is what works out of the box on the free plan.
 */
class EHSMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseModule.auth.currentUser?.uid ?: return
        FirebaseModule.firestore.collection(FirestoreCollections.USERS).document(uid)
            .update("fcmToken", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "EHS Hero"
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = getString(R.string.default_notification_channel_id)
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
