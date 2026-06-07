package com.learncore.core.notification

interface DeadlineScheduler {
    fun schedule(taskId: Long, taskTitle: String, deadlineMillis: Long, reminderMinutes: Int)
    fun cancel(taskId: Long)
}
