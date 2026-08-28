package com.ehshero.app.data.seed

import android.content.Context
import com.ehshero.app.data.model.Project
import com.ehshero.app.data.model.UserRole
import com.ehshero.app.data.remote.FirebaseModule
import com.ehshero.app.data.remote.FirestoreCollections
import com.ehshero.app.data.remote.ProjectRepository
import com.ehshero.app.data.remote.UserRepository
import kotlinx.coroutines.tasks.await

/**
 * One-time setup helper for a freshly created Firebase project: writes the
 * default levels/badges/point-rules config (spec sections 6-8) and, if
 * asked, creates the four demo users from spec section 30. Triggered once
 * by hand from Admin > Settings > "Seed demo data" - never run
 * automatically, so it's an obvious, intentional action rather than
 * something that could silently recreate demo data in a live project.
 */
class DemoDataSeeder(
    private val userRepository: UserRepository = UserRepository(),
    private val projectRepository: ProjectRepository = ProjectRepository()
) {
    data class SeedResult(val log: List<String>)

    private data class DemoUser(
        val employeeId: String,
        val name: String,
        val email: String,
        val designation: String,
        val role: UserRole,
        val level: Int,
        val xp: Int
    )

    private val demoUsers = listOf(
        DemoUser("EMP-1001", "Aamir Khan", "aamir@ehshero.demo", "HSE Officer", UserRole.HSE, 7, 1720),
        DemoUser("EMP-1002", "Rahul Sharma", "rahul@ehshero.demo", "Rigger", UserRole.STAFF, 6, 1540),
        DemoUser("EMP-1003", "Pritam Das", "pritam@ehshero.demo", "Lineman", UserRole.STAFF, 5, 1210),
        DemoUser("EMP-1004", "Sachin Verma", "sachin@ehshero.demo", "Helper", UserRole.STAFF, 4, 850)
    )

    /** Just the config (levels/badges/point rules) - safe to run any time,
     * including in a project that already has real users. */
    suspend fun seedConfigOnly(): Result<SeedResult> = runCatching {
        val log = mutableListOf<String>()
        seedLevelsAndBadgesAndPointRules(log)
        SeedResult(log)
    }

    /** Config plus the four demo accounts from spec section 30 - intended
     * for a brand new project only. Demo users are ordinary accounts and
     * can be edited or deleted afterwards from Admin > Users. */
    suspend fun seedEverything(context: Context): Result<SeedResult> = runCatching {
        val log = mutableListOf<String>()
        seedLevelsAndBadgesAndPointRules(log)

        val projectName = "Demo Transmission Line 400kV"
        val projectId = projectRepository.createOrUpdateProject(
            Project(name = projectName, location = "Site HQ", type = "Transmission Line")
        ).getOrThrow()
        log += "Created demo project \"$projectName\"."

        for (demo in demoUsers) {
            val result = userRepository.createStaffAccount(
                context = context,
                employeeId = demo.employeeId,
                name = demo.name,
                email = demo.email,
                temporaryPassword = "EhsHero@123",
                designation = demo.designation,
                projectId = projectId,
                projectName = projectName,
                role = demo.role
            )
            result.onSuccess { uid ->
                FirebaseModule.firestore.collection(FirestoreCollections.USERS).document(uid)
                    .update(mapOf("level" to demo.level, "totalXp" to demo.xp))
                    .await()
                log += "Created ${demo.name} (${demo.email}) - password EhsHero@123"
            }.onFailure { error ->
                log += "Could not create ${demo.name}: ${error.message}"
            }
        }
        SeedResult(log)
    }

    private suspend fun seedLevelsAndBadgesAndPointRules(log: MutableList<String>) {
        val firestore = FirebaseModule.firestore

        DefaultConfig.DEFAULT_LEVELS.forEach { level ->
            firestore.collection(FirestoreCollections.LEVELS)
                .document(level.levelNumber.toString())
                .set(level)
                .await()
        }
        log += "Seeded ${DefaultConfig.DEFAULT_LEVELS.size} levels."

        DefaultConfig.DEFAULT_BADGES.forEach { badge ->
            firestore.collection(FirestoreCollections.BADGES)
                .document(badge.badgeId)
                .set(badge)
                .await()
        }
        log += "Seeded ${DefaultConfig.DEFAULT_BADGES.size} badges."

        firestore.collection(FirestoreCollections.SETTINGS)
            .document("pointRules")
            .set(mapOf("xpByActivityType" to DefaultConfig.DEFAULT_POINT_RULES.mapValues { it.value.toLong() }))
            .await()
        log += "Seeded point rules."
    }
}
