package com.example.bookapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bookapp.data.BookDatabase
import com.example.bookapp.data.JsonImporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    themeChoice: Int,
    onThemeChoiceChange: (Int) -> Unit,
    fontChoice: Int,
    onFontChoiceChange: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importStatus by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    
    // Launcher برای انتخاب فایل JSON
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
                    
                    val database = BookDatabase.getInstance(context)
                    val importer = JsonImporter(database)
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========== کارت واردات JSON (جدید) ==========
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
            
            // ========== تنظیمات موجود (حالت شب، فونت و...) ==========
            // ... کدهای قبلی تنظیمات را اینجا کپی کنید ...
            
            // برای اختصار، فقط یک نمونه از تنظیمات را می‌نویسم:
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🌙 حالت شب",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("فعال کردن حالت شب")
                        Switch(
                            checked = darkMode,
                            onCheckedChange = onDarkModeChange
                        )
                    }
                }
            }
            
            // دکمه پاک کردن دیتابیس (برای تست)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ ابزارهای پیشرفته",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val database = BookDatabase.getInstance(context)
                                    database.clearAllTables()
                                    Toast.makeText(context, "✅ دیتابیس پاک شد!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "❌ خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("پاک کردن تمام داده‌ها")
                    }
                }
            }
        }
    }
}
