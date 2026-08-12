package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Today : Screen("today", "Bugün", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Tasks : Screen("tasks", "Görevler", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    object Calendar : Screen("calendar", "Takvim", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Goals : Screen("goals", "Hedefler", Icons.Filled.Flag, Icons.Outlined.Flag)
    object Habits : Screen("habits", "Alışkanlıklar", Icons.Filled.Autorenew, Icons.Outlined.Autorenew)
    object Focus : Screen("focus", "Odak (Pomodoro)", Icons.Filled.Timer, Icons.Outlined.Timer)
    object Notes : Screen("notes", "Notlar", Icons.Filled.EditNote, Icons.Outlined.EditNote)
    object Search : Screen("search", "Arama", Icons.Filled.Search, Icons.Outlined.Search)
    object Profile : Screen("profile", "Profil & Ayarlar", Icons.Filled.Person, Icons.Outlined.Person)
    object Premium : Screen("premium", "Aura Premium", Icons.Filled.WorkspacePremium, Icons.Outlined.WorkspacePremium)
    object Admin : Screen("admin", "Yönetici Paneli", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

val bottomNavItems = listOf(
    Screen.Today,
    Screen.Tasks,
    Screen.Calendar,
    Screen.Goals,
    Screen.Habits
)
