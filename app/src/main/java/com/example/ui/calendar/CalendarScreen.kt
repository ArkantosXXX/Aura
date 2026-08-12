package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddEventDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takvim & Planlama", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddEventDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Etkinlik Ekle")
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
            // View mode selector (Daily, Weekly, Monthly)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = uiState.viewMode == CalendarViewMode.DAILY,
                    onClick = { viewModel.setViewMode(CalendarViewMode.DAILY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("Günlük")
                }
                SegmentedButton(
                    selected = uiState.viewMode == CalendarViewMode.WEEKLY,
                    onClick = { viewModel.setViewMode(CalendarViewMode.WEEKLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("Haftalık")
                }
                SegmentedButton(
                    selected = uiState.viewMode == CalendarViewMode.MONTHLY,
                    onClick = { viewModel.setViewMode(CalendarViewMode.MONTHLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("Aylık")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (uiState.viewMode) {
                CalendarViewMode.DAILY -> DailyTimelineView(uiState = uiState)
                CalendarViewMode.WEEKLY -> WeeklyView(uiState = uiState, onSelectDate = { viewModel.setSelectedDate(it) })
                CalendarViewMode.MONTHLY -> MonthlyGridView(uiState = uiState, onSelectDate = { viewModel.setSelectedDate(it) })
            }
        }
    }

    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onAdd = { title ->
                val now = System.currentTimeMillis()
                viewModel.addEvent(title, now, now + 3600 * 1000L)
                showAddEventDialog = false
            }
        )
    }
}

@Composable
fun DailyTimelineView(uiState: CalendarUiState) {
    val hours = (7..22).toList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(hours) { hour ->
            val hourStr = String.format(Locale.getDefault(), "%02d:00", hour)
            val matchingTasks = uiState.tasks.filter { it.startTime.startsWith(String.format(Locale.getDefault(), "%02d", hour)) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hourStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(54.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (matchingTasks.isNotEmpty()) {
                        Text(
                            text = matchingTasks.joinToString { it.title },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun WeeklyView(uiState: CalendarUiState, onSelectDate: (String) -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    val days = mutableListOf<String>()
    for (i in 0..6) {
        days.add(dateFormat.format(cal.time))
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Haftalık Karşılaştırma", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { dateStr ->
                val isSelected = dateStr == uiState.selectedDate
                Surface(
                    onClick = { onSelectDate(dateStr) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f).padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dateStr.takeLast(2),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyGridView(uiState: CalendarUiState, onSelectDate: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Aylık Genel Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        AppCard {
            Text(
                text = "Tüm ayı kapsayan genel takvim görünümü. Seçili Tarih: ${uiState.selectedDate}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AddEventDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Etkinlik Ekle") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Etkinlik Başlığı") },
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
