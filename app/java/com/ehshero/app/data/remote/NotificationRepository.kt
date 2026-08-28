package com.ehshero.app.data.remote

import com.ehshero.app.data.model.AppNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** In-app notification center (spec section 18). This is the zero-cost,
 * always-on notification path that works purely on Firestore's free Spark
 * plan; see README "Push notifications" for the optional Cloud
 * Function + FCM upgrade that adds true background push. */
class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.firestore
) {
    private fun notificationsCollection() = firestore.collection(FirestoreCollections.NOTIFICATIONS)

    fun observeNotifications(uid: String, limit: Long = 100): Flow<List<AppNotification>> = callbackFlow {
        val registration = notificationsCollection()
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(AppNotification::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    suspend fun markRead(notificationId: String) {
        notificationsCollection().document(notificationId).update("read", true).await()
    }

    suspend fun markAllRead(uid: String) {
        val unread = notificationsCollection()
            .whereEqualTo("userId", uid)
            .whereEqualTo("read", false)
            .get()
            .await()
        if (unread.isEmpty) return
        val batch = firestore.batch()
        unread.documents.forEach { batch.update(it.reference, "read", true) }
        batch.commit().await()
    }
}
