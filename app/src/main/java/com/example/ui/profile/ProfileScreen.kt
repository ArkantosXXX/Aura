package com.example.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.domain.model.ThemeOption
import com.example.ui.components.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToPremium: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil & Ayarlar", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // User avatar & Info
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(36.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Aura Kullanıcısı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Çevrimdışı / Yerel Profil", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium & Admin Quick Links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToPremium,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.WorkspacePremium, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Premium")
                }

                OutlinedButton(
                    onClick = onNavigateToAdmin,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Admin")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Theme Settings
            Text("Tema Seçimi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = uiState.settings.theme == ThemeOption.SYSTEM,
                    onClick = { viewModel.setTheme(ThemeOption.SYSTEM) },
                    label = { Text("Sistem") }
                )
                FilterChip(
                    selected = uiState.settings.theme == ThemeOption.LIGHT,
                    onClick = { viewModel.setTheme(ThemeOption.LIGHT) },
                    label = { Text("Açık") }
                )
                FilterChip(
                    selected = uiState.settings.theme == ThemeOption.DARK,
                    onClick = { viewModel.setTheme(ThemeOption.DARK) },
                    label = { Text("Koyu") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Working Hours Configuration
            Text("Çalışma Saatleri (Planlama Motoru İçin)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Çalışma Aralığı:", style = MaterialTheme.typography.bodyMedium)
                    Text("${uiState.settings.workStart} - ${uiState.settings.workEnd}", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data Management
            Text("Veri Yönetimi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* Export */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tüm Verileri Dışa Aktar (JSON)")
            }
        }
    }
}
