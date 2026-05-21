package com.learncore.presentation.screens.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
            // Search bar
            val currentQuery = (uiState as? TaskListUiState.Success)?.searchQuery ?: ""
            OutlinedTextField(
                value = currentQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        "Search tasks...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = currentQuery.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Quadrant filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
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

            Spacer(modifier = Modifier.height(4.dp))

            when (val state = uiState) {
                is TaskListUiState.Loading -> LoadingIndicator()

                is TaskListUiState.Success -> {
                    if (state.tasks.isEmpty()) {
                        val emptyMessage = when {
                            state.searchQuery.isNotBlank() -> "No tasks match \"${state.searchQuery}\""
                            else -> "Tap + to add a new task"
                        }
                        EmptyState(
                            title = if (state.searchQuery.isNotBlank()) "No results found" else "No tasks here",
                            message = emptyMessage
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
