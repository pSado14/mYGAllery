package com.sado.mygallery.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MediaInfo(
    val sizeMb: String,
    val resolution: String,
    val date: String,
    val cameraModel: String,
    val isVideo: Boolean
)

object MediaInfoUtils {

    fun getMediaInfo(context: Context, uri: Uri, isVideo: Boolean): MediaInfo {
        var sizeStr = "Bilinmiyor"
        var resolution = "Bilinmiyor"
        var dateStr = "Bilinmiyor"
        var cameraModel = "Bilinmiyor"

        var storeWidth = 0
        var storeHeight = 0

        // Get file size and MediaStore dimensions
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    val sizeBytes = cursor.getLong(sizeIndex)
                    val sizeMb = sizeBytes / (1024f * 1024f)
                    sizeStr = String.format(Locale.US, "%.2f MB", sizeMb)
                }
                
                val widthIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.WIDTH)
                val heightIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.HEIGHT)
                if (widthIndex != -1 && heightIndex != -1 && !cursor.isNull(widthIndex) && !cursor.isNull(heightIndex)) {
                    storeWidth = cursor.getInt(widthIndex)
                    storeHeight = cursor.getInt(heightIndex)
                }
            }
        }

        // Get EXIF data if it's an image
        if (!isVideo) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val exif = ExifInterface(inputStream)
                    
                    var width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
                    var height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    
                    if (width == 0 || height == 0) {
                        width = storeWidth
                        height = storeHeight
                    }

                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        val temp = width
                        width = height
                        height = temp
                    }
                    
                    if (width > 0 && height > 0) {
                        resolution = "${width}x${height}"
                    }

                    val make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: ""
                    val model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: ""
                    if (make.isNotEmpty() || model.isNotEmpty()) {
                        cameraModel = "$make $model".trim()
                    }

                    val datetime = exif.getAttribute(ExifInterface.TAG_DATETIME)
                    if (datetime != null) {
                        try {
                            val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
                            val dateObj = sdf.parse(datetime)
                            if (dateObj != null) {
                                val outSdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr"))
                                dateStr = outSdf.format(dateObj)
                            }
                        } catch (e: Exception) {
                            dateStr = datetime
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // For videos, try to get basic width/height from MediaMetadataRetriever if possible
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val widthStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val rotationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                
                var width = widthStr?.toIntOrNull() ?: storeWidth
                var height = heightStr?.toIntOrNull() ?: storeHeight
                val rotation = rotationStr?.toIntOrNull() ?: 0
                
                if (rotation == 90 || rotation == 270) {
                    val temp = width
                    width = height
                    height = temp
                }
                
                if (width > 0 && height > 0) {
                    resolution = "${width}x${height}"
                }
                
                val date = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DATE)
                if (date != null) {
                    dateStr = date
                }
                
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return MediaInfo(sizeStr, resolution, dateStr, cameraModel, isVideo)
    }
}
