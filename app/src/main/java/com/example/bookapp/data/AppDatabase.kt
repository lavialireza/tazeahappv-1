package com.example.bookapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

@Database(
    entities = [
        FieldEntity::class,
        TaziehEntity::class,
        RoleEntity::class,
        SectionEntity::class,
        NoteEntity::class,
        SectionFts::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fieldDao(): FieldDao
    abstract fun taziehDao(): TaziehDao
    abstract fun roleDao(): RoleDao
    abstract fun sectionDao(): SectionDao
    abstract fun searchDao(): SearchDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bookapp.db"
                )
                    // چون بین نسخه‌ها Migration رسمی نداریم، دیتابیس محتوا از نو ساخته می‌شود
                    // (بارگذاری اولیه دوباره از فایل‌های assets/content انجام می‌شود)
                    .fallbackToDestructiveMigration()
                    .addCallback(ftsSyncTriggersCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * جدول sections_fts یک "external content table" است؛ Room خودش تریگر
         * همگام‌سازی نمی‌سازد، پس با این Callback بعد از ساخت دیتابیس (فقط یک‌بار،
         * موقع onCreate) تریگرهای INSERT/UPDATE/DELETE لازم را دستی می‌سازیم.
         */
        private val ftsSyncTriggersCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS sections_fts_ai AFTER INSERT ON sections BEGIN
                        INSERT INTO sections_fts(rowid, title, content) VALUES (new.id, new.title, new.content);
                    END;
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS sections_fts_ad AFTER DELETE ON sections BEGIN
                        DELETE FROM sections_fts WHERE rowid = old.id;
                    END;
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS sections_fts_au AFTER UPDATE ON sections BEGIN
                        DELETE FROM sections_fts WHERE rowid = old.id;
                        INSERT INTO sections_fts(rowid, title, content) VALUES (new.id, new.title, new.content);
                    END;
                    """.trimIndent()
                )
            }
        }
    }
}

/**
 * فایل‌های JSON پوشه‌ی assets/content را که هنوز پردازش نشده‌اند (بر اساس نام فایل)
 * پیدا کرده و محتوای هرکدام را به دیتابیس اضافه می‌کند، بدون پاک‌کردن محتوای قبلی.
 * به این ترتیب برای افزودن یک مجلس تعزیه‌ی جدید، کافی است یک فایل JSON تازه در
 * assets/content قرار بگیرد؛ بقیه‌ی محتوا دست‌نخورده باقی می‌ماند و فقط فایل جدید
 * به ترتیب (بر اساس نام فایل) به انتهای نقش‌های موجود اضافه یا نقش/تعزیه/زمینه‌ی
 * تازه ساخته می‌شود.
 */
suspend fun syncLocalContentFiles(context: Context, db: AppDatabase) {
    val allFiles = context.assets.list("content")?.filter { it.endsWith(".json") }?.sorted() ?: emptyList()
    var processed = Prefs.getProcessedContentFiles(context)

    // اگر دیتابیس به هر دلیلی (مثلاً migration مخرب بین نسخه‌ها) خالی شده باشد
    // ولی Prefs هنوز فایل‌ها را «پردازش‌شده» می‌داند، باید همه چیز دوباره بارگذاری شود.
    if (db.fieldDao().getAll().isEmpty() && processed.isNotEmpty()) {
        processed = emptySet()
    }

    val newFiles = allFiles.filter { it !in processed }
    if (newFiles.isEmpty()) return

    for (fileName in newFiles) {
        val jsonText = context.assets.open("content/$fileName")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        mergeContentFromJson(db, jsonText)
    }
    Prefs.addProcessedContentFiles(context, allFiles) // کل فهرست فعلی را به‌عنوان پردازش‌شده ثبت می‌کنیم (نه فقط newFiles، چون ممکن است processed از صفر بازنشانی شده باشد)
}

/**
 * محتوای فعلی (زمینه/تعزیه/نقش/بخش) را کامل پاک کرده و از یک متن JSON
 * که از اینترنت دریافت شده، دوباره می‌سازد. برای «بروزرسانی محتوا بدون
 * نیاز به ساخت نسخه جدید اپ» استفاده می‌شود.
 * آدرس پیش‌فرض: فایل sample_data.json روی گیت‌هاب (شاخه main).
 */
suspend fun syncRemoteContent(
    db: AppDatabase,
    url: String = "https://raw.githubusercontent.com/lavialireza/taziehapp/main/app/src/main/assets/content/001_sample.json"
): Result<Unit> {
    return try {
        val jsonText = withHttpGet(url)
        mergeContentFromJson(db, jsonText)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun withHttpGet(urlString: String): String {
    val connection = URL(urlString).openConnection() as HttpURLConnection
    connection.connectTimeout = 15000
    connection.readTimeout = 15000
    connection.requestMethod = "GET"
    return try {
        connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

internal suspend fun mergeContentFromJson(db: AppDatabase, jsonText: String) {
    val fields = JSONArray(jsonText)
    db.withTransaction {
        for (fi in 0 until fields.length()) {
            val fieldObj = fields.getJSONObject(fi)
            val fieldTitle = fieldObj.getString("title")
            val fieldId = db.fieldDao().getByTitle(fieldTitle)?.id
                ?: db.fieldDao().insert(FieldEntity(title = fieldTitle))

            val taziehs = fieldObj.getJSONArray("taziehs")
            for (ti in 0 until taziehs.length()) {
                val taziehObj = taziehs.getJSONObject(ti)
                val taziehTitle = taziehObj.getString("title")
                val taziehId = db.taziehDao().getByTitle(fieldId, taziehTitle)?.id
                    ?: db.taziehDao().insert(TaziehEntity(fieldId = fieldId, title = taziehTitle))

                val roles = taziehObj.getJSONArray("roles")
                for (ri in 0 until roles.length()) {
                    val roleObj = roles.getJSONObject(ri)
                    val roleTitle = roleObj.getString("title")
                    val roleId = db.roleDao().getByTitle(taziehId, roleTitle)?.id
                        ?: run {
                            val nextRoleOrder = db.roleDao().getMaxOrderIndex(taziehId) + 1
                            db.roleDao().insert(RoleEntity(taziehId = taziehId, title = roleTitle, orderIndex = nextRoleOrder))
                        }

                    var nextOrderIndex = db.sectionDao().getMaxOrderIndex(roleId) + 1
                    val sections = roleObj.getJSONArray("sections")
                    for (si in 0 until sections.length()) {
                        val secObj = sections.getJSONObject(si)
                        db.sectionDao().insert(
                            SectionEntity(
                                roleId = roleId,
                                orderIndex = nextOrderIndex,
                                title = secObj.getString("title"),
                                content = secObj.getString("content"),
                                audioUrl = secObj.optString("audio", "").ifBlank { null }
                            )
                        )
                        nextOrderIndex++
                    }
                }
            }
        }
    }
}
