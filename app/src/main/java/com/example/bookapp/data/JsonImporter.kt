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

// ✅ داده‌های موقت برای تبدیل JSON
data class TempField(
    val title: String,
    val taziehs: List<TempTazieh>? = null
)

data class TempTazieh(
    val title: String,
    val roles: List<TempRole>? = null
)

data class TempRole(
    val title: String,
    val sections: List<TempSection>? = null
)

data class TempSection(
    val title: String,
    val content: String,
    val orderIndex: Int? = null
)

class JsonImporter(
    private val db: AppDatabase
) {
    suspend fun importJson(jsonString: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            val gson = Gson()
            val type = object : TypeToken<List<TempField>>() {}.type
            val fields: List<TempField> = gson.fromJson(jsonString, type)

            if (fields.isEmpty()) {
                return@withContext ImportResult(false, errorMessage = "فایل JSON خالی است")
            }

            var totalSections = 0

            fields.forEach { tempField ->
                val fieldId = db.fieldDao().insert(
                    FieldEntity(title = tempField.title)
                )

                tempField.taziehs?.forEach { tempTazieh ->
                    val taziehId = db.taziehDao().insert(
                        TaziehEntity(
                            fieldId = fieldId,
                            title = tempTazieh.title
                        )
                    )

                    tempTazieh.roles?.forEach { tempRole ->
                        val roleId = db.roleDao().insert(
                            RoleEntity(
                                taziehId = taziehId,
                                title = tempRole.title
                            )
                        )

                        tempRole.sections?.forEach { tempSection ->
                            db.sectionDao().insert(
                                SectionEntity(
                                    roleId = roleId,
                                    orderIndex = tempSection.orderIndex ?: 0,
                                    title = tempSection.title,
                                    content = tempSection.content
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
