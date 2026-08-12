package com.example.ui.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.TaskEntity
import com.example.domain.model.TaskPriority
import com.example.domain.model.TaskStatus
import com.example.ui.components.AppCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PriorityBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    onOpenQuickAdd: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Görevler", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Görevlerde ara...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("tasks_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category & Priority Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategoryFilter == null && uiState.selectedPriorityFilter == null,
                        onClick = {
                            viewModel.setCategoryFilter(null)
                            viewModel.setPriorityFilter(null)
                        },
                        label = { Text("Tümü") }
                    )
                }
                items(uiState.categories) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategoryFilter == cat.id,
                        onClick = {
                            if (uiState.selectedCategoryFilter == cat.id) {
                                viewModel.setCategoryFilter(null)
                            } else {
                                viewModel.setCategoryFilter(cat.id)
                            }
                        },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.tasks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.TaskAlt,
                    title = "Görev Bulunmuyor",
                    description = "Aramanıza veya filtrenize uygun görev bulunamadı.",
                    actionLabel = "Hızlı Görev Ekle",
                    onAction = onOpenQuickAdd
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.tasks,
                        key = { it.id }
                    ) { task ->
                        TaskItemCard(
                            task = task,
                            onToggle = { comp -> viewModel.toggleTask(task.id, comp) },
                            onDefer = { viewModel.deferTaskToTomorrow(task.id) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggle: (Boolean) -> Unit,
    onDefer: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard(
        backgroundColor = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (task.desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityBadge(priority = task.priority)

                    if (task.date.isNotBlank()) {
                        Text(
                            text = "${task.date} ${task.startTime}".trim(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.status == TaskStatus.OVERDUE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (task.isDeferred) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Ertelendi",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDefer) {
                Icon(
                    imageVector = Icons.Default.Update,
                    contentDescription = "Yarına Ertele",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
