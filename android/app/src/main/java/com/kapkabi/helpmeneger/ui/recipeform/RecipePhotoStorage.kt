package com.kapkabi.helpmeneger.ui.recipeform

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Copies a picked gallery image into app-private storage so the recipe's `photo_path` stays
 * valid even if the user removes the original from their gallery (matches the desktop
 * project's behaviour of storing a stable local path rather than a content URI).
 */
object RecipePhotoStorage {
    private const val PHOTOS_DIR = "recipe_photos"

    fun copyToAppStorage(context: Context, sourceUri: Uri): String? {
        val photosDir = File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }
        val destFile = File(photosDir, "${UUID.randomUUID()}.jpg")
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            destFile.absolutePath
        } catch (_: java.io.IOException) {
            null
        }
    }
}
