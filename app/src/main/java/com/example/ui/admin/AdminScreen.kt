package com.example.ui.admin

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Flags, 1: Kill Switch, 2: Announcements, 3: Audit Logs, 4: Errors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yönetici (Admin) Paneli", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Feature Flags") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Acil Durum (Kill)") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Duyurular") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Audit Log") })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Hata Raporları") })
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> FeatureFlagsView(uiState = uiState, onToggle = { k, e -> viewModel.toggleFeatureFlag(k, e) })
                1 -> EmergencyKillSwitchView(uiState = uiState, onToggleKill = { viewModel.toggleEmergencyKillSwitch(it) })
                2 -> AnnouncementsAdminView(uiState = uiState, onAdd = { t, m -> viewModel.addAnnouncement(t, m) })
                3 -> AuditLogsView(uiState = uiState)
                4 -> ErrorReportsView(uiState = uiState)
            }
        }
    }
}

@Composable
fun FeatureFlagsView(uiState: AdminUiState, onToggle: (String, Boolean) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(uiState.featureFlags) { flag ->
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = flag.key, fontWeight = FontWeight.Bold)
                        Text(text = "Feature Flag anahtarı", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = flag.value,
                        onCheckedChange = { onToggle(flag.key, it) },
                        modifier = Modifier.testTag("admin_flag_${flag.key}")
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyKillSwitchView(uiState: AdminUiState, onToggleKill: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Emergency Kill Switch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Kritik sistem hatası durumunda senkronizasyon ve veri işleme motorunu anında durdurur.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(modifier = Modifier.height(16.dp))
                Switch(
                    checked = uiState.isEmergencyKillActive,
                    onCheckedChange = onToggleKill,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.error
                    )
                )
            }
        }
    }
}

@Composable
fun AnnouncementsAdminView(uiState: AdminUiState, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Yeni Sistem Duyurusu Ekle", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Duyuru Başlığı") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = msg, onValueChange = { msg = it }, label = { Text("Duyuru Mesajı") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                if (title.isNotBlank() && msg.isNotBlank()) {
                    onAdd(title, msg)
                    title = ""
                    msg = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Duyuruyu Yayınla")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Yayınlanan Duyurular", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        uiState.announcements.forEach { ann ->
            AppCard(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(ann.title, fontWeight = FontWeight.Bold)
                Text(ann.message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AuditLogsView(uiState: AdminUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.auditLogs) { log ->
            AppCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(log.action, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(log.adminId, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(log.details, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ErrorReportsView(uiState: AdminUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.errorReports) { report ->
            AppCard {
                Text(report.error, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("Ekran: ${report.screen} | Cihaz: ${report.deviceInfo}", style = MaterialTheme.typography.labelSmall)
                Text(report.stackTrace, style = MaterialTheme.typography.bodySmall, maxLines = 3)
            }
        }
    }
}
