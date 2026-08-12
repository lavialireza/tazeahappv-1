package com.example.bookapp.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImportResult(
    val success: Boolean,
    val count: Int = 0,
    val errorMessage: String? = null
)

class JsonImporter(
    private val db: AppDatabase
) {
    suspend fun importJson(jsonString: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            val gson = Gson()
            val type = object : TypeToken<List<FieldEntity>>() {}.type
            val fields: List<FieldEntity> = gson.fromJson(jsonString, type)

            if (fields.isEmpty()) {
                return@withContext ImportResult(false, errorMessage = "فایل JSON خالی است")
            }

            var totalSections = 0

            fields.forEach { field ->
                val fieldId = db.fieldDao().insert(field)

                field.taziehList?.forEach { tazieh ->
                    val taziehId = db.taziehDao().insert(
                        TaziehEntity(
                            fieldId = fieldId,
                            title = tazieh.title
                        )
                    )

                    tazieh.roles?.forEach { role ->
                        val roleId = db.roleDao().insert(
                            RoleEntity(
                                taziehId = taziehId,
                                title = role.title
                            )
                        )

                        role.sections?.forEach { section ->
                            db.sectionDao().insert(
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

            ImportResult(true, count = totalSections)

        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(false, errorMessage = e.message ?: "خطای ناشناخته")
        }
    }
}
