package com.learncore.presentation.screens.tasks

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformDateTimePicker(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // iOS: stub — bisa diimplementasikan dengan UIKit DatePicker jika diperlukan
    onDismiss()
}
