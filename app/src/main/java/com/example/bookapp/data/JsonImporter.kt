package com.example.bookapp.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ImportResult(
    val success: Boolean,
    val count: Int = 0,
    val errorMessage: String? = null
)

class JsonImporter(
    private val db: AppDatabase
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

            db.withTransaction {
                fields.forEach { field ->
                    val fieldId = db.fieldDao().insertField(field)

                    field.taziehList?.forEach { tazieh ->
                        val taziehId = db.taziehDao().insertTazieh(
                            TaziehEntity(
                                fieldId = fieldId,
                                title = tazieh.title
                            )
                        )

                        tazieh.roles?.forEach { role ->
                            val roleId = db.roleDao().insertRole(
                                RoleEntity(
                                    taziehId = taziehId,
                                    title = role.title
                                )
                            )

                            role.sections?.forEach { section ->
                                db.sectionDao().insertSection(
                                    SectionEntity(
                                        roleId = roleId,
                                        orderIndex = section.orderIndex ?: 0,
                                        title = section.title,
                                        content = section.content
                                    )
                                )
                                totalSections++
                            }
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
