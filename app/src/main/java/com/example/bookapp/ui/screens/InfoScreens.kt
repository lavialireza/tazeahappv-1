package com.example.bookapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bookapp.BuildConfig
import com.example.bookapp.data.AppDatabase
import com.example.bookapp.data.Prefs
import com.example.bookapp.data.exportBackup
import kotlinx.coroutines.launch

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "این اپلیکیشن یک کتابخانه دیجیتال از متون تعزیه است که بر اساس " +
                        "زمینه، تعزیه، نقش و بخش دسته‌بندی شده است."
            )
            Spacer(Modifier.height(20.dp))
            Text("آمار مجموعه:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text("$fieldsCount زمینه")
            Text("$taziehsCount تعزیه")
            Text("$rolesCount نقش")
            Text("$sectionsCount بخش")

            Spacer(Modifier.height(20.dp))
            Text("آمار مطالعه شما:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text("$readCount بخش را تا الان خوانده‌اید")
            if (streakDays > 1) {
                Text("$streakDays روز متوالی سر زده‌اید 🔥")
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("مشخصات طراحی و گردآوری", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text("طراح و گردآورنده: [نام خودتان را اینجا بنویسید]")
            Text("نسخه: ${BuildConfig.VERSION_NAME}")
            Text("راه ارتباطی: [ایمیل یا شبکه اجتماعی]")

            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        android.content.Intent.EXTRA_TEXT,
                        "این اپ رو ببین: «تعزیه و شبیه‌خوانی» — کتابخانه‌ای کامل و آفلاین از نسخه‌های تعزیه.\nhttps://github.com/lavialireza/taziehapp"
                    )
                }
                context.startActivity(android.content.Intent.createChooser(intent, "معرفی اپ به دیگران"))
            }) {
                Text("معرفی این اپ به دیگران")
            }
        }
    }
}

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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("حالت شب (تیره)", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
            }

            Spacer(Modifier.height(24.dp))

            Text("سایز متن", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FontSizeOption("کوچک", 0.85f, fontScale, onFontScaleChange)
                FontSizeOption("متوسط", 1.0f, fontScale, onFontScaleChange)
                FontSizeOption("بزرگ", 1.3f, fontScale, onFontScaleChange)
                FontSizeOption("خیلی بزرگ", 1.6f, fontScale, onFontScaleChange)
            }

            Spacer(Modifier.height(24.dp))
            Text("فاصله خطوط متن", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            var lineSpacing by remember { mutableStateOf(Prefs.getLineSpacing(context)) }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LineSpacingOption("فشرده", 1.1f, lineSpacing) { lineSpacing = it; Prefs.setLineSpacing(context, it) }
                LineSpacingOption("معمولی", 1.4f, lineSpacing) { lineSpacing = it; Prefs.setLineSpacing(context, it) }
                LineSpacingOption("بازتر", 1.8f, lineSpacing) { lineSpacing = it; Prefs.setLineSpacing(context, it) }
            }
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    "این یک متن نمونه است تا فاصله‌ی خطوط را همین‌جا ببینید.\nخط دوم نمونه برای مقایسه فاصله با خط بالا.",
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.fontSize * lineSpacing),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("تم رنگی", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOption("طلایی", "default", themeChoice, onThemeChoiceChange, androidx.compose.ui.graphics.Color(0xFFD4A94A))
                ThemeOption("سبز", "green", themeChoice, onThemeChoiceChange, androidx.compose.ui.graphics.Color(0xFF3E8E5A))
                ThemeOption("قرمز", "red", themeChoice, onThemeChoiceChange, androidx.compose.ui.graphics.Color(0xFFA33B3B))
            }

            Spacer(Modifier.height(24.dp))
            Text("فونت متن", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.bookapp.ui.theme.FontChoiceLabels.forEach { (key, label) ->
                    ThemeOption(label, key, fontChoice, onFontChoiceChange)
                }
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("بروزرسانی محتوا", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "اگر محتوای جدیدی به گیت‌هاب اضافه شده، با این دکمه بدون نیاز به نصب دوباره اپ، محتوا به‌روز می‌شود (نیاز به اینترنت دارد).",
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
                            "خطا در بروزرسانی: ${result.exceptionOrNull()?.message ?: "اتصال اینترنت را بررسی کنید"} ❌"
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

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("تغییر رمز عبور", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "اگر رمزی تنظیم نکنید، ورود به برنامه بدون رمز آزاد خواهد بود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            ChangePasswordSection()

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("پشتیبان‌گیری", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "یادداشت‌ها و علاقه‌مندی‌های خود را در یک فایل متنی ذخیره یا اشتراک‌گذاری کنید (مثلاً برای وقتی گوشی عوض می‌کنید).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                scope.launch { exportBackup(context, db) }
            }) {
                Text("خروجی گرفتن از یادداشت‌ها و علاقه‌مندی‌ها")
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
private fun LineSpacingOption(label: String, value: Float, current: Float, onSelect: (Float) -> Unit) {
    val selected = kotlin.math.abs(current - value) < 0.01f
    FilterChip(
        selected = selected,
        onClick = { onSelect(value) },
        label = { Text(label) }
    )
}

@Composable
private fun ThemeOption(
    label: String,
    value: String,
    current: String,
    onSelect: (String) -> Unit,
    swatchColor: androidx.compose.ui.graphics.Color? = null
) {
    FilterChip(
        selected = current == value,
        onClick = { onSelect(value) },
        label = { Text(label) },
        leadingIcon = swatchColor?.let {
            {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(it, shape = androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    )
}

@Composable
private fun ChangePasswordSection() {
    val context = LocalContext.current
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    val hasPasswordSet = Prefs.getAppPassword(context).isNotBlank()

    Column {
        if (hasPasswordSet) {
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("رمز فعلی") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("رمز جدید (برای غیرفعال‌کردن رمز، خالی بگذارید)") },
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("تکرار رمز جدید") },
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        message?.let {
            Text(
                it,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }
        Button(onClick = {
            val savedPassword = Prefs.getAppPassword(context)
            when {
                savedPassword.isNotBlank() && currentPassword != savedPassword -> {
                    isError = true
                    message = "رمز فعلی درست نیست"
                }
                newPassword != confirmPassword -> {
                    isError = true
                    message = "رمز جدید و تکرار آن یکسان نیستند"
                }
                else -> {
                    Prefs.setAppPassword(context, newPassword)
                    isError = false
                    message = if (newPassword.isBlank()) "رمز عبور غیرفعال شد" else "رمز عبور با موفقیت تغییر کرد ✅"
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                }
            }
        }) {
            Text("ذخیره رمز")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ورژن برنامه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("نسخه برنامه: ${BuildConfig.VERSION_NAME}")
        }
    }
}
