package com.example.smarthomedemo2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Rounded.Home)
    object Camera : Screen("camera", "Access", Icons.Rounded.Videocam)
    object Voice : Screen("voice", "Voice", Icons.Rounded.Mic)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
    object ActivityLog : Screen("activity_log", "Activity Log", Icons.Rounded.History)
}

val navItems = listOf(
    Screen.Dashboard,
    Screen.Camera,
    Screen.Voice,
    Screen.Settings
)
