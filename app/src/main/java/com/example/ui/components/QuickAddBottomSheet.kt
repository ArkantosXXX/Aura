package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.HabitEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.TaskEntity
import com.example.domain.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    onDismiss: () -> Unit,
    onSaveTask: (TaskEntity) -> Unit,
    onSaveHabit: (HabitEntity) -> Unit,
    onSaveGoal: (GoalEntity) -> Unit,
    onSaveNote: (NoteEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Görev, 1: Alışkanlık, 2: Hedef, 3: Not
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Text(
                text = "Hızlı Ekle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Görev") },
                    icon = { Icon(Icons.Default.TaskAlt, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Alışkanlık") },
                    icon = { Icon(Icons.Default.Autorenew, null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Hedef") },
                    icon = { Icon(Icons.Default.Flag, null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Not") },
                    icon = { Icon(Icons.Default.EditNote, null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> QuickAddTaskForm(onSave = { task ->
                    onSaveTask(task)
                    onDismiss()
                })
                1 -> QuickAddHabitForm(onSave = { habit ->
                    onSaveHabit(habit)
                    onDismiss()
                })
                2 -> QuickAddGoalForm(onSave = { goal ->
                    onSaveGoal(goal)
                    onDismiss()
                })
                3 -> QuickAddNoteForm(onSave = { note ->
                    onSaveNote(note)
                    onDismiss()
                })
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickAddTaskForm(onSave: (TaskEntity) -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = dateFormat.format(Date())

    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayStr) }
    var startTime by remember { mutableStateOf("09:00") }
    var durationMins by remember { mutableStateOf("30") }
    var showAdvanced by remember { mutableStateOf(false) }

    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Görev Başlığı") },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_add_title_input"),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Tarih (YYYY-MM-DD)") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Saat (HH:mm)") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = durationMins,
            onValueChange = { durationMins = it },
            label = { Text("Süre (dk)") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = { showAdvanced = !showAdvanced }) {
        Icon(if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (showAdvanced) "Gelişmiş Seçenekleri Gizle" else "Gelişmiş Seçenekler (Öncelik, Açıklama)")
    }

    if (showAdvanced) {
        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Açıklama / Not") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Öncelik Seviyesi:", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskPriority.entries.forEach { p ->
                FilterChip(
                    selected = priority == p,
                    onClick = { priority = p },
                    label = { Text(p.name) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (title.isNotBlank()) {
                val dur = durationMins.toIntOrNull() ?: 30
                val task = TaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    desc = desc,
                    date = date,
                    startTime = startTime,
                    endTime = calculateEndTime(startTime, dur),
                    estimatedDuration = dur,
                    priority = priority
                )
                onSave(task)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_add_save_task_btn"),
        enabled = title.isNotBlank()
    ) {
        Icon(Icons.Default.Check, null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Görev Kaydet")
    }
}

@Composable
fun QuickAddHabitForm(onSave: (HabitEntity) -> Unit) {
    var name by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("09:00") }

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Alışkanlık Adı") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = time,
        onValueChange = { time = it },
        label = { Text("Hatırlatma Saati") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (name.isNotBlank()) {
                onSave(
                    HabitEntity(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        reminderTime = time
                    )
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = name.isNotBlank()
    ) {
        Text("Alışkanlık Oluştur")
    }
}

@Composable
fun QuickAddGoalForm(onSave: (GoalEntity) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Hedef Başlığı") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = desc,
        onValueChange = { desc = it },
        label = { Text("Hedef Açıklaması") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (title.isNotBlank()) {
                onSave(
                    GoalEntity(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = desc
                    )
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = title.isNotBlank()
    ) {
        Text("Hedef Oluştur")
    }
}

@Composable
fun QuickAddNoteForm(onSave: (NoteEntity) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Not Başlığı") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = content,
        onValueChange = { content = it },
        label = { Text("Not İçeriği (Markdown destekli)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (title.isNotBlank() || content.isNotBlank()) {
                onSave(
                    NoteEntity(
                        id = UUID.randomUUID().toString(),
                        title = if (title.isBlank()) "Başlıksız Not" else title,
                        content = content
                    )
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = title.isNotBlank() || content.isNotBlank()
    ) {
        Text("Notu Kaydet")
    }
}

private fun calculateEndTime(start: String, durationMins: Int): String {
    return try {
        val parts = start.split(":")
        val startMins = parts[0].toInt() * 60 + parts[1].toInt()
        val endMins = startMins + durationMins
        val hrs = (endMins / 60) % 24
        val mins = endMins % 60
        String.format(Locale.getDefault(), "%02d:%02d", hrs, mins)
    } catch (e: Exception) {
        "09:30"
    }
}
