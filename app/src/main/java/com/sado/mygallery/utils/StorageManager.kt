package com.sado.mygallery.utils

import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object StorageManager {

    /**
     * Physically moves a file from its current path to a new folder inside the DCIM or Pictures directory.
     * Required MANAGE_EXTERNAL_STORAGE permission on Android 11+ for standard File API.
     */
    suspend fun moveFileToAlbum(context: Context, sourcePath: String, imageUri: Uri, albumName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return@withContext false

            // Target directory: Pictures/AlbumName
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val targetDir = File(picturesDir, albumName)

            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val destFile = File(targetDir, sourceFile.name)
            
            // If it's the exact same file path, just return
            if (sourceFile.absolutePath == destFile.absolutePath) return@withContext true

            // Try native rename first (Move)
            val renamed = sourceFile.renameTo(destFile)
            if (!renamed) {
                // Move by copying and deleting original
                val inputStream = FileInputStream(sourceFile)
                val outputStream = FileOutputStream(destFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // Delete original using File API
                val deleted = sourceFile.delete()
                if (!deleted) {
                    // Fallback to ContentResolver delete
                    try {
                        context.contentResolver.delete(imageUri, null, null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // If we can't delete the original, we should probably delete the copy we just made 
                        // so it doesn't result in a duplicate, OR we just let it be a copy.
                        // Since the user complained about copy-paste, if delete fails, we just keep the copy 
                        // but log the error.
                    }
                }
            }

            // Notify MediaScanner to update Android OS gallery databases
            MediaScannerConnection.scanFile(
                context,
                arrayOf(destFile.absolutePath, sourceFile.absolutePath),
                null,
                null
            )

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Trash files using Android 11+ MediaStore Trash API.
     * On older versions, this will just attempt a direct delete since there is no native Trash.
     */
    fun getTrashIntentSender(context: Context, uris: List<Uri>): android.content.IntentSender? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return MediaStore.createTrashRequest(context.contentResolver, uris, true).intentSender
        } else {
            // Below Android 11, try direct delete or use createDeleteRequest for Android 10
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
            }
            return null
        }
    }

    suspend fun deleteLegacy(context: Context, uris: List<Uri>) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            uris.forEach { uri ->
                context.contentResolver.delete(uri, null, null)
            }
        }
    }

    /**
     * Restore files from Trash (Android 11+).
     */
    fun getRestoreIntentSender(context: Context, uris: List<Uri>): android.content.IntentSender? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return MediaStore.createTrashRequest(context.contentResolver, uris, false).intentSender
        }
        return null
    }
}
