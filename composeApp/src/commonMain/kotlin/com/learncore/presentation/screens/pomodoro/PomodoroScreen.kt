package com.learncore.presentation.screens.pomodoro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learncore.domain.model.Task
import org.koin.compose.viewmodel.koinViewModel

private val WorkColor = Color(0xFFE53935)
private val BreakColor = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val timerColor by animateColorAsState(
        targetValue = if (uiState.phase == TimerPhase.WORK) WorkColor else BreakColor,
        animationSpec = tween(500),
        label = "timerColor"
    )

    val progress by animateFloatAsState(
        targetValue = uiState.progress,
        animationSpec = tween(800),
        label = "timerProgress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Focus Timer",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Phase label
            Text(
                text = if (uiState.phase == TimerPhase.WORK) "Work Session" else "Break Time",
                style = MaterialTheme.typography.labelLarge,
                color = timerColor,
                fontWeight = FontWeight.Medium
            )

            // Circular timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = timerColor.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = timerColor,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.formattedTime,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Session ${uiState.sessionCount + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = viewModel::reset,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(22.dp)
                    )
                }

                FilledIconButton(
                    onClick = viewModel::playPause,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = timerColor
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.status == TimerStatus.RUNNING) {
                            Icons.Outlined.Pause
                        } else {
                            Icons.Outlined.PlayArrow
                        },
                        contentDescription = if (uiState.status == TimerStatus.RUNNING) "Pause" else "Play",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.size(48.dp))
            }

            // Task selector
            TaskSelector(
                tasks = uiState.activeTasks,
                selectedTask = uiState.selectedTask,
                onTaskSelected = viewModel::selectTask
            )

            // Progress tracker
            if (uiState.sessionCount > 0) {
                SessionProgressRow(count = uiState.sessionCount)
            }
        }
    }
}

@Composable
private fun TaskSelector(
    tasks: List<Task>,
    selectedTask: Task?,
    onTaskSelected: (Task?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            TextButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Focusing on",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedTask?.title ?: "No task selected",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedTask != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("No task", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = {
                        onTaskSelected(null)
                        expanded = false
                    }
                )
                tasks.forEach { task ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = task.quadrant.action,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onTaskSelected(task)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionProgressRow(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sessions completed:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
