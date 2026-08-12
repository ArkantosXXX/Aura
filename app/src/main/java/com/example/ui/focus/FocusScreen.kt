package com.example.ui.focus

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    viewModel: FocusViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val mins = uiState.remainingSeconds / 60
    val secs = uiState.remainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Odaklanma (Pomodoro)", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Preset selection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = uiState.workDurationMins == 25,
                    onClick = { viewModel.setPreset(25, 5) },
                    label = { Text("25/5 dk") }
                )
                FilterChip(
                    selected = uiState.workDurationMins == 50,
                    onClick = { viewModel.setPreset(50, 10) },
                    label = { Text("50/10 dk") }
                )
                FilterChip(
                    selected = uiState.workDurationMins == 90,
                    onClick = { viewModel.setPreset(90, 15) },
                    label = { Text("90/15 dk") }
                )
            }

            // Big Timer Display
            Surface(
                shape = CircleShape,
                color = if (uiState.isBreak) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(260.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (uiState.isBreak) "Mola Zamanı" else "Odaklanma Zamanı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isBreak) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formattedTime,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (uiState.isBreak) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Task linking & Control buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Bugün Toplam Odaklanma Süresi: ${uiState.totalFocusMinsToday} dakika",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sıfırla", modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Button(
                        onClick = {
                            if (uiState.isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                        },
                        modifier = Modifier
                            .height(64.dp)
                            .width(160.dp)
                            .testTag("focus_start_pause_btn"),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isRunning) "Duraklat" else "Başlat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
