package com.learncore.presentation.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.learncore.presentation.components.EmptyState
import com.learncore.presentation.components.LoadingIndicator
import com.learncore.presentation.components.TaskCard
import com.learncore.presentation.components.color
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    quadrantFilter: String?,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: (String?) -> Unit,
    viewModel: TaskListViewModel = koinViewModel()
) {
    LaunchedEffect(quadrantFilter) {
        val quadrant = quadrantFilter?.let { EisenhowerQuadrant.fromString(it) }
        viewModel.setQuadrantFilter(quadrant)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val selectedQuadrant = (uiState as? TaskListUiState.Success)
                        ?.selectedQuadrant?.name
                    onNavigateToAdd(selectedQuadrant)
                }
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Quadrant filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = (uiState as? TaskListUiState.Success)?.selectedQuadrant == null,
                        onClick = { viewModel.setQuadrantFilter(null) },
                        label = { Text("All") }
                    )
                }
                items(EisenhowerQuadrant.entries) { quadrant ->
                    val selected =
                        (uiState as? TaskListUiState.Success)?.selectedQuadrant == quadrant
                    FilterChip(
                        selected = selected,
                        onClick = {
                            viewModel.setQuadrantFilter(
                                if (selected) null else quadrant
                            )
                        },
                        label = { Text(quadrant.action) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = quadrant.color().copy(alpha = 0.15f),
                            selectedLabelColor = quadrant.color()
                        )
                    )
                }
            }

            when (val state = uiState) {
                is TaskListUiState.Loading -> LoadingIndicator()

                is TaskListUiState.Success -> {
                    if (state.tasks.isEmpty()) {
                        EmptyState(
                            title = "No tasks here",
                            message = "Tap + to add a new task"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 88.dp,
                                top = 4.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = state.tasks,
                                key = { it.id }
                            ) { task ->
                                TaskCard(
                                    task = task,
                                    onClick = { onNavigateToDetail(task.id) },
                                    onToggleComplete = { viewModel.toggleCompletion(it) }
                                )
                            }
                        }
                    }
                }

                is TaskListUiState.Error -> EmptyState(
                    title = "Something went wrong",
                    message = state.message
                )
            }
        }
    }
}
