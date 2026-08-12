package com.example.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evrensel Arama", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.setQuery(it) },
                placeholder = { Text("Görev, Not, Hedef veya Alışkanlık ara...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("global_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "Tümü",
                    "TASKS" to "Görevler",
                    "NOTES" to "Notlar",
                    "HABITS" to "Alışkanlıklar",
                    "GOALS" to "Hedefler"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = uiState.selectedFilter == key,
                        onClick = { viewModel.setFilter(key) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.results.tasks.isNotEmpty()) {
                    item { Text("Görevler (${uiState.results.tasks.size})", fontWeight = FontWeight.Bold) }
                    items(uiState.results.tasks) { task ->
                        AppCard { Text(task.title, fontWeight = FontWeight.Medium) }
                    }
                }

                if (uiState.results.notes.isNotEmpty()) {
                    item { Text("Notlar (${uiState.results.notes.size})", fontWeight = FontWeight.Bold) }
                    items(uiState.results.notes) { note ->
                        AppCard { Text(note.title, fontWeight = FontWeight.Medium) }
                    }
                }

                if (uiState.results.habits.isNotEmpty()) {
                    item { Text("Alışkanlıklar (${uiState.results.habits.size})", fontWeight = FontWeight.Bold) }
                    items(uiState.results.habits) { habit ->
                        AppCard { Text(habit.name, fontWeight = FontWeight.Medium) }
                    }
                }

                if (uiState.results.goals.isNotEmpty()) {
                    item { Text("Hedefler (${uiState.results.goals.size})", fontWeight = FontWeight.Bold) }
                    items(uiState.results.goals) { goal ->
                        AppCard { Text(goal.title, fontWeight = FontWeight.Medium) }
                    }
                }
            }
        }
    }
}
