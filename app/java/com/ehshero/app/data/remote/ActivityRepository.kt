package com.ehshero.app.data.remote

import com.ehshero.app.data.model.ActivityStatus
import com.ehshero.app.data.model.SafetyActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * CRUD + queries over the `activities` collection (spec sections 11-14).
 * Every query here that combines an equality filter with an orderBy on a
 * different field needs a Firestore composite index - see
 * firebase/firestore.indexes.json, which declares exactly the ones this
 * file uses.
 */
class ActivityRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.firestore
) {
    private fun activitiesCollection() = firestore.collection(FirestoreCollections.ACTIVITIES)

    /** Creates a new PENDING activity. XP is intentionally never credited
     * here - see GamificationRepository.approveActivity for the only place
     * XP is actually granted, per spec section 6. */
    suspend fun submitActivity(activity: SafetyActivity): Result<String> = runCatching {
        val id = activity.activityId.ifBlank { UUID.randomUUID().toString() }
        val toSave = activity.copy(activityId = id, status = ActivityStatus.PENDING.name)
        activitiesCollection().document(id).set(toSave).await()
        id
    }

    suspend fun getActivity(activityId: String): SafetyActivity? =
        activitiesCollection().document(activityId).get().await().toObject(SafetyActivity::class.java)

    /** One user's own activities, most recent first - backs Activity
     * History (spec section 17) and "own XP" views for Staff. */
    fun observeUserActivities(uid: String, limit: Long = 100): Flow<List<SafetyActivity>> = callbackFlow {
        val registration = activitiesCollection()
            .whereEqualTo("userId", uid)
            .orderBy("submittedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(SafetyActivity::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    /** Every PENDING activity org-wide, oldest first so nothing sits
     * forgotten - the HSE Approvals queue (spec section 14). */
    fun observePendingApprovals(): Flow<List<SafetyActivity>> = callbackFlow {
        val registration = activitiesCollection()
            .whereEqualTo("status", ActivityStatus.PENDING.name)
            .orderBy("submittedAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(SafetyActivity::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    /** All activities org-wide, newest first - HSE/Admin's Activities tab
     * (spec section 27: capped with [limit] and paginated by raising it,
     * since Firestore doesn't offer offset-based paging). */
    fun observeAllActivities(limit: Long = 100): Flow<List<SafetyActivity>> = callbackFlow {
        val registration = activitiesCollection()
            .orderBy("submittedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(SafetyActivity::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    /**
     * One-shot fetch for CSV export (spec section 25). [projectId]/[status]
     * are applied as Firestore filters; the date range is narrowed
     * client-side afterwards so this doesn't need a separate composite
     * index for every filter combination an Admin might pick. Fine at the
     * scale this app targets (an internal HSE tool, not a multi-tenant
     * SaaS) - see README "Scaling notes" if that stops being true.
     */
    suspend fun getActivitiesForExport(
        fromMillis: Long?,
        toMillis: Long?,
        projectId: String?,
        status: ActivityStatus?
    ): List<SafetyActivity> {
        var query: Query = activitiesCollection()
        if (!projectId.isNullOrBlank()) query = query.whereEqualTo("projectId", projectId)
        if (status != null) query = query.whereEqualTo("status", status.name)
        query = query.orderBy("submittedAt", Query.Direction.DESCENDING)

        val snapshot = query.get().await()
        val all = snapshot.toObjects(SafetyActivity::class.java)
        return all.filter { activity ->
            val ts = activity.submittedAt?.time ?: return@filter true
            (fromMillis == null || ts >= fromMillis) && (toMillis == null || ts <= toMillis)
        }
    }
}
