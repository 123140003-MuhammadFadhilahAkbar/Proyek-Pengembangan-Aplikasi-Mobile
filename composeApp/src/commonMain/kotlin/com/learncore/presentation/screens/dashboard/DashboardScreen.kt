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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.Task
import com.learncore.presentation.components.LoadingIndicator
import com.learncore.presentation.components.QuadrantDot
import com.learncore.presentation.components.color
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

// ── Greeting helper ──────────────────────────────────────────────────────────
private fun greetingText(): String {
    val hour = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when {
        hour < 12 -> "Good Morning"
        hour < 18 -> "Good Afternoon"
        else       -> "Good Evening"
    }
}

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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTask,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("New Task") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Header ──────────────────────────────────────────────────────
            item {
                DashboardHeader(userName = uiState.userName, photoUri = uiState.userPhotoUri)
            }

            // ── Greeting + subtitle ─────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "${greetingText()}, ${uiState.userName.trim().split(" ").firstOrNull()?.ifBlank { null } ?: "Halo"}.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Hari ini adalah kesempatan baru untuk menjadi lebih baik !",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Stat cards row ──────────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tasks Done
                    StatCardFocus(
                        label = "TASKS DONE",
                        value = "${uiState.stats.completedTasks}",
                        subValue = "/${uiState.stats.totalTasks}",
                        icon = Icons.Outlined.CheckCircle,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    // Focus Time
                    StatCardFocus(
                        label = "FOCUS TIME",
                        value = "%.1f".format(uiState.stats.totalFocusMinutes / 60f),
                        subValue = " hrs",
                        icon = Icons.Outlined.Timer,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Productivity Score ──────────────────────────────────────────
            item {
                Spacer(Modifier.height(12.dp))
                ProductivityScoreCard(
                    completedTasks = uiState.stats.completedTasks,
                    totalTasks = uiState.stats.totalTasks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }

            // ── Eisenhower Matrix ───────────────────────────────────────────
            item {
                Spacer(Modifier.height(12.dp))
                EisenhowerMatrixCard(
                    quadrantCounts = uiState.quadrantCounts,
                    tasksByQuadrant = EisenhowerQuadrant.entries.associateWith { q ->
                        uiState.tasks.filter { it.quadrant == q && !it.isCompleted }
                    },
                    onQuadrantClick = { quadrant -> onNavigateToTaskList(quadrant.name) },
                    onViewAll = { onNavigateToTaskList(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }

            // ── Pomodoro shortcut ───────────────────────────────────────────
            item {
                Spacer(Modifier.height(12.dp))
                PomodoroShortcutCard(
                    onClick = onNavigateToPomodoro,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }
        }
    }
}

// ── Dashboard Header ─────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(userName: String = "", photoUri: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar: show actual profile photo if available, else fallback initial
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri.isNotBlank()) {
                com.learncore.presentation.screens.profile.ProfileAvatarDisplay(
                    photoUri = photoUri,
                    userName = userName
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (userName.firstOrNull()?.uppercaseChar() ?: 'U').toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = "LearnCore",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Stat Card (FocusFlow style) ───────────────────────────────────────────────

@Composable
private fun StatCardFocus(
    label: String,
    value: String,
    subValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp, start = 1.dp)
                )
            }
        }
    }
}

// ── Productivity Score Card ───────────────────────────────────────────────────

@Composable
private fun ProductivityScoreCard(
    completedTasks: Int,
    totalTasks: Int,
    modifier: Modifier = Modifier
) {
    val completionRate = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks
    val percentage = (completionRate * 100).toInt()

    // Delta: selisih antara tasks selesai vs sisa. Positif = lebih banyak selesai.
    // Kalkulasi: (completedTasks - pendingTasks) / totalTasks * 100
    val pendingTasks = totalTasks - completedTasks
    val delta = if (totalTasks == 0) 0
    else ((completedTasks - pendingTasks).toFloat() / totalTasks * 100).toInt()
    val isPositive = delta >= 0

    val chipColor = if (isPositive) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.errorContainer
    val chipTextColor = if (isPositive) MaterialTheme.colorScheme.tertiary
    else MaterialTheme.colorScheme.error
    val deltaLabel = if (isPositive) "+$delta% ahead of pending"
    else "$delta% behind pending"

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "PRODUCTIVITY SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (totalTasks > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(chipColor)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = chipTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = deltaLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = chipTextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ── Eisenhower Matrix Card ────────────────────────────────────────────────────

@Composable
private fun EisenhowerMatrixCard(
    quadrantCounts: Map<EisenhowerQuadrant, Int>,
    tasksByQuadrant: Map<EisenhowerQuadrant, List<Task>>,
    onQuadrantClick: (EisenhowerQuadrant) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Eisenhower Matrix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "VIEW ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewAll() }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Top row: Do First + Schedule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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

            Spacer(Modifier.height(10.dp))

            // Bottom row: Delegate + Eliminate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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

// ── Quadrant Cell ─────────────────────────────────────────────────────────────

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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = accentColor.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Label + count badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quadrant.action,
                    style = MaterialTheme.typography.labelMedium,
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

            // Task dots
            if (tasks.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    tasks.take(5).forEach { _ ->
                        QuadrantDot(quadrant = quadrant, size = 8.dp)
                    }
                    if (tasks.size > 5) {
                        Text(
                            text = "+${tasks.size - 5}",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontSize = 9.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.25f))
                )
            }
        }
    }
}

// ── Pomodoro Shortcut Card ────────────────────────────────────────────────────

@Composable
private fun PomodoroShortcutCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
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