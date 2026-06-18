package com.rustyn.sentinel.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Rules : Screen("rules", "Rules")
    object History : Screen("history", "History")
    object Suggestions : Screen("suggestions", "Suggestions")
    object Settings : Screen("settings", "Settings")
}
