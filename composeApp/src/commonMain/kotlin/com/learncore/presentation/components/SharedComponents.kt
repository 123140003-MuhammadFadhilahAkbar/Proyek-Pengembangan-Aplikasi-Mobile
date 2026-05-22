package com.learncore.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.Task
import com.learncore.presentation.theme.QuadrantDelegate
import com.learncore.presentation.theme.QuadrantDoFirst
import com.learncore.presentation.theme.QuadrantEliminate
import com.learncore.presentation.theme.QuadrantSchedule
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ==================== QUADRANT COLOR UTILS ====================

fun EisenhowerQuadrant.color(): Color = when (this) {
    EisenhowerQuadrant.DO_FIRST -> QuadrantDoFirst
    EisenhowerQuadrant.SCHEDULE -> QuadrantSchedule
    EisenhowerQuadrant.DELEGATE -> QuadrantDelegate
    EisenhowerQuadrant.ELIMINATE -> QuadrantEliminate
}

// ==================== QUADRANT DOT ====================

@Composable
fun QuadrantDot(
    quadrant: EisenhowerQuadrant,
    size: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(quadrant.color())
    )
}

// ==================== QUADRANT BADGE ====================

@Composable
fun QuadrantBadge(
    quadrant: EisenhowerQuadrant,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(quadrant.color().copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        QuadrantDot(quadrant = quadrant, size = 6.dp)
        Text(
            text = quadrant.action,
            style = MaterialTheme.typography.labelSmall,
            color = quadrant.color(),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ==================== TASK CARD ====================

/** Format an Instant deadline into a human-readable label. */
private fun formatDeadline(deadline: Instant): String {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz)
    val dt = deadline.toLocalDateTime(tz)

    val dayLabel = when {
        dt.date == now.date -> "Today"
        dt.date.toEpochDays() == now.date.toEpochDays() + 1 -> "Tomorrow"
        dt.date.toEpochDays() == now.date.toEpochDays() - 1 -> "Yesterday"
        else -> "${dt.date.dayOfMonth} ${
            dt.date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        }"
    }

    val hour = dt.hour
    val minute = dt.minute
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val minuteStr = minute.toString().padStart(2, '0')
    return "$dayLabel, $displayHour:$minuteStr $amPm"
}

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggleComplete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val quadrantColor = task.quadrant.color()

    // Card background: abu-abu redup untuk completed, putih untuk aktif
    val cardBackground by animateColorAsState(
        targetValue = if (task.isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(250),
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (task.isCompleted)
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // ── Baris 1: Badge Eisenhower ────────────────────────────────
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(quadrantColor.copy(alpha = if (task.isCompleted) 0.07f else 0.13f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = task.quadrant.description.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = quadrantColor.copy(alpha = if (task.isCompleted) 0.6f else 1f),
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Baris 2: Checkbox + Judul ────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tombol complete — selalu biru (primary) saat checked
                IconButton(
                    onClick = { onToggleComplete(task.id) },
                    modifier = Modifier.size(28.dp)
                ) {
                    val checkColor by animateColorAsState(
                        targetValue = if (task.isCompleted)
                            MaterialTheme.colorScheme.primary      // biru
                        else
                            MaterialTheme.colorScheme.outline,
                        animationSpec = tween(200),
                        label = "checkColor"
                    )
                    Icon(
                        imageVector = if (task.isCompleted)
                            Icons.Outlined.CheckCircle
                        else
                            Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (task.isCompleted) "Tandai belum selesai" else "Tandai selesai",
                        tint = checkColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Baris 3: Chip tipe kuadran + deadline / label selesai ────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task.isCompleted) {
                    // Label "Selesai" menggantikan semua chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Selesai",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Chip tipe kuadran (Do First, Schedule, dll.)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(quadrantColor.copy(alpha = 0.10f))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        QuadrantDot(quadrant = task.quadrant, size = 6.dp)
                        Text(
                            text = task.quadrant.action,
                            style = MaterialTheme.typography.labelSmall,
                            color = quadrantColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Chip deadline (jika ada)
                    if (task.deadline != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = formatDeadline(task.deadline),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Chip deskripsi/kategori (jika diisi)
                    if (task.description.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== LOADING INDICATOR ====================

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 2.dp
        )
    }
}

// ==================== EMPTY STATE ====================

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

// ==================== STAT CARD ====================

@Composable
fun StatCard(
    label: String,
    value: String,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
