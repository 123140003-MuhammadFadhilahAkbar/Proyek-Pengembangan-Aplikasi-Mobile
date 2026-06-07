package com.learncore.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class DeadlineSchedulerImpl(private val context: Context) : DeadlineScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(taskId: Long, taskTitle: String, deadlineMillis: Long, reminderMinutes: Int) {
        val triggerMillis = deadlineMillis - (reminderMinutes * 60 * 1000L)
        if (triggerMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, DeadlineNotificationReceiver::class.java).apply {
            putExtra(DeadlineNotificationReceiver.EXTRA_TASK_ID, taskId)
            putExtra(DeadlineNotificationReceiver.EXTRA_TASK_TITLE, taskTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    override fun cancel(taskId: Long) {
        val intent = Intent(context, DeadlineNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
