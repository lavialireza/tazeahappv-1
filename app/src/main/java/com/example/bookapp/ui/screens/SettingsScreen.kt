package com.example.bookapp.ui.screens

import androidx.compose.material3.Divider
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bookapp.data.AppDatabase
import com.example.bookapp.data.JsonImporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    fontChoice: String,
    onFontChoiceChange: (String) -> Unit,
    themeChoice: String,
    onThemeChoiceChange: (String) -> Unit,
    onSyncContent: suspend () -> Result<Unit>,
    db: AppDatabase,
    onBack: () -> Unit
) {
    var syncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var importStatus by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ===== Launcher برای انتخاب فایل JSON =====
    val jsonPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isImporting = true
                importStatus = "⏳ در حال خواندن فایل..."

                try {
                    val jsonString = context.contentResolver.openInputStream(it)
                        ?.bufferedReader()
                        ?.use { reader -> reader.readText() }

                    if (jsonString.isNullOrEmpty()) {
                        importStatus = "❌ فایل خالی است"
                        isImporting = false
                        return@launch
                    }

                    importStatus = "⏳ در حال وارد کردن داده‌ها..."

                    val importer = JsonImporter(db)
                    val result = importer.importJson(jsonString)

                    if (result.success) {
                        importStatus = "✅ واردات موفق! ${result.count} بخش وارد شد."
                        Toast.makeText(context, "✅ محتوای جدید با موفقیت وارد شد!", Toast.LENGTH_LONG).show()
                    } else {
                        importStatus = "❌ خطا: ${result.errorMessage}"
                        Toast.makeText(context, "❌ خطا: ${result.errorMessage}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    importStatus = "❌ خطا: ${e.message}"
                    Toast.makeText(context, "❌ خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isImporting = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== کارت واردات JSON (جدید) =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "📂 واردات محتوا از فایل JSON",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "فایل JSON تولید شده توسط اسکریپت docx_to_json.py را انتخاب کنید.\n" +
                                "داده‌های جدید به دیتابیس اضافه می‌شوند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { jsonPickerLauncher.launch("application/json") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isImporting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("در حال وارد کردن...")
                        } else {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("انتخاب فایل JSON")
                        }
                    }

                    if (importStatus != null) {
                        Text(
                            text = importStatus!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                importStatus!!.contains("✅") -> MaterialTheme.colorScheme.primary
                                importStatus!!.contains("❌") -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            // ===== تنظیمات موجود =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("حالت شب (تیره)", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
            }

            Spacer(Modifier.height(8.dp))

            Text("سایز متن", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FontSizeOption("کوچک", 0.85f, fontScale, onFontScaleChange)
                FontSizeOption("متوسط", 1.0f, fontScale, onFontScaleChange)
                FontSizeOption("بزرگ", 1.3f, fontScale, onFontScaleChange)
                FontSizeOption("خیلی بزرگ", 1.6f, fontScale, onFontScaleChange)
            }

            Spacer(Modifier.height(16.dp))

            Text("تم رنگی", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ThemeOption("طلایی", "default", themeChoice, onThemeChoiceChange)
                ThemeOption("سبز", "green", themeChoice, onThemeChoiceChange)
                ThemeOption("قرمز", "red", themeChoice, onThemeChoiceChange)
            }

            Spacer(Modifier.height(16.dp))

            Text("فونت متن", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                com.example.bookapp.ui.theme.FontChoiceLabels.forEach { (key, label) ->
                    ThemeOption(label, key, fontChoice, onFontChoiceChange)
                }
            }

            Spacer(Modifier.height(24.dp))
           Divider()
            Spacer(Modifier.height(16.dp))

            Text("بروزرسانی محتوا", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "اگر محتوای جدیدی به گیت‌هاب اضافه شده، با این دکمه بدون نیاز به نصب دوباره اپ، محتوا به‌روز می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    syncing = true
                    syncMessage = null
                    scope.launch {
                        val result = onSyncContent()
                        syncing = false
                        syncMessage = if (result.isSuccess) {
                            "محتوا با موفقیت به‌روزرسانی شد ✅"
                        } else {
                            "خطا در بروزرسانی ❌"
                        }
                    }
                },
                enabled = !syncing
            ) {
                Text(if (syncing) "در حال بروزرسانی..." else "بروزرسانی محتوا از اینترنت")
            }
            syncMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FontSizeOption(label: String, value: Float, current: Float, onSelect: (Float) -> Unit) {
    val selected = kotlin.math.abs(current - value) < 0.01f
    FilterChip(
        selected = selected,
        onClick = { onSelect(value) },
        label = { Text(label) }
    )
}

@Composable
private fun ThemeOption(label: String, value: String, current: String, onSelect: (String) -> Unit) {
    FilterChip(
        selected = current == value,
        onClick = { onSelect(value) },
        label = { Text(label) }
    )
}
