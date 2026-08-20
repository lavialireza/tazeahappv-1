# راهنمای کامل توسعه‌دهنده — اپلیکیشن تعزیه (taziehapp)

این فایل طوری نوشته شده که بدون نیاز به کمک گرفتن از جای دیگه، بتونید با مراجعه بهش:
- بفهمید هر فایل چیکار می‌کنه،
- یک صفحه/قابلیت جدید اضافه کنید،
- یک قابلیت موجود رو حذف/غیرفعال کنید،
- خروجی (APK) بگیرید،
- و محتوا یا خود برنامه رو آپدیت کنید.

فهرست مطالب:
1. [زبان و ابزارهای برنامه](#1-زبان-و-ابزارهای-برنامه)
2. [نقشه کلی معماری (چگونه همه چیز به هم وصله)](#2-نقشه-کلی-معماری)
3. [توضیح تک‌تک فایل‌ها](#3-توضیح-تک‌تک-فایل‌ها)
4. [چطور یک صفحه جدید اضافه کنم؟](#4-چطور-یک-صفحه-جدید-اضافه-کنم)
5. [چطور یک قابلیت موجود را حذف/غیرفعال کنم؟](#5-چطور-یک-قابلیت-موجود-را-حذفغیرفعال-کنم)
6. [توضیح کدها بر اساس موضوع](#6-توضیح-کدها-بر-اساس-موضوع)
7. [چطور خروجی (APK) بگیرم؟](#7-چطور-خروجی-apk-بگیرم)
8. [آپدیت برنامه و آپدیت محتوا چگونه است؟](#8-آپدیت-برنامه-و-آپدیت-محتوا-چگونه-است)
9. [اشکالات رایج و راه‌حل سریع](#9-اشکالات-رایج-و-راه‌حل-سریع)

---

## ۱. زبان و ابزارهای برنامه

| بخش | زبان/ابزار |
|---|---|
| اپلیکیشن اندروید (رابط کاربری + منطق) | **Kotlin** با **Jetpack Compose** (UI اعلانی، نه XML قدیمی اندروید) |
| دیتابیس داخل گوشی | **Room** (یک لایه‌ی راحت روی SQLite) |
| فایل‌های محتوای تعزیه | **JSON** (داخل `app/src/main/assets/content/`) |
| اسکریپت تبدیل Word به JSON | **Python** (`scripts/docx_to_json.py`) |
| ساخت خروجی (Build) | **Gradle** (فایل‌های `build.gradle.kts` با نحو Kotlin) |
| تست خودکار Kotlin | JUnit + Robolectric (`app/src/test/...`) |
| تست خودکار Python | `unittest` (`scripts/tests/test_docx_to_json.py`) |
| ساخت خودکار (CI) | GitHub Actions (`.github/workflows/build.yml`) |

نکته: کل UI با Compose نوشته شده، یعنی صفحات فایل XML لایوت (`layout/*.xml`) ندارن؛ هر صفحه یک تابع Kotlin با علامت `@Composable` هست.

---

## ۲. نقشه کلی معماری

```
MainActivity.kt  (نقطه ورود اپ)
        │
        ▼
AppNavigation.kt  (مسیریاب: کدوم صفحه بعد از کدوم صفحه میاد + انیمیشن گذر)
        │
        ├── screens/ (هر صفحه یک @Composable مستقل، فقط UI - بدون منطق دیتابیس مستقیم)
        │
        └── data/  (دیتابیس Room، DAO ها، Entity ها، کمک‌کننده‌های PDF/صدا/بک‌آپ/تنظیمات)
```

قانون مهم پروژه: **فایل‌های `ui/screens/*.kt` نباید مستقیم با دیتابیس کار کنند.**
واکشی داده از دیتابیس همیشه داخل `AppNavigation.kt` (در بلوک `LaunchedEffect`) انجام می‌شود و نتیجه به‌صورت پارامتر به Composable صفحه پاس داده می‌شود. این باعث می‌شه صفحات ساده، قابل تست، و قابل استفاده مجدد بمونن.

جریان داده تعزیه‌ها به این شکله (سلسله‌مراتب چهار سطحی):

```
Field (زمینه: اصفهان، تهران، ...)
  └── Tazieh (عاشورا، بازار شام، ...)
        └── Role (شمر، امام، یزید، ...)
              └── Section (ورود، ساقی‌نامه، شهادت، ...) → متن شعر
```

---

## ۳. توضیح تک‌تک فایل‌ها

### فایل اصلی
- **`MainActivity.kt`** — فعالیت (Activity) اصلی و تنها اکتیویتی اپ. تم برنامه رو ست می‌کنه و `AppNavigation()` رو صدا می‌زنه. تقریباً هیچ‌وقت لازم نیست این فایل رو تغییر بدید مگر برای تنظیمات سطح اپلیکیشن (مثل edge-to-edge).

### `ui/AppNavigation.kt` — قلب برنامه
مسیرها (route ها) و اینکه کدوم صفحه با چه داده‌ای باز می‌شه، همه اینجاست. هر `composable("route") { ... }` یعنی یک مقصد. اینجا:
- داده از `db.xxxDao()` واکشی می‌شه (`LaunchedEffect`)
- به Composable مربوطه در `ui/screens` پاس داده می‌شه
- `navController.navigate("route/آرگومان")` برای رفتن بین صفحات استفاده می‌شه
- انیمیشن گذر بین صفحات (`enterTransition`/`exitTransition`) هم همینجا تعریف شده، با متغیر `navAnimDuration` (بر حسب میلی‌ثانیه) که سرعت انیمیشن رو کنترل می‌کنه؛ عدد کوچیک‌تر = سریع‌تر.

### `ui/screens/*.kt` — صفحات (فقط UI)
| فایل | صفحه | چی نشون می‌ده |
|---|---|---|
| `SplashScreen.kt` | اسپلش | لوگو + انیمیشن ورود، چند ثانیه اول اجرای برنامه |
| `OnboardingScreen.kt` | آموزش اولیه | چند صفحه معرفی برای اولین اجرا |
| `LoginScreen.kt` | ورود | نام‌کاربری + رمز (رمز اختیاریه، پیش‌فرض خالی) |
| `MainMenuScreen.kt` | منوی اصلی | دسترسی سریع به همه بخش‌ها (فهرست، جستجو، بوکمارک، یادداشت، نقش من، تنظیمات...) |
| `ListScreen.kt` | فهرست عمومی (`GenericListScreen`) | یک لیست قابل‌کلیک با پشتیبانی از حالت انتخاب (برای مقایسه یا انتخاب «نقش من») — توسط چند صفحه دیگه هم استفاده می‌شه |
| `TextScreen.kt` | نمایش متن یک بخش + `TextPagerScreen` (ورق‌زدن بین بخش‌های یک نقش) | نمایش شعر، بوکمارک، TTS/صوت واقعی، بخش‌های مرتبط |
| `SearchScreen.kt` | جستجو | جستجوی متن در همه محتوا (با FTS) |
| `BookmarksScreen.kt` | نشان‌شده‌ها | لیست بخش‌های بوکمارک‌شده |
| `NotesScreen.kt` | یادداشت‌ها | یادداشت‌های شخصی روی بخش‌ها |
| `CompareScreen.kt` | مقایسه دو نقش | نمایش پهلو‌به‌پهلوی دو نقش از یک تعزیه |
| `MyRoleScreen.kt` | نقش من | لیست نقش‌هایی که کاربر مشخص کرده، با دسترسی سریع به مطالعه/تمرین/PDF |
| `RehearsalScreen.kt` | حالت تمرین | آشکارسازی خط‌به‌خط شعر برای حفظ‌کردن |
| `InfoScreens.kt` | چند صفحه در یک فایل: `AboutScreen`, `SettingsScreen`, `VersionScreen` | درباره برنامه / تنظیمات (تم، فونت، تغییر رمز، پشتیبان‌گیری، بروزرسانی محتوا) / اطلاعات نسخه |

### `ui/theme/*.kt`
- **`ThemeColors.kt`** — پالت رنگی برنامه (رنگ سبز/طلایی هویت بصری تعزیه، حالت روشن/تاریک).
- **`Typography.kt`** — فونت‌ها و سایز متن‌ها.

### `data/` — دیتابیس و منطق
- **`Entities.kt`** — تعریف جدول‌های دیتابیس: `FieldEntity`, `TaziehEntity`, `RoleEntity`, `SectionEntity` (شامل `audioUrl` اختیاری برای صوت واقعی).
- **`NoteEntity.kt`** — جدول یادداشت‌ها (جدا از بقیه چون به‌صورت مستقل اضافه شده بود).
- **`SectionFts.kt`** — جدول ایندکس جستجوی سریع (FTS4) روی عنوان و متن بخش‌ها.
- **`Dao.kt`** — عملیات دیتابیس (`FieldDao`, `TaziehDao`, `RoleDao`, `SectionDao`, `NoteDao`؛ هرکدوم `getAll`, `getById`, `getByTitle`, `insert`, `deleteAll` و مشابه).
- **`SearchDao.kt`** — کوئری‌های جستجو (جستجوی کلی با FTS، جستجوی داخل یک زمینه/تعزیه، و کوئری «بخش‌های مرتبط» بر اساس هم‌نامی عنوان).
- **`AppDatabase.kt`** — تعریف کلاس دیتابیس Room (نسخه فعلی: `version = 3`)، و تابع‌های:
  - `syncLocalContentFiles` — فایل‌های تازه‌ی `assets/content/*.json` رو تشخیص و اضافه می‌کنه (بدون پاک‌کردن قبلی‌ها).
  - `syncRemoteContent` — محتوا رو از یک URL روی اینترنت می‌گیره و merge می‌کنه (برای بروزرسانی محتوا بدون نیاز به نسخه جدید اپ).
  - `mergeContentFromJson` — منطق اصلی merge: اگه زمینه/تعزیه/نقش با همون عنوان موجود باشه، بهش اضافه می‌کنه؛ وگرنه می‌سازه. بخش‌ها همیشه با شماره‌ترتیب ادامه‌دار اضافه می‌شن.
- **`Prefs.kt`** — تنظیمات ساده (`SharedPreferences`) مثل: بوکمارک‌ها، رمز عبور، نقش‌های «من»، فایل‌های محتوای پردازش‌شده، تم/فونت انتخابی، آمار مطالعه.
- **`PdfExportHelper.kt`** — ساخت PDF از بخش‌های یک نقش (`exportRoleToPdf`).
- **`BackupHelper.kt`** — پشتیبان‌گیری/بازیابی از بوکمارک‌ها و یادداشت‌ها (نه از محتوای تعزیه، چون اون از assets میاد).
- **`SpeechHelper.kt`** — پخش صدای مصنوعی (Text-to-Speech اندروید) برای متن یک بخش.
- **`AudioPlayerHelper.kt`** — پخش صدای واقعی/ضبط‌شده (`MediaPlayer`) وقتی بخش یک `audioUrl` دارد؛ در غیر این صورت TTS جایگزین می‌شود.

### `scripts/`
- **`docx_to_json.py`** — فایل Word (با استایل‌های Heading 1 تا 4) رو به JSON قابل‌فهم برنامه تبدیل می‌کنه. با اجرای ساده (`python docx_to_json.py file.docx`) یک فایل تازه و شماره‌دار داخل `app/src/main/assets/content/` می‌سازه (نیازی به تعیین مسیر خروجی نیست).
- **`tests/test_docx_to_json.py`** — تست واحد پایتون برای منطق تبدیل.

### `.github/workflows/build.yml`
تعریف پایپلاین GitHub Actions: با هر push به شاخه `main`، یک APK دیباگ می‌سازه و به‌عنوان Artifact قابل‌دانلود آپلود می‌کنه (بخش «چطور خروجی بگیرم» رو ببینید).

### `app/src/main/assets/content/*.json`
محتوای واقعی تعزیه‌ها، به‌صورت چند فایل مستقل که هرکدوم فقط شامل محتوای تازه هستن (نه کل داده). ترتیب بارگذاری بر اساس نام فایل (الفبایی/عددی) است.

---

## ۴. چطور یک صفحه جدید اضافه کنم؟

فرض کنید می‌خواید یک صفحه‌ی جدید به اسم «آمار من» اضافه کنید.

**قدم ۱ — ساخت فایل Composable صفحه**
یک فایل جدید بسازید: `app/src/main/java/com/example/bookapp/ui/screens/MyStatsScreen.kt`

```kotlin
package com.example.bookapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStatsScreen(
    totalReadCount: Int,   // داده‌ای که از AppNavigation پاس داده می‌شه
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("آمار من") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("تعداد بخش‌های خوانده‌شده: $totalReadCount")
        }
    }
}
```

**قدم ۲ — تعریف route در `AppNavigation.kt`**
اول یک ثابت route بالای فایل (کنار بقیه `ROUTE_...`) اضافه کنید:
```kotlin
private const val ROUTE_MY_STATS = "my_stats"
```

بعد، جایی در بدنه‌ی `NavHost { ... }` یک `composable` جدید اضافه کنید:
```kotlin
composable(ROUTE_MY_STATS) {
    var count by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        count = Prefs.getReadCount(context) // یا هر منبع داده‌ی دیگه
    }
    MyStatsScreen(
        totalReadCount = count,
        onBack = { navController.popBackStack() }
    )
}
```

**قدم ۳ — راه ورود به صفحه**
جایی که می‌خواید کاربر به این صفحه بره (مثلاً یک دکمه در `MainMenuScreen`)، یک لامبدا `onOpenMyStats: () -> Unit` به پارامترهای اون صفحه اضافه کنید، و در `AppNavigation.kt` وصلش کنید به:
```kotlin
onOpenMyStats = { navController.navigate(ROUTE_MY_STATS) }
```

همین سه قدم برای هر صفحه‌ی جدید تکرار می‌شه. اگه صفحه به آرگومان نیاز داره (مثلاً یک شناسه)، آرگومان رو داخل مسیر بذارید:
```kotlin
private const val ROUTE_MY_STATS = "my_stats/{someId}"
...
composable(ROUTE_MY_STATS) { backStackEntry ->
    val someId = backStackEntry.arguments?.getString("someId")?.toLongOrNull() ?: 0L
    ...
}
...
navController.navigate("my_stats/$someId")
```

---

## ۵. چطور یک قابلیت موجود را حذف/غیرفعال کنم؟

سه سطح داره، از ساده به کامل:

**سطح ۱ — فقط پنهان‌کردن از UI (سریع‌ترین و کم‌ریسک‌ترین راه)**
مثلاً برای حذف دکمه‌ی «نقش من» از منوی اصلی، کافیه در `MainMenuScreen.kt` خط مربوط به اون `MenuCard(...)` رو کامنت یا حذف کنید. کد دیتابیس/منطق پشتش دست‌نخورده می‌مونه (بی‌خطرترین حالت، چون به‌راحتی قابل بازگردوندنه).

**سطح ۲ — حذف کامل یک صفحه**
1. `composable(ROUTE_XXX) { ... }` مربوطه رو از `AppNavigation.kt` حذف کنید.
2. فایل `ui/screens/XxxScreen.kt` رو حذف کنید.
3. هر جایی که `navController.navigate("xxx...")` صداش می‌زد رو پیدا و حذف/جایگزین کنید (با جستجوی سراسری اسم route).
4. اگه DAO یا Entity مخصوص همون قابلیت بود (مثلاً یک جدول جدید) و جای دیگه استفاده نمی‌شه، می‌تونید حذفش کنید — ولی دقت کنید که حذف یک Entity از لیست `entities = [...]` در `AppDatabase.kt` باعث تغییر «نسخه دیتابیس» می‌شه (بخش ۸ رو ببینید).

**سطح ۳ — غیرفعال‌سازی موقت بدون حذف کد (برای قابلیت‌های آزمایشی)**
یک پرچم (flag) ساده تعریف کنید، مثلاً بالای `AppNavigation.kt`:
```kotlin
private const val FEATURE_MY_STATS_ENABLED = false
```
و در جایی که دکمه/ورودی رو نشون می‌دید:
```kotlin
if (FEATURE_MY_STATS_ENABLED) {
    MenuCard("آمار من", ..., onOpenMyStats)
}
```
این روش برای قابلیت‌هایی که مطمئن نیستید می‌خواید نگه دارید عالیه، چون با یک خط تغییر می‌تونید دوباره روشنش کنید.

---

## ۶. توضیح کدها بر اساس موضوع

### چرخه بارگذاری محتوا (تعزیه‌ها)
1. اپ باز می‌شه → `AppNavigation.kt` در `LaunchedEffect(Unit)` تابع `syncLocalContentFiles(context, db)` رو صدا می‌زنه.
2. این تابع پوشه‌ی `assets/content/` رو لیست می‌کنه، فایل‌هایی که در `Prefs` به‌عنوان «پردازش‌شده» ثبت نشدن رو پیدا می‌کنه.
3. هر فایل جدید با `mergeContentFromJson` وارد دیتابیس می‌شه (با upsert بر اساس عنوان، نه insert کورکورانه).
4. نام فایل‌های پردازش‌شده در `Prefs` (`SharedPreferences`) ذخیره می‌شه تا دفعه بعد دوباره پردازش نشن.

### چرخه جستجو
- `SearchScreen.kt` عبارت جستجو رو می‌گیره → `AppNavigation.kt` صداش می‌زنه به `db.searchDao().search(query)` (که از جدول FTS `sections_fts` با `MATCH` استفاده می‌کنه، نه `LIKE`، برای سرعت بیشتر روی محتوای زیاد).
- تریگرهای SQL (در `AppDatabase.kt`، داخل `ftsSyncTriggersCallback`) جدول FTS رو خودکار با جدول اصلی `sections` همگام نگه می‌دارن؛ یعنی وقتی محتوای جدید insert می‌شه، جستجو هم خودکار به‌روز می‌شه — نیازی به کد اضافه نیست.

### چرخه پخش صدا (TTS در برابر صدای واقعی)
- در `TextScreen.kt`، اگه `section.audioUrl` مقدار داشته باشه، دکمه‌ی پخش از `AudioPlayerHelper` (صدای واقعی/ضبط‌شده، فایل mp3) استفاده می‌کنه.
- اگه `audioUrl` خالی/نال باشه، از `SpeechHelper` (TTS، صدای مصنوعی اندروید) استفاده می‌شه.
- برای اضافه‌کردن صدای واقعی به یک بخش: فایل mp3 رو در `app/src/main/assets/audio/` بذارید و در JSON محتوای اون بخش، فیلد `"audio": "audio/filename.mp3"` رو اضافه کنید (اختیاریه؛ نبودش مشکلی ایجاد نمی‌کنه).

### چرخه ورود (Login) و رمز عبور
- `Prefs.getAppPassword(context)` رمز فعلی رو برمی‌گردونه؛ اگه خالی باشه یعنی رمزی تنظیم نشده و ورود با هر چیزی (حتی خالی) در فیلد رمز قبول می‌شه.
- تغییر رمز از `SettingsScreen` (داخل `InfoScreens.kt`، تابع `ChangePasswordSection`) انجام می‌شه؛ خالی‌گذاشتن رمز جدید یعنی غیرفعال‌کردن رمز.

### چرخه ناوبری و انیمیشن
- همه‌ی گذرها بین صفحات از یک تنظیم مشترک در `AppNavigation.kt` استفاده می‌کنن (`navAnimDuration`، فعلاً `220` میلی‌ثانیه) — یعنی سریع و نرم، مناسب هم برای مرور آروم (تمرین) هم برای جابه‌جایی سریع حین اجرا. اگه خواستید یک صفحه‌ی خاص انیمیشن متفاوت داشته باشه، می‌تونید در همون `composable(...)` پارامترهای `enterTransition`/`exitTransition` رو جداگانه override کنید.

### نقش من / حالت تمرین / PDF نقش
- `Prefs.setMyRole/getMyRole/getAllMyRoles` — نگه‌داری اینکه کاربر کدوم نقش رو در کدوم تعزیه به‌عنوان نقش خودش انتخاب کرده.
- `RehearsalScreen.kt` — از روی لیست بخش‌های همون نقش (`db.sectionDao().getByRole(roleId)`) می‌خونه و خط‌به‌خط آشکار می‌کنه.
- `PdfExportHelper.exportRoleToPdf` — از همون لیست بخش‌ها یک PDF چاپی می‌سازه.

---

## ۷. چطور خروجی (APK) بگیرم؟

### روش ۱ — خودکار با GitHub Actions (پیشنهادی، بدون نیاز به نصب چیزی روی سیستم خودتون)
1. تغییرات کد رو به شاخه `main` روی گیت‌هاب push کنید (یا از تب Actions روی گیت‌هاب دستی «Run workflow» بزنید).
2. به تب **Actions** پروژه روی گیت‌هاب برید، روی آخرین اجرا (workflow run) کلیک کنید.
3. اگه ساخت موفق بود (تیک سبز)، پایین صفحه بخش **Artifacts** یک فایل به اسم `app-debug-apk` هست؛ دانلودش کنید و داخلش `app-debug.apk` هست.
4. این APK دیباگه (قابل نصب مستقیم روی گوشی برای تست، ولی برای انتشار در Google Play باید امضای release بگیره — این کار توی این پروژه هنوز تنظیم نشده).

### روش ۲ — با Android Studio روی سیستم خودتون
1. پروژه رو در Android Studio باز کنید (`File → Open` و پوشه‌ی ریشه پروژه رو انتخاب کنید).
2. صبر کنید Gradle Sync تموم بشه.
3. از منو: `Build → Build Bundle(s) / APK(s) → Build APK(s)`.
4. بعد از تموم‌شدن، یک نوتیفیکیشن پایین صفحه لینک `locate` رو نشون می‌ده که مسیر فایل APK رو باز می‌کنه (معمولاً در `app/build/outputs/apk/debug/`).

### روش ۳ — خط فرمان (اگه Android SDK + Gradle نصب دارید)
```bash
./gradlew assembleDebug
```
(روی ویندوز: `gradlew.bat assembleDebug`)

---

## ۸. آپدیت برنامه و آپدیت محتوا چگونه است؟

این پروژه دو نوع «آپدیت» کاملاً جدا از هم داره:

### الف) آپدیت محتوا (اضافه‌کردن مجالس/تعزیه‌های جدید) — نیازی به نسخه جدید اپ نیست
1. فایل Word جدید رو با اسکریپت تبدیل کنید:
   ```bash
   python scripts/docx_to_json.py majles_jadid.docx
   ```
2. این دستور خودش یک فایل تازه‌ی شماره‌دار (مثلاً `003_majles-jadid.json`) داخل `app/src/main/assets/content/` می‌سازه.
3. این یک فایل رو commit/push کنید و یک نسخه جدید APK بسازید (بخش ۷).
4. کاربرانی که اپ رو نصب دارن، با نصب نسخه جدید، **فقط همون فایل تازه** به دیتابیسشون اضافه می‌شه؛ چیزی که قبلاً داشتن (بوکمارک، یادداشت، نقش من، تنظیمات) پاک نمی‌شه، چون این مکانیزم بر اساس «کدوم فایل‌ها قبلاً پردازش شدن» کار می‌کنه، نه پاک‌کردن کامل دیتابیس.

> ⚠️ این روش نیاز به ساخت نسخه‌ی تازه‌ی اپ داره (چون فایل‌های `assets` داخل خود APK پکیج می‌شن). اگه می‌خواید محتوا رو **بدون** نسخه جدید اپ آپدیت کنید، از روش (ب) استفاده کنید.

### ب) آپدیت محتوا از راه دور (بدون نسخه جدید اپ)
تابع `syncRemoteContent(db, url)` در `AppDatabase.kt` یک فایل JSON از یک آدرس اینترنتی (پیش‌فرض: از گیت‌هاب) می‌خونه و merge می‌کنه — دقیقاً با همون منطق merge محلی (بدون پاک‌کردن قبلی). این تابع از `SettingsScreen` (دکمه‌ی «بروزرسانی محتوا») صدا زده می‌شه. برای استفاده از این روش:
1. فایل JSON جدید رو (با همون ساختار fields/taziehs/roles/sections) در یک آدرس عمومی (مثلاً یک فایل روی گیت‌هاب) قرار بدید.
2. اگه آدرس دائمی‌تون همون فایل `001_sample.json` روی گیت‌هابه، محتوای همون فایل رو آپدیت کنید (کاربران با زدن دکمه‌ی «بروزرسانی محتوا» توی تنظیمات، آخرین نسخه رو می‌گیرن).
3. اگه می‌خواید یک URL دیگه پیش‌فرض بشه، پارامتر `url` در تعریف `syncRemoteContent` رو عوض کنید.

### ج) آپدیت خود برنامه (نسخه جدید اکیوایمگ کد/طراحی)
- شماره نسخه در `app/build.gradle.kts` تعریف می‌شه (`versionCode`, `versionName`) — برای هر انتشار جدید این دو رو افزایش بدید.
- بعد از تغییر کد، طبق بخش ۷ خروجی بگیرید.
- **نکته مهم درباره دیتابیس:** اگه ساختار جدول‌ها رو عوض کردید (مثلاً یک ستون جدید به `SectionEntity` اضافه کردید)، باید عدد `version` در `@Database(...)` داخل `AppDatabase.kt` رو یکی افزایش بدید. چون این پروژه از `fallbackToDestructiveMigration()` استفاده می‌کنه (یعنی Migration رسمی نداره)، با افزایش نسخه، دیتابیس کاربر پاک و از نو از `assets/content` ساخته می‌شه (بوکمارک/یادداشت/نقش‌من هم پاک می‌شن، چون تو همون دیتابیس هستن). اگه نمی‌خواید این اتفاق بیفته، باید یک [Room Migration رسمی](https://developer.android.com/training/data-storage/room/migrating-db-versions) بنویسید (فراتر از این راهنماست، ولی مستندات Room توضیح کامل داره).

---

## ۹. اشکالات رایج و راه‌حل سریع

| خطا/مشکل | دلیل رایج | راه‌حل |
|---|---|---|
| `Unresolved reference: dp` یا مشابه در کامپایل | یک `import androidx.compose.ui.unit.dp` (یا مشابه) فراموش شده | همون خط خطا رو نگاه کنید، ببینید چه کلاس/تابعی «Unresolved» است، importش رو دستی اضافه کنید |
| صفحه‌ی تنظیمات/یک صفحه‌ی دیگه، بخشی از محتواش دیده نمی‌شه | `Column` بدون `verticalScroll` است و محتوا از صفحه بیرون افتاده | به `Modifier` همون `Column` این رو اضافه کنید: `.verticalScroll(rememberScrollState())` (و importهای `androidx.compose.foundation.rememberScrollState` / `verticalScroll` رو هم اضافه کنید) |
| بعد از آپدیت اپ، محتوای جدید اضافه نشده | فایل JSON جدید رو در `assets/content/` نذاشتید، یا اسم فایلش با یکی از فایل‌های قبلی یکیه (پس «پردازش‌شده» حساب می‌شه) | مطمئن شید اسم فایل جدید و منحصربه‌فرده و داخل پوشه content هست |
| بعد از تغییر Entity، اپ روی گوشی‌های قبلی کرش می‌کنه یا داده خالیه | فراموش کردید `version` دیتابیس رو در `AppDatabase.kt` افزایش بدید | عدد `version` رو یکی زیاد کنید (بخش ۸-ج) |
| Build با خطای Gradle/Dependency شکست می‌خوره | نسخه یک کتابخونه در `build.gradle.kts` با بقیه ناسازگاره | پیام خطای کامل Gradle رو بخونید (معمولاً اسم کتابخونه مشکل‌دار رو می‌گه)؛ نسخه‌ش رو با بقیه هماهنگ کنید |
