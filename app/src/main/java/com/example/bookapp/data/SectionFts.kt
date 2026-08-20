package com.example.bookapp.data

import androidx.room.Entity
import androidx.room.Fts4

/**
 * جدول ایندکس متنی (FTS4) روی عنوان و متن بخش‌ها، برای جستجوی سریع به‌جای LIKE.
 * این یک «external content table» است: محتوای واقعی همان جدول sections است،
 * فقط ایندکس جستجو اینجا نگه‌داری می‌شود. همگام‌سازی آن با تریگرهای SQL
 * (در AppDatabase، هنگام ساخت دیتابیس) انجام می‌شود.
 */
@Entity(tableName = "sections_fts")
@Fts4(contentEntity = SectionEntity::class)
data class SectionFts(
    val title: String,
    val content: String
)
