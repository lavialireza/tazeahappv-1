package com.example.bookapp.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * تصویر انتخاب‌شده از گالری گوشی را داخل حافظه‌ی اختصاصی خود اپ کپی می‌کند
 * (پوشه‌ی files/tazieh_images) تا حتی اگر کاربر بعداً عکس اصلی را از گالری
 * پاک کند یا گوشی ری‌استارت شود، تصویر داخل اپ باقی بماند.
 * مسیر فایل کپی‌شده را برمی‌گرداند (برای ذخیره در دیتابیس).
 */
fun copyImageToAppStorage(context: Context, sourceUri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "tazieh_images").apply { mkdirs() }
        val destFile = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        null
    }
}

fun deleteImageFromAppStorage(filePath: String) {
    try {
        File(filePath).delete()
    } catch (e: Exception) {
        // نادیده گرفتن؛ حذف رکورد دیتابیس مهم‌تر از حذف فایل باقی‌مانده است
    }
}
