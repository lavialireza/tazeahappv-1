package com.example.bookapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bookapp.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    fieldsCount: Int,
    taziehsCount: Int,
    rolesCount: Int,
    sectionsCount: Int,
    readCount: Int,
    streakDays: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("درباره برنامه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "این اپلیکیشن یک کتابخانه دیجیتال از متون تعزیه است که بر اساس " +
                        "زمینه، تعزیه، نقش و بخش دسته‌بندی شده است."
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "آمار مجموعه:",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$fieldsCount زمینه")
            Text(text = "$taziehsCount تعزیه")
            Text(text = "$rolesCount نقش")
            Text(text = "$sectionsCount بخش")

            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "آمار مطالعه شما:",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$readCount بخش را تا الان خوانده‌اید")
            if (streakDays > 1) {
                Text(text = "$streakDays روز متوالی سر زده‌اید 🔥")
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            android.content.Intent.EXTRA_TEXT,
                            "این اپ رو ببین: «تعزیه و شبیه‌خوانی» — کتابخانه‌ای کامل و آفلاین از نسخه‌های تعزیه.\nhttps://github.com/lavialireza/taziehapp"
                        )
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "معرفی اپ به دیگران"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "معرفی این اپ به دیگران")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ورژن برنامه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "نسخه برنامه: ${BuildConfig.VERSION_NAME}")
        }
    }
}
