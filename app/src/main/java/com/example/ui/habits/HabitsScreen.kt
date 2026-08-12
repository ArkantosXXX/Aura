package com.example.ui.habits

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppCard
import com.example.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel,
    onOpenQuickAdd: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alışkanlıklar & Seriler", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.items.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Autorenew,
                    title = "Alışkanlık Bulunmuyor",
                    description = "Her gün tekrarlamak istediğin rutinleri oluşturarak serilerini başlat.",
                    actionLabel = "Yeni Alışkanlık Ekle",
                    onAction = onOpenQuickAdd
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.items,
                        key = { it.habit.id }
                    ) { item ->
                        HabitCard(
                            item = item,
                            onToggleToday = { viewModel.toggleHabitToday(item.habit.id) },
                            onDelete = { viewModel.deleteHabit(item.habit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitCard(
    item: HabitItemUiState,
    onToggleToday: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.streakResult.currentStreak} Gün Seri (En iyi: ${item.streakResult.longestStreak})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = item.isCompletedToday,
                    onClick = onToggleToday,
                    label = { Text(if (item.isCompletedToday) "Bugün Tamamlandı" else "Tamamla") },
                    leadingIcon = {
                        Icon(
                            if (item.isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.testTag("habit_chip_${item.habit.id}")
                )

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
