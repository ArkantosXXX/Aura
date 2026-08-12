package com.example.ui.today

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TaskEntity
import com.example.domain.model.DashboardCardType
import com.example.domain.model.DashboardTemplate
import com.example.ui.components.AppCard
import com.example.ui.components.PriorityBadge
import com.example.ui.components.SyncStatusBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayDashboardScreen(
    viewModel: TodayViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToFocus: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTemplateDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val columnCount = if (configuration.screenWidthDp >= 600) 2 else 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Aura Plan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.dayNameStr,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showTemplateDialog = true },
                        modifier = Modifier.testTag("dashboard_template_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DashboardCustomize,
                            contentDescription = "Düzeni Özelleştir"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SyncStatusBanner(syncState = uiState.syncState)

            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.cardOrder.forEach { cardType ->
                    when (cardType) {
                        DashboardCardType.WELCOME -> {
                            item(span = { GridItemSpan(columnCount) }) {
                                WelcomeCard(
                                    greeting = uiState.greetingStr,
                                    dateStr = uiState.dayNameStr
                                )
                            }
                        }
                        DashboardCardType.PROGRESS -> {
                            item(span = { GridItemSpan(columnCount) }) {
                                val completedCount = uiState.tasks.count { it.isCompleted }
                                val totalCount = uiState.tasks.size
                                ProgressSummaryCard(
                                    completed = completedCount,
                                    total = totalCount
                                )
                            }
                        }
                        DashboardCardType.TASKS -> {
                            item {
                                TasksSummaryCard(
                                    tasks = uiState.tasks,
                                    onToggleTask = { id, comp -> viewModel.toggleTask(id, comp) },
                                    onSeeAll = onNavigateToTasks
                                )
                            }
                        }
                        DashboardCardType.TIMELINE -> {
                            item {
                                TimelineCard(
                                    tasks = uiState.tasks,
                                    onSeeAll = onNavigateToTasks
                                )
                            }
                        }
                        DashboardCardType.HABITS -> {
                            item {
                                HabitsSummaryCard(
                                    habits = uiState.habits,
                                    logs = uiState.habitLogs,
                                    onToggleHabit = { viewModel.toggleHabit(it) },
                                    onSeeAll = onNavigateToHabits
                                )
                            }
                        }
                        DashboardCardType.GOALS -> {
                            item {
                                GoalsSummaryCard(
                                    goals = uiState.goals,
                                    onSeeAll = onNavigateToGoals
                                )
                            }
                        }
                        DashboardCardType.FOCUS -> {
                            item {
                                FocusSummaryCard(
                                    sessions = uiState.focusSessions,
                                    onStartFocus = onNavigateToFocus
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    if (showTemplateDialog) {
        TemplateSelectionDialog(
            currentTemplate = uiState.template,
            onSelectTemplate = {
                viewModel.applyTemplate(it)
                showTemplateDialog = false
            },
            onDismiss = { showTemplateDialog = false }
        )
    }
}

@Composable
fun WelcomeCard(greeting: String, dateStr: String) {
    AppCard(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bugün senin için optimize edilmiş deterministic plan hazır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressSummaryCard(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    val percent = (progress * 100).toInt()

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Günün İlerlemesi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (total > 0) "$total görevden $completed tanesi tamamlandı (%$percent)" else "Henüz bugün için görev yok.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun TasksSummaryCard(
    tasks: List<TaskEntity>,
    onToggleTask: (String, Boolean) -> Unit,
    onSeeAll: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bugünün Görevleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAll) {
                Text("Tümünü Gör")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            Text(
                text = "Bugün için kayıtlı görev bulunmuyor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            tasks.take(4).forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleTask(task.id, it) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        if (task.startTime.isNotBlank()) {
                            Text(
                                text = "${task.startTime} - ${task.endTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    PriorityBadge(priority = task.priority)
                }
            }
        }
    }
}

@Composable
fun TimelineCard(tasks: List<TaskEntity>, onSeeAll: () -> Unit) {
    val scheduledTasks = tasks.filter { it.startTime.isNotBlank() }.sortedBy { it.startTime }

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zaman Çizelgesi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAll) {
                Text("Detay")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (scheduledTasks.isEmpty()) {
            Text(
                text = "Zamanlanmış program bulunmuyor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            scheduledTasks.take(3).forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = task.startTime,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun HabitsSummaryCard(
    habits: List<com.example.data.local.entities.HabitEntity>,
    logs: List<com.example.data.local.entities.HabitLogEntity>,
    onToggleHabit: (String) -> Unit,
    onSeeAll: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Alışkanlık Takibi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAll) {
                Text("Tümünü Gör")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (habits.isEmpty()) {
            Text(
                text = "Henüz alışkanlık eklenmedi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            habits.take(3).forEach { habit ->
                val isDone = logs.any { it.habitId == habit.id && it.isCompleted }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    FilterChip(
                        selected = isDone,
                        onClick = { onToggleHabit(habit.id) },
                        label = { Text(if (isDone) "Tamamlandı" else "Tamamla") },
                        leadingIcon = {
                            Icon(
                                if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GoalsSummaryCard(
    goals: List<com.example.data.local.entities.GoalEntity>,
    onSeeAll: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hedef İlerlemesi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAll) {
                Text("Detaylar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (goals.isEmpty()) {
            Text(
                text = "Henüz aktif hedef bulunmuyor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            goals.take(2).forEach { goal ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "%${(goal.progress * 100).toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { goal.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun FocusSummaryCard(
    sessions: List<com.example.data.local.entities.FocusSessionEntity>,
    onStartFocus: () -> Unit
) {
    val totalMins = sessions.sumOf { it.duration }

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Odaklanma Seansı",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toplam $totalMins dk odaklanıldı",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onStartFocus,
                modifier = Modifier.testTag("dashboard_start_focus_btn")
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Başlat")
            }
        }
    }
}

@Composable
fun TemplateSelectionDialog(
    currentTemplate: DashboardTemplate,
    onSelectTemplate: (DashboardTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dashboard Şablonu Seçin") },
        text = {
            Column {
                Text("Kullanım tarzınıza en uygun düzeni seçin:")
                Spacer(modifier = Modifier.height(12.dp))
                DashboardTemplate.entries.forEach { template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTemplate(template) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTemplate == template,
                            onClick = { onSelectTemplate(template) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (template) {
                                DashboardTemplate.MINIMAL -> "Minimal (Sadece Görevler)"
                                DashboardTemplate.STUDENT -> "Öğrenci (Odak & Ders)"
                                DashboardTemplate.PRODUCTIVITY -> "Verimlilik (Tam Çizelge)"
                                DashboardTemplate.GOAL_FOCUSED -> "Hedef Odaklı"
                                DashboardTemplate.HABIT_FOCUSED -> "Alışkanlık Odaklı"
                                DashboardTemplate.BALANCED -> "Dengeli (Varsayılan)"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (currentTemplate == template) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}
