package com.learncore.presentation.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.Task
import com.learncore.presentation.components.LoadingIndicator
import com.learncore.presentation.components.QuadrantDot
import com.learncore.presentation.components.StatCard
import com.learncore.presentation.components.color
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTaskList: (String?) -> Unit,
    onNavigateToPomodoro: () -> Unit,
    onNavigateToAddTask: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LearnCore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Focus. Prioritize. Learn Smarter.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTask,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("New Task") }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Tasks Done",
                        value = "${uiState.stats.completedTasks}",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Focus (min)",
                        value = "${uiState.stats.totalFocusMinutes}",
                        accent = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Sessions",
                        value = "${uiState.stats.pomodoroSessions}",
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Eisenhower Matrix
            item {
                EisenhowerMatrixCard(
                    quadrantCounts = uiState.quadrantCounts,
                    tasksByQuadrant = EisenhowerQuadrant.entries.associateWith { q ->
                        uiState.tasks.filter { it.quadrant == q && !it.isCompleted }
                    },
                    onQuadrantClick = { quadrant -> onNavigateToTaskList(quadrant.name) }
                )
            }

            // Pomodoro shortcut card
            item {
                PomodoroShortcutCard(onClick = onNavigateToPomodoro)
            }

            // Active tasks preview
            if (uiState.tasks.any { !it.isCompleted }) {
                item {
                    Text(
                        text = "Active Tasks",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item {
                    ActiveTasksPreview(
                        tasks = uiState.tasks.filter { !it.isCompleted }.take(4),
                        onTaskClick = { /* navigate to task list */ onNavigateToTaskList(null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EisenhowerMatrixCard(
    quadrantCounts: Map<EisenhowerQuadrant, Int>,
    tasksByQuadrant: Map<EisenhowerQuadrant, List<Task>>,
    onQuadrantClick: (EisenhowerQuadrant) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Eisenhower Matrix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tap to view",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Matrix axis labels + grid
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left axis label spacer aligned with row labels below
                Spacer(modifier = Modifier.width(52.dp))
                // Column labels: URGENT / NOT URGENT
                Row(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "URGENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "NOT URGENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 1: IMPORTANT
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Row label
                Text(
                    text = "IMP.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .width(52.dp)
                        .padding(end = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuadrantCell(
                        quadrant = EisenhowerQuadrant.DO_FIRST,
                        count = quadrantCounts[EisenhowerQuadrant.DO_FIRST] ?: 0,
                        tasks = tasksByQuadrant[EisenhowerQuadrant.DO_FIRST] ?: emptyList(),
                        onClick = { onQuadrantClick(EisenhowerQuadrant.DO_FIRST) },
                        modifier = Modifier.weight(1f)
                    )
                    QuadrantCell(
                        quadrant = EisenhowerQuadrant.SCHEDULE,
                        count = quadrantCounts[EisenhowerQuadrant.SCHEDULE] ?: 0,
                        tasks = tasksByQuadrant[EisenhowerQuadrant.SCHEDULE] ?: emptyList(),
                        onClick = { onQuadrantClick(EisenhowerQuadrant.SCHEDULE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: NOT IMPORTANT
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "NOT IMP.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .width(52.dp)
                        .padding(end = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuadrantCell(
                        quadrant = EisenhowerQuadrant.DELEGATE,
                        count = quadrantCounts[EisenhowerQuadrant.DELEGATE] ?: 0,
                        tasks = tasksByQuadrant[EisenhowerQuadrant.DELEGATE] ?: emptyList(),
                        onClick = { onQuadrantClick(EisenhowerQuadrant.DELEGATE) },
                        modifier = Modifier.weight(1f)
                    )
                    QuadrantCell(
                        quadrant = EisenhowerQuadrant.ELIMINATE,
                        count = quadrantCounts[EisenhowerQuadrant.ELIMINATE] ?: 0,
                        tasks = tasksByQuadrant[EisenhowerQuadrant.ELIMINATE] ?: emptyList(),
                        onClick = { onQuadrantClick(EisenhowerQuadrant.ELIMINATE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuadrantCell(
    quadrant: EisenhowerQuadrant,
    count: Int,
    tasks: List<Task>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = quadrant.color()

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top: action label + count badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = quadrant.action,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (count > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Description
            Text(
                text = quadrant.description,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor.copy(alpha = 0.7f),
                fontSize = androidx.compose.ui.unit.TextUnit(
                    9f,
                    androidx.compose.ui.unit.TextUnitType.Sp
                )
            )

            // Task dots
            if (tasks.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    tasks.take(5).forEach { _ ->
                        QuadrantDot(quadrant = quadrant, size = 6.dp)
                    }
                    if (tasks.size > 5) {
                        Text(
                            text = "+${tasks.size - 5}",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontSize = androidx.compose.ui.unit.TextUnit(
                                8f,
                                androidx.compose.ui.unit.TextUnitType.Sp
                            )
                        )
                    }
                }
            } else {
                Text(
                    text = "No tasks",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        9f,
                        androidx.compose.ui.unit.TextUnitType.Sp
                    )
                )
            }
        }
    }
}

@Composable
private fun PomodoroShortcutCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Start Focus Session",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Pomodoro Timer — stay in the zone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ActiveTasksPreview(
    tasks: List<Task>,
    onTaskClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onTaskClick)
    ) {
        tasks.forEach { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuadrantDot(quadrant = task.quadrant, size = 8.dp)
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = task.quadrant.action,
                    style = MaterialTheme.typography.labelSmall,
                    color = task.quadrant.color()
                )
            }
        }
    }
}
