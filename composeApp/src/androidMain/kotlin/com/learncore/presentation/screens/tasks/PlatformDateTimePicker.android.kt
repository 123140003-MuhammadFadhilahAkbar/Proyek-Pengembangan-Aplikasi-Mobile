package com.learncore.presentation.screens.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@Composable
actual fun PlatformDateTimePicker(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }

        val timePicker = TimePickerDialog(
            context,
            { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                onConfirm(cal.timeInMillis)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        )
        timePicker.setOnCancelListener { onDismiss() }

        val datePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                timePicker.show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.setOnCancelListener { onDismiss() }
        datePicker.show()
    }
}
