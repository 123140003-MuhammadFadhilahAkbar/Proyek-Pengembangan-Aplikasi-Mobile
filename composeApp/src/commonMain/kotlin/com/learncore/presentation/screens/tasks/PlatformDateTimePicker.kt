package com.learncore.presentation.screens.tasks

import androidx.compose.runtime.Composable

@Composable
expect fun PlatformDateTimePicker(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
)
