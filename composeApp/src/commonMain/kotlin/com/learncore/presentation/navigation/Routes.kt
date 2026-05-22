package com.learncore.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Dashboard : Route

    @Serializable
    data class TaskList(val quadrantFilter: String? = null) : Route

    @Serializable
    data class TaskDetail(val taskId: Long) : Route

    @Serializable
    data class AddEditTask(val taskId: Long? = null, val defaultQuadrant: String? = null) : Route

    @Serializable
    data object Pomodoro : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object AccountEdit : Route

    @Serializable
    data object HelpSupport : Route

    @Serializable
    data object AIAssistant : Route
}

interface NavigationActions {
    fun navigateToDashboard()
    fun navigateToTaskList(quadrantFilter: String? = null)
    fun navigateToTaskDetail(taskId: Long)
    fun navigateToAddEditTask(taskId: Long? = null, defaultQuadrant: String? = null)
    fun navigateToPomodoro()
    fun navigateToProfile()
    fun navigateToAccountEdit()
    fun navigateToHelpSupport()
    fun navigateToAIAssistant()
    fun navigateBack()
}