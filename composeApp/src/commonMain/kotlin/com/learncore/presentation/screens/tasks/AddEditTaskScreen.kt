package com.learncore.presentation.screens.tasks

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.presentation.components.color
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: Long?,
    defaultQuadrant: String?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditTaskViewModel = koinViewModel()
) {
    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.loadTask(taskId)
        } else if (defaultQuadrant != null) {
            viewModel.setDefaultQuadrant(EisenhowerQuadrant.fromString(defaultQuadrant))
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Tugas" else "Tugas Baru",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── 1. Judul Tugas ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Judul Tugas")
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = { Text("Apa yang perlu dikerjakan?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error != null && uiState.title.isBlank(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── 2. Kategori ─────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Kategori")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DEFAULT_CATEGORIES.forEach { cat ->
                        val isSelected = !uiState.isCustomCategory && uiState.category == cat
                        CategoryChip(
                            label = cat,
                            isSelected = isSelected,
                            onClick = { viewModel.onCategorySelect(cat) }
                        )
                    }
                    CategoryChip(
                        label = "Lainnya...",
                        isSelected = uiState.isCustomCategory,
                        onClick = { viewModel.onCustomCategoryToggle() }
                    )
                }
                if (uiState.isCustomCategory) {
                    OutlinedTextField(
                        value = uiState.customCategory,
                        onValueChange = viewModel::onCustomCategoryChange,
                        placeholder = { Text("Tulis kategori kustom...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ── 3. Deskripsi ────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Deskripsi (opsional)")
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = { Text("Tambahkan detail tugas...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── 4. Deadline ─────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Deadline (opsional)")
                DeadlinePicker(
                    selectedMillis = uiState.deadlineMillis,
                    onDeadlineSelected = viewModel::onDeadlineChange
                )
            }

            // ── 5. Reminder (hanya tampil jika ada deadline) ────────────
            if (uiState.deadlineMillis != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Ingatkan Saya")
                    ReminderPicker(
                        selectedMinutes = uiState.reminderMinutes,
                        onReminderSelected = viewModel::onReminderChange
                    )
                }
            }

            // ── 6. Kuadran Eisenhower ───────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Kuadran Eisenhower")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuadrantSelectorCard(
                            quadrant = EisenhowerQuadrant.DO_FIRST,
                            isSelected = uiState.quadrant == EisenhowerQuadrant.DO_FIRST,
                            onClick = { viewModel.onQuadrantChange(EisenhowerQuadrant.DO_FIRST) },
                            modifier = Modifier.weight(1f)
                        )
                        QuadrantSelectorCard(
                            quadrant = EisenhowerQuadrant.SCHEDULE,
                            isSelected = uiState.quadrant == EisenhowerQuadrant.SCHEDULE,
                            onClick = { viewModel.onQuadrantChange(EisenhowerQuadrant.SCHEDULE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuadrantSelectorCard(
                            quadrant = EisenhowerQuadrant.DELEGATE,
                            isSelected = uiState.quadrant == EisenhowerQuadrant.DELEGATE,
                            onClick = { viewModel.onQuadrantChange(EisenhowerQuadrant.DELEGATE) },
                            modifier = Modifier.weight(1f)
                        )
                        QuadrantSelectorCard(
                            quadrant = EisenhowerQuadrant.ELIMINATE,
                            isSelected = uiState.quadrant == EisenhowerQuadrant.ELIMINATE,
                            onClick = { viewModel.onQuadrantChange(EisenhowerQuadrant.ELIMINATE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = viewModel::saveTask,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                enabled = !uiState.isLoading
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (uiState.isEditMode) "Simpan Perubahan" else "Buat Tugas")
            }
        }
    }
}

// ── Deadline Picker ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlinePicker(
    selectedMillis: Long?,
    onDeadlineSelected: (Long?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = { showPicker = true },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Outlined.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = if (selectedMillis != null) {
                    val dt = Instant.fromEpochMilliseconds(selectedMillis)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    "%02d/%02d/%d %02d:%02d".format(
                        dt.dayOfMonth, dt.monthNumber, dt.year, dt.hour, dt.minute
                    )
                } else {
                    "Pilih Tanggal & Waktu"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (selectedMillis != null) {
            TextButton(onClick = { onDeadlineSelected(null) }) {
                Text("Hapus", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showPicker) {
        PlatformDateTimePicker(
            initialMillis = selectedMillis ?: System.currentTimeMillis(),
            onConfirm = { millis ->
                onDeadlineSelected(millis)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

// ── Reminder Picker ────────────────────────────────────────────────────────

@Composable
private fun ReminderPicker(
    selectedMinutes: Int?,
    onReminderSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = REMINDER_OPTIONS.find { it.first == selectedMinutes }?.second
        ?: "Tidak ada"

    Box {
        FilledTonalButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text(selectedLabel, style = MaterialTheme.typography.bodyMedium)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            REMINDER_OPTIONS.forEach { (minutes, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onReminderSelected(minutes)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ── Section label ──────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

// ── Category chip ──────────────────────────────────────────────────────────

@Composable
private fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

// ── Quadrant selector card ─────────────────────────────────────────────────

@Composable
private fun QuadrantSelectorCard(
    quadrant: EisenhowerQuadrant,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = quadrant.color()
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = quadrant.action,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = quadrant.description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) accentColor.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
