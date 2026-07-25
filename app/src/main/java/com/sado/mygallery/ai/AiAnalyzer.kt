package com.sado.mygallery.ai

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AiAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    
    private val faceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()
    private val faceDetector = FaceDetection.getClient(faceDetectorOptions)

    suspend fun analyzeImage(uriString: String): AnalysisResult {
        return try {
            val uri = Uri.parse(uriString)
            
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream, null, options)
            }
            
            var inSampleSize = 1
            val reqWidth = 1024
            val reqHeight = 1024
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            
            val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw Exception("Görsel yüklenemedi")

            var rotationDegree = 0
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = androidx.exifinterface.media.ExifInterface(stream)
                val orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL)
                rotationDegree = when (orientation) {
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }

            val image = InputImage.fromBitmap(bitmap, rotationDegree)
            
            val labelsTask = labeler.process(image)
            val facesTask = faceDetector.process(image)
            
            val labels = labelsTask.await().map { it.text }
            val facesCount = facesTask.await().size
            
            AnalysisResult.Success(labels, facesCount)
        } catch (e: Exception) {
            AnalysisResult.Error(e.message ?: "Bilinmeyen bir hata oluştu")
        }
    }
}

sealed class AnalysisResult {
    data class Success(val labels: List<String>, val faceCount: Int) : AnalysisResult()
    data class Error(val message: String) : AnalysisResult()
}
