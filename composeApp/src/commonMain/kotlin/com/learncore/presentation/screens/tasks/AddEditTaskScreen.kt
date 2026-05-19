package com.learncore.presentation.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.presentation.components.color
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
                        text = if (uiState.isEditMode) "Edit Task" else "New Task",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Task Title") },
                placeholder = { Text("What needs to be done?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null && uiState.title.isBlank(),
                shape = RoundedCornerShape(12.dp)
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optional)") },
                placeholder = { Text("Add details...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            // Quadrant selector
            Text(
                text = "Eisenhower Quadrant",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1: DO_FIRST, SCHEDULE
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
                // Row 2: DELEGATE, ELIMINATE
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

            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::saveTask,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (uiState.isEditMode) "Update Task" else "Create Task")
            }
        }
    }
}

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
            containerColor = if (isSelected) {
                accentColor.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = quadrant.action,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = quadrant.description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) {
                    accentColor.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
