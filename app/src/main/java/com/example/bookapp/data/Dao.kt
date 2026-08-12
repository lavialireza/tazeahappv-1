package com.example.bookapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// ============================================================
//  بخش ۱: FieldDao
// ============================================================
@Dao
interface FieldDao {
    @Query("SELECT * FROM fields ORDER BY id")
    suspend fun getAll(): List<FieldEntity>

    @Insert
    suspend fun insert(field: FieldEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: FieldEntity): Long

    @Query("DELETE FROM fields")
    suspend fun deleteAll()
}

// ============================================================
//  بخش ۲: TaziehDao
// ============================================================
@Dao
interface TaziehDao {
    @Query("SELECT * FROM taziehs WHERE fieldId = :fieldId ORDER BY id")
    suspend fun getByField(fieldId: Long): List<TaziehEntity>

    @Query("SELECT * FROM taziehs ORDER BY fieldId, id")
    suspend fun getAll(): List<TaziehEntity>

    @Insert
    suspend fun insert(tazieh: TaziehEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTazieh(tazieh: TaziehEntity): Long

    @Query("DELETE FROM taziehs")
    suspend fun deleteAll()
}

// ============================================================
//  بخش ۳: RoleDao
// ============================================================
@Dao
interface RoleDao {
    @Query("SELECT * FROM roles WHERE taziehId = :taziehId ORDER BY id")
    suspend fun getByTazieh(taziehId: Long): List<RoleEntity>

    @Query("SELECT * FROM roles WHERE id = :roleId")
    suspend fun getById(roleId: Long): RoleEntity

    @Insert
    suspend fun insert(role: RoleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: RoleEntity): Long

    @Query("DELETE FROM roles")
    suspend fun deleteAll()
}

// ============================================================
//  بخش ۴: SectionDao
// ============================================================
@Dao
interface SectionDao {
    @Query("SELECT * FROM sections WHERE roleId = :roleId ORDER BY orderIndex")
    suspend fun getByRole(roleId: Long): List<SectionEntity>

    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getById(sectionId: Long): SectionEntity

    @Insert
    suspend fun insert(section: SectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity): Long

    @Query("DELETE FROM sections")
    suspend fun deleteAll()
}
