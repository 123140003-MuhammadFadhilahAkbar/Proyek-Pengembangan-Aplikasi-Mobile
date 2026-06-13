package com.learncore.domain.usecase

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.AIRepository
import com.learncore.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetAllTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getAllTasks()
}

class GetTasksByQuadrantUseCase(private val repository: TaskRepository) {
    operator fun invoke(quadrant: EisenhowerQuadrant): Flow<List<Task>> =
        repository.getTasksByQuadrant(quadrant)
}

class GetActiveTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getActiveTasks()
}

class GetTaskByIdUseCase(private val repository: TaskRepository) {
    operator fun invoke(id: Long): Flow<Task?> = repository.getTaskById(id)
}

class SaveTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Long> = runCatching {
        require(task.title.isNotBlank()) { "Task title cannot be empty" }
        if (task.id == 0L) {
            repository.insertTask(task)
        } else {
            repository.updateTask(task)
            task.id
        }
    }
}

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteTask(id)
}

class ToggleTaskCompletionUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long) = repository.toggleTaskCompletion(id)
}

class GetProductivityStatsUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(): ProductivityStats = repository.getProductivityStats()
}

class RecordPomodoroSessionUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Long?, durationSeconds: Int) =
        repository.recordPomodoroSession(taskId, durationSeconds)
}

class AnalyzeProductivityUseCase(
    private val aiRepository: AIRepository,
    private val taskRepository: TaskRepository,
    private val statsUseCase: GetProductivityStatsUseCase
) {
    suspend operator fun invoke(customPrompt: String? = null): Result<String> {
        val stats = statsUseCase()
        val allTasks = taskRepository.getAllTasks().first()
        val activeTasks = allTasks.filter { !it.isCompleted }
        val completedTasks = allTasks.filter { it.isCompleted }
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val overdueTasks = activeTasks.filter { it.deadline != null && it.deadline < now }
        val urgentTasks = activeTasks.filter { it.quadrant == EisenhowerQuadrant.DO_FIRST }

        val taskContext = buildString {
            appendLine("Data tugas pengguna:")
            appendLine("- Selesai: ${stats.completedTasks}/${stats.totalTasks} (${"%.0f".format(stats.completionRate * 100)}%)")
            appendLine("- Aktif: ${activeTasks.size} | Terlambat: ${overdueTasks.size}")
            appendLine("- Fokus: ${stats.totalFocusMinutes} menit | Pomodoro: ${stats.pomodoroSessions} sesi")
            appendLine()
            appendLine("Kuadran aktif:")
            EisenhowerQuadrant.entries.forEach { q ->
                val count = activeTasks.count { it.quadrant == q }
                if (count > 0) appendLine("- ${q.displayName}: $count tugas")
            }
            if (urgentTasks.isNotEmpty()) {
                appendLine()
                appendLine("Do First:")
                urgentTasks.take(3).forEach { task ->
                    val dl = task.deadline?.toLocalDateTime(tz)?.let {
                        "${it.dayOfMonth}/${it.monthNumber}"
                    } ?: "no deadline"
                    appendLine("- ${task.title} ($dl)")
                }
            }
            if (overdueTasks.isNotEmpty()) {
                appendLine()
                appendLine("Terlambat:")
                overdueTasks.take(3).forEach { task ->
                    appendLine("- ${task.title}")
                }
            }
        }

        val prompt = if (customPrompt != null) {
            "$taskContext\nPermintaan: $customPrompt"
        } else {
            "$taskContext\nBerikan analisis singkat dan 1 saran terpenting."
        }

        return aiRepository.generateResponse(prompt, PRODUCTIVITY_SYSTEM_PROMPT)
    }

    companion object {
        private val PRODUCTIVITY_SYSTEM_PROMPT = """
            Kamu adalah LearnCore AI, asisten produktivitas ringkas.
            
            Aturan WAJIB:
            - Jawab SINGKAT — maksimal 5 poin atau 80 kata
            - Langsung ke inti tanpa pembukaan
            - Sebut nama tugas spesifik jika relevan
            - Gunakan poin pendek dengan emoji
            - Akhiri dengan 1 aksi konkret hari ini
        """.trimIndent()
    }
}
