package com.ehshero.app.data.remote

import com.ehshero.app.data.model.Mission
import com.ehshero.app.data.model.MissionStatus
import com.ehshero.app.data.model.UserMission
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/** Daily missions (spec section 15) and each user's progress against them. */
class MissionRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.firestore
) {
    private fun missionsCollection() = firestore.collection(FirestoreCollections.MISSIONS)
    private fun userMissionsCollection() = firestore.collection(FirestoreCollections.USER_MISSIONS)

    fun observeActiveMissions(): Flow<List<Mission>> = callbackFlow {
        val registration = missionsCollection()
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Mission::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    fun observeUserMissions(uid: String): Flow<List<UserMission>> = callbackFlow {
        val registration = userMissionsCollection()
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(UserMission::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    /** HSE/Admin create a mission (spec sections 4, 15). */
    suspend fun createMission(mission: Mission): Result<String> = runCatching {
        val id = mission.missionId.ifBlank { UUID.randomUUID().toString() }
        missionsCollection().document(id).set(mission.copy(missionId = id)).await()
        id
    }

    suspend fun setMissionActive(missionId: String, active: Boolean) {
        missionsCollection().document(missionId).update("active", active).await()
    }

    /** Staff taps "START MISSION" - creates/overwrites their progress doc as
     * IN_PROGRESS. Safe to call repeatedly (idempotent merge-set). */
    suspend fun startMission(uid: String, missionId: String): Result<Unit> = runCatching {
        userMissionsCollection().document("${uid}_$missionId").set(
            UserMission(userId = uid, missionId = missionId, status = MissionStatus.IN_PROGRESS.name),
            SetOptions.merge()
        ).await()
    }

    /** Called right after a staff member submits the activity that
     * fulfils a mission - flips it to COMPLETED ("awaiting HSE" in the UI).
     * [GamificationRepository.approveActivity] later flips it to APPROVED
     * once HSE actually approves the underlying activity. */
    suspend fun markMissionCompleted(uid: String, missionId: String, activityId: String): Result<Unit> = runCatching {
        userMissionsCollection().document("${uid}_$missionId").set(
            mapOf(
                "userId" to uid,
                "missionId" to missionId,
                "status" to MissionStatus.COMPLETED.name,
                "activityId" to activityId
            ),
            SetOptions.merge()
        ).await()
    }
}
