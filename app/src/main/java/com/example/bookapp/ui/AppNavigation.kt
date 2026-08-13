package com.example.bookapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookapp.data.AppDatabase
import com.example.bookapp.ui.screens.*

@Composable
fun AppNavigation(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    fontChoice: String,
    onFontChoiceChange: (String) -> Unit,
    themeChoice: String,
    onThemeChoiceChange: (String) -> Unit,
    db: AppDatabase,
    onSyncContent: suspend () -> Result<Unit>,
    shortcutTarget: String?
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (shortcutTarget != null) "main" else "login"
    ) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        composable("main") {
            MainMenuScreen(
                randomVerse = null,
                recentItems = emptyList(),
                onOpenTaziehList = { navController.navigate("fields") },
                onOpenSearch = { navController.navigate("search") },
                onOpenBookmarks = { navController.navigate("bookmarks") },
                onOpenNotes = { navController.navigate("notes") },
                onOpenAbout = { navController.navigate("about") },
                onOpenSettings = { navController.navigate("settings") },
                onOpenVersion = { navController.navigate("version") },
                onItemClick = {}
            )
        }

        composable("fields") {
            GenericListScreen(
                screenTitle = "زمینه‌ها",
                items = emptyList(),
                onItemClick = {},
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                darkMode = darkMode,
                onDarkModeChange = onDarkModeChange,
                fontScale = fontScale,
                onFontScaleChange = onFontScaleChange,
                fontChoice = fontChoice,
                onFontChoiceChange = onFontChoiceChange,
                themeChoice = themeChoice,
                onThemeChoiceChange = onThemeChoiceChange,
                onSyncContent = onSyncContent,
                db = db,
                onBack = { navController.popBackStack() }
            )
        }

        composable("about") {
            AboutScreen(
                fieldsCount = 0,
                taziehsCount = 0,
                rolesCount = 0,
                sectionsCount = 0,
                readCount = 0,
                streakDays = 0,
                onBack = { navController.popBackStack() }
            )
        }

        composable("version") {
            VersionScreen(onBack = { navController.popBackStack() })
        }

        composable("bookmarks") {
            BookmarksScreen(
                items = emptyList(),
                onItemClick = {},
                onBack = { navController.popBackStack() }
            )
        }

        composable("notes") {
            NotesScreen(
                notes = emptyList(),
                onAddNote = { _, _ -> },
                onDeleteNote = {},
                onBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            SearchScreen(
                fields = emptyList(),
                allTaziehs = emptyList(),
                onSearch = { _, _, _ -> emptyList() },
                onResultClick = {},
                onBack = { navController.popBackStack() }
            )
        }
    }
}