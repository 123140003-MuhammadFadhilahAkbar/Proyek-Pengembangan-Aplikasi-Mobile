package com.learncore.domain.usecase

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.AIRepository
import com.learncore.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

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
    private val statsUseCase: GetProductivityStatsUseCase
) {
    suspend operator fun invoke(customPrompt: String? = null): Result<String> {
        val stats = statsUseCase()
        val statsContext = buildString {
            appendLine("Data Produktivitas Pengguna:")
            appendLine("- Total tugas: ${stats.totalTasks}")
            appendLine("- Tugas selesai: ${stats.completedTasks}")
            appendLine("- Tingkat penyelesaian: ${"%.0f".format(stats.completionRate * 100)}%")
            appendLine("- Total fokus: ${stats.totalFocusMinutes} menit")
            appendLine("- Sesi Pomodoro: ${stats.pomodoroSessions}")
            appendLine("- Distribusi kuadran:")
            EisenhowerQuadrant.entries.forEach { q ->
                appendLine("  ${q.displayName}: ${stats.tasksByQuadrant[q] ?: 0} tugas")
            }
        }

        val prompt = if (customPrompt != null) {
            "$statsContext\n\nPermintaan: $customPrompt"
        } else {
            "$statsContext\n\nBerikan analisis produktivitas dan saran peningkatan berdasarkan data di atas."
        }

        return aiRepository.generateResponse(prompt, PRODUCTIVITY_SYSTEM_PROMPT)
    }

    companion object {
        private val PRODUCTIVITY_SYSTEM_PROMPT = """
            Kamu adalah asisten produktivitas cerdas yang membantu pelajar dan profesional.
            Spesialisasi: analisis Matriks Eisenhower, manajemen waktu, dan sesi Pomodoro.
            
            Rules:
            - Gunakan Bahasa Indonesia yang profesional namun ramah
            - Berikan analisis berdasarkan data statistik yang diberikan
            - Sertakan saran konkret dan actionable
            - Format respons dengan poin-poin yang jelas
            - Maksimal 300 kata
        """.trimIndent()
    }
}
