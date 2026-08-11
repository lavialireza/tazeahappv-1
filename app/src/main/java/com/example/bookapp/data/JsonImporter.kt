package com.example.bookapp.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ImportResult(
    val success: Boolean,
    val count: Int = 0,
    val errorMessage: String? = null
)

class JsonImporter(
    private val database: BookDatabase
) {
    
    suspend fun importJson(jsonString: String): ImportResult {
        return try {
            val gson = Gson()
            val type = object : TypeToken<List<FieldEntity>>() {}.type
            val fields: List<FieldEntity> = gson.fromJson(jsonString, type)
            
            if (fields.isEmpty()) {
                return ImportResult(false, errorMessage = "فایل JSON خالی است")
            }
            
            var totalSections = 0
            
            fields.forEach { field ->
                val fieldId = database.taziehDao().insertField(field)
                
                field.taziehList?.forEach { tazieh ->
                    val taziehId = database.taziehDao().insertTazieh(
                        TaziehEntity(
                            fieldId = fieldId,
                            title = tazieh.title,
                            description = tazieh.description ?: ""
                        )
                    )
                    
                    tazieh.roles?.forEach { role ->
                        val roleId = database.taziehDao().insertRole(
                            RoleEntity(
                                taziehId = taziehId,
                                name = role.name,
                                description = role.description ?: ""
                            )
                        )
                        
                        role.sections?.forEach { section ->
                            database.taziehDao().insertSection(
                                SectionEntity(
                                    roleId = roleId,
                                    title = section.title,
                                    content = section.content
                                )
                            )
                            totalSections++
                        }
                    }
                }
            }
            
            ImportResult(true, count = totalSections)
            
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(false, errorMessage = e.message ?: "خطای ناشناخته")
        }
    }
}
