package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.AuraApplication
import com.example.ui.admin.AdminScreen
import com.example.ui.admin.AdminViewModel
import com.example.ui.calendar.CalendarScreen
import com.example.ui.calendar.CalendarViewModel
import com.example.ui.components.QuickAddBottomSheet
import com.example.ui.focus.FocusScreen
import com.example.ui.focus.FocusViewModel
import com.example.ui.goals.GoalsScreen
import com.example.ui.goals.GoalsViewModel
import com.example.ui.habits.HabitsScreen
import com.example.ui.habits.HabitsViewModel
import com.example.ui.notes.NotesScreen
import com.example.ui.notes.NotesViewModel
import com.example.ui.premium.PremiumScreen
import com.example.ui.premium.PremiumViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.ProfileViewModel
import com.example.ui.search.SearchScreen
import com.example.ui.search.SearchViewModel
import com.example.ui.tasks.TasksScreen
import com.example.ui.tasks.TasksViewModel
import com.example.ui.today.TodayDashboardScreen
import com.example.ui.today.TodayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current.applicationContext as AuraApplication
    val container = context.container

    val navController = rememberNavController()

    // ViewModel instances created using AppContainer
    val todayViewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory(container))
    val tasksViewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory(container))
    val calendarViewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(container))
    val goalsViewModel: GoalsViewModel = viewModel(factory = GoalsViewModel.Factory(container))
    val habitsViewModel: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory(container))
    val focusViewModel: FocusViewModel = viewModel(factory = FocusViewModel.Factory(container))
    val notesViewModel: NotesViewModel = viewModel(factory = NotesViewModel.Factory(container))
    val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory(container))
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(container))
    val premiumViewModel: PremiumViewModel = viewModel(factory = PremiumViewModel.Factory(container))
    val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(container))

    var showQuickAddSheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Today.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aura Plan",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.Search.route) },
                        modifier = Modifier.testTag("nav_search_btn")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Arama")
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.Notes.route) },
                        modifier = Modifier.testTag("nav_notes_btn")
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = "Notlar")
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.Focus.route) },
                        modifier = Modifier.testTag("nav_focus_btn")
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = "Odaklanma")
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.Premium.route) },
                        modifier = Modifier.testTag("nav_premium_btn")
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = "Aura Premium",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.Profile.route) },
                        modifier = Modifier.testTag("nav_profile_btn")
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profil & Ayarlar")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("bottom_nav_${screen.route}")
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("global_quick_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Hızlı Ekle")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Today.route) {
                TodayDashboardScreen(
                    viewModel = todayViewModel,
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToHabits = { navController.navigate(Screen.Habits.route) },
                    onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                    onNavigateToFocus = { navController.navigate(Screen.Focus.route) }
                )
            }
            composable(Screen.Tasks.route) {
                TasksScreen(
                    viewModel = tasksViewModel,
                    onOpenQuickAdd = { showQuickAddSheet = true }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = calendarViewModel
                )
            }
            composable(Screen.Goals.route) {
                GoalsScreen(
                    viewModel = goalsViewModel,
                    onOpenQuickAdd = { showQuickAddSheet = true }
                )
            }
            composable(Screen.Habits.route) {
                HabitsScreen(
                    viewModel = habitsViewModel,
                    onOpenQuickAdd = { showQuickAddSheet = true }
                )
            }
            composable(Screen.Focus.route) {
                FocusScreen(
                    viewModel = focusViewModel
                )
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    viewModel = notesViewModel,
                    onOpenQuickAdd = { showQuickAddSheet = true }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToPremium = { navController.navigate(Screen.Premium.route) },
                    onNavigateToAdmin = { navController.navigate(Screen.Admin.route) }
                )
            }
            composable(Screen.Premium.route) {
                PremiumScreen(
                    viewModel = premiumViewModel
                )
            }
            composable(Screen.Admin.route) {
                AdminScreen(
                    viewModel = adminViewModel
                )
            }
        }
    }

    if (showQuickAddSheet) {
        QuickAddBottomSheet(
            onDismiss = { showQuickAddSheet = false },
            onSaveTask = { task -> tasksViewModel.saveTask(task) },
            onSaveHabit = { habit -> habitsViewModel.saveHabit(habit) },
            onSaveGoal = { goal -> goalsViewModel.saveGoal(goal) },
            onSaveNote = { note -> notesViewModel.saveNote(note) }
        )
    }
}
