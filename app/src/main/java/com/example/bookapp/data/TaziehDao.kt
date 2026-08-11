    // ===== متدهای Insert (برای واردات JSON) =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: FieldEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTazieh(tazieh: TaziehEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: RoleEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity): Long
    
    // ===== متدهای Delete (برای پاک کردن دیتابیس) =====
    @Query("DELETE FROM sections")
    suspend fun deleteAllSections()
    
    @Query("DELETE FROM roles")
    suspend fun deleteAllRoles()
    
    @Query("DELETE FROM taziehs")
    suspend fun deleteAllTaziehs()
    
    @Query("DELETE FROM fields")
    suspend fun deleteAllFields()
