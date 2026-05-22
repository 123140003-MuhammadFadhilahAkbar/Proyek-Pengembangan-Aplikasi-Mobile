package com.learncore.presentation.screens.pomodoro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learncore.domain.model.Task
import com.learncore.presentation.components.color
import org.koin.compose.viewmodel.koinViewModel

private val TimerRingColor  = Color(0xFF1A237E)
private val TimerBreakColor = Color(0xFF2E7D32)
private val RingTrackColor  = Color(0xFFE8EAF6)
private val DoneGreen       = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val timerColor by animateColorAsState(
        targetValue = if (uiState.phase == TimerPhase.WORK) TimerRingColor else TimerBreakColor,
        animationSpec = tween(500),
        label = "timerColor"
    )
    val progress by animateFloatAsState(
        targetValue = uiState.progress,
        animationSpec = tween(800),
        label = "timerProgress"
    )

    val phaseLabel = if (uiState.phase == TimerPhase.WORK) "WORK SESSION" else "BREAK TIME"

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp)
            ) {

                // ── Header ───────────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Focus Timer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // ── Circular timer ───────────────────────────────────────────
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(240.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 10.dp,
                            color = RingTrackColor,
                            strokeCap = StrokeCap.Round
                        )
                        androidx.compose.material3.CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 10.dp,
                            color = timerColor,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.formattedTime,
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-1).sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = phaseLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }

                // ── Task selector ─────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(16.dp))
                    TaskSelectorPill(
                        tasks = uiState.activeTasks,
                        selectedTask = uiState.selectedTask,
                        onTaskSelected = viewModel::selectTask,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }

                // ── Controls ─────────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ControlButtonWithLabel(
                            icon = Icons.Outlined.Refresh,
                            label = "RESET",
                            onClick = viewModel::reset,
                            size = 52.dp,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(20.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(timerColor)
                                    .clickable { viewModel.playPause() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.status == TimerStatus.RUNNING)
                                        Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (uiState.status == TimerStatus.RUNNING) "PAUSE" else "START SESSION",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(Modifier.width(20.dp))
                        // ── NEXT (bukan SKIP) ─────────────────────────────────
                        ControlButtonWithLabel(
                            icon = Icons.Outlined.NavigateNext,
                            label = "NEXT",
                            onClick = viewModel::nextPhase,
                            size = 52.dp,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── NEXT UP header ────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEXT UP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "After 5m Break",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // ── Task list ─────────────────────────────────────────────
                if (uiState.activeTasks.isEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Semua tugas selesai 🎉",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else {
                    itemsIndexed(uiState.activeTasks) { _, task ->
                        val isSelected = task.id == uiState.selectedTask?.id
                        NextUpTaskRow(
                            task = task,
                            isSelected = isSelected,
                            timerColor = timerColor,
                            onToggleDone = { viewModel.toggleTaskDone(task.id) }
                        )
                    }
                }
            }
        }

        // ── Break Before Next Task overlay ────────────────────────────────
        AnimatedVisibility(
            visible = uiState.showBreakBeforeNextTask,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            BreakBeforeNextTaskOverlay(
                formattedTime = uiState.breakBeforeFormattedTime,
                progress = uiState.breakBeforeProgress,
                onSkip = viewModel::skipBreakBeforeNextTask
            )
        }
    }
}

// ── Break Before Next Task Overlay ───────────────────────────────────────────

@Composable
private fun BreakBeforeNextTaskOverlay(
    formattedTime: String,
    progress: Float,
    onSkip: () -> Unit
) {
    val breakColor = Color(0xFF2E7D32)
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "breakBeforeProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "☕",
                fontSize = 48.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Break Before Next Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tugas selesai! Istirahat sebentar sebelum mulai lagi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            // Ring timer break
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = Color(0xFFE8EAF6),
                    strokeCap = StrokeCap.Round
                )
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { animProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = breakColor,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "BREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // Tombol Skip
            OutlinedButton(
                onClick = onSkip,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.NavigateNext,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Lewati istirahat")
            }
        }
    }
}

// ── Next Up Task Row ──────────────────────────────────────────────────────────

@Composable
private fun NextUpTaskRow(
    task: Task,
    isSelected: Boolean,
    timerColor: Color,
    onToggleDone: () -> Unit
) {
    val accentColor = task.quadrant.color()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Est. 25 mins  ·  ${task.quadrant.action}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(timerColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "NOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = timerColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(4.dp))
        }

        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = "Mark done",
            tint = DoneGreen,
            modifier = Modifier
                .size(26.dp)
                .clickable { onToggleDone() }
        )
    }

    Divider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

// ── Task Selector Pill ────────────────────────────────────────────────────────

@Composable
private fun TaskSelectorPill(
    tasks: List<Task>,
    selectedTask: Task?,
    onTaskSelected: (Task?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.SyncAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = selectedTask?.title ?: "Pilih tugas yang sedang dikerjakan",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selectedTask != null) FontWeight.Medium else FontWeight.Normal,
                color = if (selectedTask != null) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Tidak ada", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = { onTaskSelected(null); expanded = false }
            )
            tasks.forEach { task ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(task.title, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                task.quadrant.action,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = { onTaskSelected(task); expanded = false }
                )
            }
        }
    }
}

// ── Control Button with Label ─────────────────────────────────────────────────

@Composable
private fun ControlButtonWithLabel(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    containerColor: Color,
    iconColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(containerColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconColor,
                modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}