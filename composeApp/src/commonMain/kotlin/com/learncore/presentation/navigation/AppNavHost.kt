package com.learncore.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.learncore.presentation.screens.ai.AIAssistantScreen
import com.learncore.presentation.screens.dashboard.DashboardScreen
import com.learncore.presentation.screens.pomodoro.PomodoroScreen
import com.learncore.presentation.screens.profile.AccountEditScreen
import com.learncore.presentation.screens.profile.HelpSupportScreen
import com.learncore.presentation.screens.profile.ProfileScreen
import com.learncore.presentation.screens.tasks.AddEditTaskScreen
import com.learncore.presentation.screens.tasks.TaskDetailScreen
import com.learncore.presentation.screens.tasks.TaskListScreen

private val BOTTOM_NAV_ROUTES = listOf(
    Route.Dashboard,
    Route.TaskList(),
    Route.Pomodoro,
    Route.AIAssistant,
    Route.Profile
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val actions = createNavigationActions(navController)
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val showBottomBar = currentRoute?.let { route ->
        route.contains("Dashboard") ||
                route.contains("TaskList") ||
                route.contains("Pomodoro") ||
                route.contains("AIAssistant") ||
                route.contains("Profile") &&
                !route.contains("AccountEdit") &&
                !route.contains("HelpSupport")
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                LearnCoreBottomBar(
                    currentRoute = currentRoute,
                    onNavigateTo = { destination ->
                        when (destination) {
                            "Dashboard" -> actions.navigateToDashboard()
                            "TaskList" -> actions.navigateToTaskList()
                            "Pomodoro" -> actions.navigateToPomodoro()
                            "AIAssistant" -> actions.navigateToAIAssistant()
                            "Profile" -> actions.navigateToProfile()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Dashboard,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(220)) +
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            tween(220)
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(220))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(180)) +
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(220)
                        )
            }
        ) {
            composable<Route.Dashboard> {
                DashboardScreen(
                    onNavigateToTaskList = { quadrant -> actions.navigateToTaskList(quadrant) },
                    onNavigateToPomodoro = { actions.navigateToPomodoro() },
                    onNavigateToAddTask = { actions.navigateToAddEditTask() }
                )
            }

            composable<Route.TaskList> { backStackEntry ->
                val route: Route.TaskList = backStackEntry.toRoute()
                TaskListScreen(
                    quadrantFilter = route.quadrantFilter,
                    onNavigateToDetail = { taskId -> actions.navigateToTaskDetail(taskId) },
                    onNavigateToAdd = { quadrant -> actions.navigateToAddEditTask(defaultQuadrant = quadrant) }
                )
            }

            composable<Route.TaskDetail> { backStackEntry ->
                val route: Route.TaskDetail = backStackEntry.toRoute()
                TaskDetailScreen(
                    taskId = route.taskId,
                    onNavigateBack = { actions.navigateBack() },
                    onNavigateToEdit = { actions.navigateToAddEditTask(route.taskId) }
                )
            }

            composable<Route.AddEditTask> { backStackEntry ->
                val route: Route.AddEditTask = backStackEntry.toRoute()
                AddEditTaskScreen(
                    taskId = route.taskId,
                    defaultQuadrant = route.defaultQuadrant,
                    onNavigateBack = { actions.navigateBack() }
                )
            }

            composable<Route.Pomodoro> {
                PomodoroScreen()
            }

            composable<Route.Profile> {
                ProfileScreen(
                    onNavigateToAccountEdit = { actions.navigateToAccountEdit() },
                    onNavigateToHelpSupport = { actions.navigateToHelpSupport() }
                )
            }

            composable<Route.AccountEdit> {
                AccountEditScreen(
                    onNavigateBack = { actions.navigateBack() }
                )
            }

            composable<Route.HelpSupport> {
                HelpSupportScreen(
                    onNavigateBack = { actions.navigateBack() }
                )
            }

            composable<Route.AIAssistant> {
                AIAssistantScreen()
            }
        }
    }
}

@Composable
private fun LearnCoreBottomBar(
    currentRoute: String?,
    onNavigateTo: (String) -> Unit
) {
    data class BottomNavItem(
        val key: String,
        val label: String,
        val icon: @Composable () -> Unit
    )

    val items = listOf(
        BottomNavItem("Dashboard", "Dashboard") {
            Icon(Icons.Outlined.Dashboard, contentDescription = null)
        },
        BottomNavItem("TaskList", "Tasks") {
            Icon(Icons.Outlined.Task, contentDescription = null)
        },
        BottomNavItem("Pomodoro", "Focus") {
            Icon(Icons.Outlined.Timer, contentDescription = null)
        },
        BottomNavItem("AIAssistant", "AI") {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
        },
        BottomNavItem("Profile", "Profile") {
            Icon(Icons.Outlined.Person, contentDescription = null)
        }
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute?.contains(item.key) == true,
                onClick = { onNavigateTo(item.key) },
                icon = item.icon,
                label = { Text(item.label) }
            )
        }
    }
}

private fun createNavigationActions(navController: NavHostController): NavigationActions {
    return object : NavigationActions {
        override fun navigateToDashboard() {
            navController.navigate(Route.Dashboard) {
                popUpTo(Route.Dashboard) { inclusive = true }
                launchSingleTop = true
            }
        }

        override fun navigateToTaskList(quadrantFilter: String?) {
            navController.navigate(Route.TaskList(quadrantFilter)) {
                // Pop semua TaskList lama agar filter tidak bocor antar-instance
                popUpTo<Route.Dashboard>()
                launchSingleTop = true
            }
        }

        override fun navigateToTaskDetail(taskId: Long) {
            navController.navigate(Route.TaskDetail(taskId))
        }

        override fun navigateToAddEditTask(taskId: Long?, defaultQuadrant: String?) {
            navController.navigate(Route.AddEditTask(taskId, defaultQuadrant))
        }

        override fun navigateToPomodoro() {
            navController.navigate(Route.Pomodoro) {
                launchSingleTop = true
            }
        }

        override fun navigateToProfile() {
            navController.navigate(Route.Profile) {
                launchSingleTop = true
            }
        }

        override fun navigateToAccountEdit() {
            navController.navigate(Route.AccountEdit)
        }

        override fun navigateToHelpSupport() {
            navController.navigate(Route.HelpSupport)
        }

        override fun navigateToAIAssistant() {
            navController.navigate(Route.AIAssistant) {
                launchSingleTop = true
            }
        }

        override fun navigateBack() {
            navController.popBackStack()
        }
    }
}