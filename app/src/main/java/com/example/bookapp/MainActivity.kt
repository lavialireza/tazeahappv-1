package com.example.bookapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.bookapp.data.AppDatabase
import com.example.bookapp.data.Prefs
import com.example.bookapp.data.syncRemoteContent
import com.example.bookapp.ui.AppNavigation
import com.example.bookapp.ui.theme.colorSchemeFor
import com.example.bookapp.ui.theme.typographyFor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutTarget = intent?.getStringExtra("shortcut_target")
        val db = AppDatabase.getInstance(this)

        setContent {
            var darkMode by remember { mutableStateOf(Prefs.isDarkMode(this)) }
            var fontScale by remember { mutableFloatStateOf(Prefs.getFontScale(this)) }
            var themeChoice by remember { mutableStateOf(Prefs.getThemeChoice(this)) }
            var fontChoice by remember { mutableStateOf(Prefs.getFontChoice(this)) }

            val colorScheme = colorSchemeFor(themeChoice, darkMode)
            val typography = typographyFor(fontChoice)

            val onSyncContent: suspend () -> Result<Unit> = {
                syncRemoteContent(db)
            }

            MaterialTheme(colorScheme = colorScheme, typography = typography) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        darkMode = darkMode,
                        onDarkModeChange = {
                            darkMode = it
                            Prefs.setDarkMode(this, it)
                        },
                        fontScale = fontScale,
                        onFontScaleChange = {
                            fontScale = it
                            Prefs.setFontScale(this, it)
                        },
                        fontChoice = fontChoice,
                        onFontChoiceChange = {
                            fontChoice = it
                            Prefs.setFontChoice(this, it)
                        },
                        themeChoice = themeChoice,
                        onThemeChoiceChange = {
                            themeChoice = it
                            Prefs.setThemeChoice(this, it)
                        },
                        db = db,
                        onSyncContent = onSyncContent,
                        shortcutTarget = shortcutTarget
                    )
                }
            }
        }
    }
}