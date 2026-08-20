package com.example.bookapp.data

import androidx.room.Dao
import androidx.room.Query

data class SearchResult(
    val sectionId: Long,
    val sectionTitle: String,
    val roleTitle: String,
    val taziehTitle: String,
    val fieldTitle: String
)

@Dao
interface SearchDao {
    // جستجوی سریع با FTS (به‌جای LIKE) روی عنوان/متن بخش‌ها، به‌علاوه‌ی جستجوی
    // ساده روی عنوان نقش/تعزیه (جدول‌های کوچک، LIKE برایشان مشکلی ایجاد نمی‌کند)
    @Query(
        """
        SELECT
            sections.id AS sectionId,
            sections.title AS sectionTitle,
            roles.title AS roleTitle,
            taziehs.title AS taziehTitle,
            fields.title AS fieldTitle
        FROM sections
        INNER JOIN roles ON sections.roleId = roles.id
        INNER JOIN taziehs ON roles.taziehId = taziehs.id
        INNER JOIN fields ON taziehs.fieldId = fields.id
        WHERE sections.id IN (SELECT rowid FROM sections_fts WHERE sections_fts MATCH :ftsQuery)
           OR roles.title LIKE '%' || :rawQuery || '%'
           OR taziehs.title LIKE '%' || :rawQuery || '%'
        ORDER BY fields.title, taziehs.title, roles.title
        LIMIT 100
        """
    )
    suspend fun searchFtsAll(ftsQuery: String, rawQuery: String): List<SearchResult>

    @Query(
        """
        SELECT
            sections.id AS sectionId,
            sections.title AS sectionTitle,
            roles.title AS roleTitle,
            taziehs.title AS taziehTitle,
            fields.title AS fieldTitle
        FROM sections
        INNER JOIN roles ON sections.roleId = roles.id
        INNER JOIN taziehs ON roles.taziehId = taziehs.id
        INNER JOIN fields ON taziehs.fieldId = fields.id
        WHERE fields.id = :fieldId
          AND (
            sections.id IN (SELECT rowid FROM sections_fts WHERE sections_fts MATCH :ftsQuery)
            OR roles.title LIKE '%' || :rawQuery || '%'
            OR taziehs.title LIKE '%' || :rawQuery || '%'
          )
        ORDER BY taziehs.title, roles.title
        LIMIT 100
        """
    )
    suspend fun searchFtsInField(ftsQuery: String, rawQuery: String, fieldId: Long): List<SearchResult>

    @Query(
        """
        SELECT
            sections.id AS sectionId,
            sections.title AS sectionTitle,
            roles.title AS roleTitle,
            taziehs.title AS taziehTitle,
            fields.title AS fieldTitle
        FROM sections
        INNER JOIN roles ON sections.roleId = roles.id
        INNER JOIN taziehs ON roles.taziehId = taziehs.id
        INNER JOIN fields ON taziehs.fieldId = fields.id
        WHERE taziehs.id = :taziehId
          AND (
            sections.id IN (SELECT rowid FROM sections_fts WHERE sections_fts MATCH :ftsQuery)
            OR roles.title LIKE '%' || :rawQuery || '%'
          )
        ORDER BY roles.title
        LIMIT 100
        """
    )
    suspend fun searchFtsInTazieh(ftsQuery: String, rawQuery: String, taziehId: Long): List<SearchResult>

    @Query(
        """
        SELECT
            sections.id AS sectionId,
            sections.title AS sectionTitle,
            roles.title AS roleTitle,
            taziehs.title AS taziehTitle,
            fields.title AS fieldTitle
        FROM sections
        INNER JOIN roles ON sections.roleId = roles.id
        INNER JOIN taziehs ON roles.taziehId = taziehs.id
        INNER JOIN fields ON taziehs.fieldId = fields.id
        WHERE sections.id IN (:ids)
        """
    )
    suspend fun getByIds(ids: List<Long>): List<SearchResult>

    @Query(
        """
        SELECT
            sections.id AS sectionId,
            sections.title AS sectionTitle,
            roles.title AS roleTitle,
            taziehs.title AS taziehTitle,
            fields.title AS fieldTitle
        FROM sections
        INNER JOIN roles ON sections.roleId = roles.id
        INNER JOIN taziehs ON roles.taziehId = taziehs.id
        INNER JOIN fields ON taziehs.fieldId = fields.id
        ORDER BY RANDOM()
        LIMIT 1
        """
    )
    suspend fun getRandomSection(): SearchResult?

    @Query("SELECT COUNT(*) FROM fields")
    suspend fun countFields(): Int

    @Query("SELECT COUNT(*) FROM taziehs")
    suspend fun countTaziehs(): Int

    @Query("SELECT COUNT(*) FROM roles")
    suspend fun countRoles(): Int

    @Query("SELECT COUNT(*) FROM sections")
    suspend fun countSections(): Int

    @Query(
        """
        SELECT
            sections.id AS sectionId,
            sections.title AS sectionTitle,
            roles.title AS roleTitle,
            taziehs.title AS taziehTitle,
            fields.title AS fieldTitle
        FROM sections
        INNER JOIN roles ON sections.roleId = roles.id
        INNER JOIN taziehs ON roles.taziehId = taziehs.id
        INNER JOIN fields ON taziehs.fieldId = fields.id
        WHERE sections.title = :sectionTitle AND sections.id != :excludeSectionId
        ORDER BY fields.title, taziehs.title, roles.title
        LIMIT 20
        """
    )
    suspend fun getRelatedByTitle(sectionTitle: String, excludeSectionId: Long): List<SearchResult>
}

/**
 * از روی متن آزاد کاربر یک عبارت جستجوی FTS (با prefix-match روی هر کلمه) می‌سازد.
 * مثلاً «شمر شهاد» -> «شمر* شهاد*». کاراکترهای خاص FTS حذف می‌شوند تا خطای سینتکس ندهد.
 */
private fun buildFtsQuery(raw: String): String {
    return raw.trim()
        .split(Regex("\\s+"))
        .map { it.replace(Regex("[\"*^]"), "").trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ") { "$it*" }
}

/** جستجو در کل مجموعه؛ اگر query خالی باشد، جای اجرای کوئری FTS (که با ورودی خالی خطا می‌دهد) لیست خالی برمی‌گرداند. */
suspend fun SearchDao.search(query: String): List<SearchResult> {
    val fts = buildFtsQuery(query)
    if (fts.isBlank()) return emptyList()
    return searchFtsAll(fts, query)
}

suspend fun SearchDao.searchInField(query: String, fieldId: Long): List<SearchResult> {
    val fts = buildFtsQuery(query)
    if (fts.isBlank()) return emptyList()
    return searchFtsInField(fts, query, fieldId)
}

suspend fun SearchDao.searchInTazieh(query: String, taziehId: Long): List<SearchResult> {
    val fts = buildFtsQuery(query)
    if (fts.isBlank()) return emptyList()
    return searchFtsInTazieh(fts, query, taziehId)
}
