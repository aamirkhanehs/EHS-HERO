package com.ehshero.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ehshero.app.data.model.UserRole
import com.ehshero.app.data.remote.AuthRepository
import com.ehshero.app.data.remote.UserRepository
import com.ehshero.app.ui.admin.AdminDashboardScreen
import com.ehshero.app.ui.admin.AdminSettingsScreen
import com.ehshero.app.ui.admin.ReportsExportScreen
import com.ehshero.app.ui.admin.UserManagementScreen
import com.ehshero.app.ui.auth.LoginScreen
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.hse.ApprovalDetailScreen
import com.ehshero.app.ui.hse.ApprovalsScreen
import com.ehshero.app.ui.hse.HseDashboardScreen
import com.ehshero.app.ui.leaderboard.LeaderboardScreen
import com.ehshero.app.ui.notifications.NotificationsScreen
import com.ehshero.app.ui.profile.ProfileScreen
import com.ehshero.app.ui.staff.ActivityHistoryScreen
import com.ehshero.app.ui.staff.ActivitySubmitScreen
import com.ehshero.app.ui.staff.BadgesScreen
import com.ehshero.app.ui.staff.MissionsScreen
import com.ehshero.app.ui.staff.StaffHomeScreen
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.TextMedium

/** Top of the app's Compose tree: shows Login until FirebaseAuth has a
 * signed-in user, then hands off to [AuthedApp]. Re-checks currentUid after
 * a successful login/logout rather than listening to an AuthStateListener
 * continuously, since sign-in/sign-out only ever happen from a single
 * explicit user action in this app (the Login button, or Profile's Log Out
 * button), not from anywhere else. */
@Composable
fun EHSNavGraph() {
    val authRepository = remember { AuthRepository() }
    var uid by remember { mutableStateOf(authRepository.currentUid) }

    if (uid == null) {
        LoginScreen(onLoginSuccess = { uid = authRepository.currentUid })
    } else {
        AuthedApp(uid = uid!!, onLoggedOut = { uid = null })
    }
}

@Composable
private fun AuthedApp(uid: String, onLoggedOut: () -> Unit) {
    val userRepository = remember { UserRepository() }
    val user by userRepository.observeUser(uid).collectAsState(initial = null)

    val currentUser = user
    if (currentUser == null) {
        LoadingState(message = "Loading your account...")
        return
    }
    val role = currentUser.roleEnum
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { EHSBottomBar(navController = navController, role = role) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = homeRouteFor(role),
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.STAFF_HOME) {
                StaffHomeScreen(
                    uid = uid,
                    onStartMission = { missionId, type ->
                        navController.navigate(Routes.activitySubmit(missionId, type))
                    }
                )
            }
            composable(Routes.MISSIONS) {
                MissionsScreen(
                    uid = uid,
                    onStartMission = { missionId, type ->
                        navController.navigate(Routes.activitySubmit(missionId, type))
                    }
                )
            }
            composable(
                route = "${Routes.ACTIVITY_SUBMIT}?${Routes.ARG_MISSION_ID}={${Routes.ARG_MISSION_ID}}&${Routes.ARG_PRESET_TYPE}={${Routes.ARG_PRESET_TYPE}}",
                arguments = listOf(
                    navArgument(Routes.ARG_MISSION_ID) { type = NavType.StringType; defaultValue = "" },
                    navArgument(Routes.ARG_PRESET_TYPE) { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                ActivitySubmitScreen(
                    uid = uid,
                    missionId = backStackEntry.arguments?.getString(Routes.ARG_MISSION_ID)?.takeIf { it.isNotBlank() },
                    presetActivityType = backStackEntry.arguments?.getString(Routes.ARG_PRESET_TYPE)?.takeIf { it.isNotBlank() },
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Routes.ACTIVITY_HISTORY) {
                ActivityHistoryScreen(uid = if (role == UserRole.STAFF) uid else null)
            }
            composable(Routes.BADGES) {
                BadgesScreen(uid = uid)
            }
            composable(Routes.LEADERBOARD) {
                LeaderboardScreen(uid = uid)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    uid = uid,
                    onViewActivityHistory = { navController.navigate(Routes.ACTIVITY_HISTORY) },
                    onLoggedOut = onLoggedOut
                )
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(uid = uid)
            }
            composable(Routes.HSE_DASHBOARD) {
                HseDashboardScreen(onViewApprovals = { navController.navigate(Routes.APPROVALS) })
            }
            composable(Routes.APPROVALS) {
                ApprovalsScreen(
                    reviewerUid = uid,
                    reviewerName = currentUser.name,
                    onOpenActivity = { activityId -> navController.navigate(Routes.approvalDetail(activityId)) }
                )
            }
            composable(
                route = "${Routes.APPROVAL_DETAIL}/{${Routes.ARG_ACTIVITY_ID}}",
                arguments = listOf(navArgument(Routes.ARG_ACTIVITY_ID) { type = NavType.StringType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString(Routes.ARG_ACTIVITY_ID).orEmpty()
                ApprovalDetailScreen(
                    activityId = activityId,
                    reviewerUid = uid,
                    reviewerName = currentUser.name,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Routes.ADMIN_DASHBOARD) {
                AdminDashboardScreen(
                    onOpenUsers = { navController.navigate(Routes.ADMIN_USERS) },
                    onOpenSettings = { navController.navigate(Routes.ADMIN_SETTINGS) },
                    onOpenReports = { navController.navigate(Routes.ADMIN_REPORTS) }
                )
            }
            composable(Routes.ADMIN_USERS) { UserManagementScreen() }
            composable(Routes.ADMIN_SETTINGS) { AdminSettingsScreen() }
            composable(Routes.ADMIN_REPORTS) { ReportsExportScreen() }
        }
    }
}

@Composable
private fun EHSBottomBar(navController: NavHostController, role: UserRole) {
    val items = bottomNavItemsFor(role)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(containerColor = SteelPanel) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GuardianAmber,
                    selectedTextColor = GuardianAmber,
                    unselectedIconColor = TextMedium,
                    unselectedTextColor = TextMedium,
                    indicatorColor = GuardianAmberDim
                )
            )
        }
    }
}
