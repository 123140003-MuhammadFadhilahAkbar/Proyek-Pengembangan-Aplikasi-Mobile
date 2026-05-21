package com.learncore.core.di

import com.learncore.core.network.HttpClientFactory
import com.learncore.core.util.DatabaseDriverFactory
import com.learncore.data.local.LearnCoreDatabase
import com.learncore.data.local.datastore.DataStoreFactory
import com.learncore.data.local.datastore.UserPreferences
import com.learncore.data.remote.api.GeminiService
import com.learncore.data.repository.AIRepositoryImpl
import com.learncore.data.repository.TaskRepositoryImpl
import com.learncore.domain.repository.AIRepository
import com.learncore.domain.repository.TaskRepository
import com.learncore.domain.usecase.AnalyzeProductivityUseCase
import com.learncore.domain.usecase.DeleteTaskUseCase
import com.learncore.domain.usecase.GetActiveTasksUseCase
import com.learncore.domain.usecase.GetAllTasksUseCase
import com.learncore.domain.usecase.GetProductivityStatsUseCase
import com.learncore.domain.usecase.GetTaskByIdUseCase
import com.learncore.domain.usecase.GetTasksByQuadrantUseCase
import com.learncore.domain.usecase.RecordPomodoroSessionUseCase
import com.learncore.domain.usecase.SaveTaskUseCase
import com.learncore.domain.usecase.ToggleTaskCompletionUseCase
import com.learncore.presentation.screens.ai.AIAssistantViewModel
import com.learncore.presentation.screens.dashboard.DashboardViewModel
import com.learncore.presentation.screens.pomodoro.PomodoroViewModel
import com.learncore.presentation.screens.profile.ProfileViewModel
import com.learncore.presentation.screens.tasks.AddEditTaskViewModel
import com.learncore.presentation.screens.tasks.TaskDetailViewModel
import com.learncore.presentation.screens.tasks.TaskListViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create(enableLogging = true) }
    singleOf(::GeminiService)
}

val databaseModule = module {
    single {
        val driverFactory: DatabaseDriverFactory = get()
        LearnCoreDatabase(driverFactory.createDriver())
    }
}

val preferencesModule = module {
    single { get<DataStoreFactory>().create() }
    singleOf(::UserPreferences)
}

val repositoryModule = module {
    singleOf(::TaskRepositoryImpl) bind TaskRepository::class
    singleOf(::AIRepositoryImpl) bind AIRepository::class
}

val useCaseModule = module {
    singleOf(::GetAllTasksUseCase)
    singleOf(::GetTasksByQuadrantUseCase)
    singleOf(::GetActiveTasksUseCase)
    singleOf(::GetTaskByIdUseCase)
    singleOf(::SaveTaskUseCase)
    singleOf(::DeleteTaskUseCase)
    singleOf(::ToggleTaskCompletionUseCase)
    singleOf(::GetProductivityStatsUseCase)
    singleOf(::RecordPomodoroSessionUseCase)
    singleOf(::AnalyzeProductivityUseCase)
}

val viewModelModule = module {
    viewModelOf(::DashboardViewModel)
    viewModelOf(::TaskListViewModel)
    viewModelOf(::TaskDetailViewModel)
    viewModelOf(::AddEditTaskViewModel)
    viewModelOf(::PomodoroViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::AIAssistantViewModel)
}

val sharedModules = listOf(
    networkModule,
    databaseModule,
    preferencesModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)

fun initKoin(
    platformModules: List<Module> = emptyList(),
    config: KoinAppDeclaration? = null
) {
    startKoin {
        config?.invoke(this)
        modules(platformModules + sharedModules)
    }
}
