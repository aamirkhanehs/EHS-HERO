package com.ehshero.app.data.remote

import com.ehshero.app.data.model.Project
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/** Projects (spec section 4: "Create projects"; section 24's site
 * categories live on ActivityType/HseCategory instead, since those are
 * activity-classification concepts rather than the projects themselves). */
class ProjectRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.firestore
) {
    private fun projectsCollection() = firestore.collection(FirestoreCollections.PROJECTS)

    fun observeProjects(): Flow<List<Project>> = callbackFlow {
        val registration = projectsCollection().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(Project::class.java) ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    suspend fun createOrUpdateProject(project: Project): Result<String> = runCatching {
        val id = project.projectId.ifBlank { UUID.randomUUID().toString() }
        projectsCollection().document(id).set(project.copy(projectId = id)).await()
        id
    }

    suspend fun setProjectActive(projectId: String, active: Boolean) {
        projectsCollection().document(projectId).update("active", active).await()
    }
}
