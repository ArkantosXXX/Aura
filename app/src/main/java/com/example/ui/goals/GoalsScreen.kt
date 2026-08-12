package com.example.ui.goals

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.MilestoneEntity
import com.example.ui.components.AppCard
import com.example.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    onOpenQuickAdd: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedGoalIdForMilestone by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hedefler & Kilometre Taşları", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.goalsWithDetails.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Flag,
                    title = "Henüz Hedef Yok",
                    description = "Büyük hedeflerini parçalara bölerek adım adım başarabilirsin.",
                    actionLabel = "Yeni Hedef Oluştur",
                    onAction = onOpenQuickAdd
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.goalsWithDetails,
                        key = { it.goal.id }
                    ) { detail ->
                        GoalCard(
                            detail = detail,
                            onToggleMilestone = { viewModel.toggleMilestone(it) },
                            onAddMilestoneClick = { selectedGoalIdForMilestone = detail.goal.id },
                            onDelete = { viewModel.deleteGoal(detail.goal.id) }
                        )
                    }
                }
            }
        }
    }

    if (selectedGoalIdForMilestone != null) {
        AddMilestoneDialog(
            onDismiss = { selectedGoalIdForMilestone = null },
            onAdd = { title ->
                viewModel.addMilestone(selectedGoalIdForMilestone!!, title)
                selectedGoalIdForMilestone = null
            }
        )
    }
}

@Composable
fun GoalCard(
    detail: GoalWithDetails,
    onToggleMilestone: (MilestoneEntity) -> Unit,
    onAddMilestoneClick: () -> Unit,
    onDelete: () -> Unit
) {
    val goal = detail.goal
    val percent = (goal.progress * 100).toInt()

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (goal.description.isNotBlank()) {
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tamamlanma: %$percent",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${detail.milestones.count { it.isCompleted }}/${detail.milestones.size} Aşama",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { goal.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Kilometre Taşları (Aşamalar)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        detail.milestones.forEach { milestone ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = milestone.isCompleted,
                    onCheckedChange = { onToggleMilestone(milestone) }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        TextButton(
            onClick = onAddMilestoneClick,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Aşama Ekle")
        }
    }
}

@Composable
fun AddMilestoneDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Aşama (Milestone) Ekle") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Aşama Başlığı") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title) },
                enabled = title.isNotBlank()
            ) { Text("Ekle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
