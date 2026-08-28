package com.ehshero.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ehshero.app.data.model.UserRole

object Routes {
    const val LOGIN = "login"

    const val STAFF_HOME = "staff_home"
    const val MISSIONS = "missions"
    const val ACTIVITY_SUBMIT = "activity_submit"
    const val ACTIVITY_HISTORY = "activity_history"
    const val BADGES = "badges"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"

    const val HSE_DASHBOARD = "hse_dashboard"
    const val APPROVALS = "approvals"
    const val APPROVAL_DETAIL = "approval_detail"

    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_USERS = "admin_users"
    const val ADMIN_SETTINGS = "admin_settings"
    const val ADMIN_REPORTS = "admin_reports"

    const val ARG_ACTIVITY_ID = "activityId"
    const val ARG_MISSION_ID = "missionId"
    const val ARG_PRESET_TYPE = "presetType"

    fun approvalDetail(activityId: String) = "$APPROVAL_DETAIL/$activityId"

    /** Always emits both query params (empty string = "none") rather than
     * omitting missing ones, so it always matches the same fixed route
     * pattern declared in EHSNavGraph - simpler to verify correct than
     * relying on Navigation Compose's handling of partially-omitted
     * optional query arguments. */
    fun activitySubmit(missionId: String? = null, presetType: String? = null): String {
        val m = java.net.URLEncoder.encode(missionId.orEmpty(), "UTF-8")
        val t = java.net.URLEncoder.encode(presetType.orEmpty(), "UTF-8")
        return "$ACTIVITY_SUBMIT?$ARG_MISSION_ID=$m&$ARG_PRESET_TYPE=$t"
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

private val staffNavItems = listOf(
    BottomNavItem(Routes.STAFF_HOME, "Home", Icons.Filled.Home),
    BottomNavItem(Routes.MISSIONS, "Missions", Icons.Filled.Flag),
    BottomNavItem(Routes.LEADERBOARD, "Leaderboard", Icons.Filled.Leaderboard),
    BottomNavItem(Routes.ACTIVITY_HISTORY, "Activities", Icons.Filled.Assignment),
    BottomNavItem(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

private val hseNavItems = listOf(
    BottomNavItem(Routes.HSE_DASHBOARD, "Dashboard", Icons.Filled.SpaceDashboard),
    BottomNavItem(Routes.APPROVALS, "Approvals", Icons.Filled.FactCheck),
    BottomNavItem(Routes.LEADERBOARD, "Leaderboard", Icons.Filled.Leaderboard),
    BottomNavItem(Routes.ACTIVITY_HISTORY, "Activities", Icons.Filled.Assignment),
    BottomNavItem(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

private val adminNavItems = listOf(
    BottomNavItem(Routes.ADMIN_DASHBOARD, "Dashboard", Icons.Filled.SpaceDashboard),
    BottomNavItem(Routes.ADMIN_USERS, "Users", Icons.Filled.People),
    BottomNavItem(Routes.ADMIN_SETTINGS, "Settings", Icons.Filled.Settings),
    BottomNavItem(Routes.ADMIN_REPORTS, "Reports", Icons.Filled.Assessment),
    BottomNavItem(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

fun bottomNavItemsFor(role: UserRole): List<BottomNavItem> = when (role) {
    UserRole.STAFF -> staffNavItems
    UserRole.HSE -> hseNavItems
    UserRole.ADMIN -> adminNavItems
}

fun homeRouteFor(role: UserRole): String = when (role) {
    UserRole.STAFF -> Routes.STAFF_HOME
    UserRole.HSE -> Routes.HSE_DASHBOARD
    UserRole.ADMIN -> Routes.ADMIN_DASHBOARD
}
