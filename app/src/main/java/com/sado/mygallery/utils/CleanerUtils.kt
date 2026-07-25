package com.sado.mygallery.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CleanerUtils {

    // Simple Variance of Laplacian to detect blur
    suspend fun getBlurScore(context: Context, uri: Uri): Double = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            val inputStreamBounds = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStreamBounds, null, options)
            inputStreamBounds?.close()

            // Calculate sample size to load a small image for performance
            var inSampleSize = 1
            val reqWidth = 256
            val reqHeight = 256
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight: Int = options.outHeight / 2
                val halfWidth: Int = options.outWidth / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) return@withContext 1000.0 // Safe fallback

            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            bitmap.recycle()

            val gray = IntArray(width * height)
            for (i in pixels.indices) {
                val c = pixels[i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                gray[i] = (r * 299 + g * 587 + b * 114) / 1000
            }

            var sum = 0.0
            var sumSq = 0.0
            var count = 0
            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    val i = y * width + x
                    val v = gray[i - width] + gray[i - 1] + gray[i + 1] + gray[i + width] - 4 * gray[i]
                    sum += v
                    sumSq += v * v
                    count++
                }
            }

            val mean = sum / count
            val variance = (sumSq / count) - (mean * mean)
            variance
        } catch (e: Exception) {
            1000.0 // Return high variance so it's not marked as blur if it fails
        }
    }

    // Perceptual Hash for 8x8 pixel size
    suspend fun getPHash(context: Context, uri: Uri): Long = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            var inSampleSize = 1
            if (options.outHeight > 32 || options.outWidth > 32) {
                inSampleSize = Math.max(options.outHeight, options.outWidth) / 32
            }
            
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext 0L

            val scaled = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
            if (bitmap != scaled) bitmap.recycle()
            
            val pixels = IntArray(64)
            scaled.getPixels(pixels, 0, 8, 0, 0, 8, 8)
            scaled.recycle()

            val gray = IntArray(64)
            var total = 0
            for (i in pixels.indices) {
                val c = pixels[i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                val gVal = (r * 299 + g * 587 + b * 114) / 1000
                gray[i] = gVal
                total += gVal
            }
            val avg = total / 64

            var hash = 0L
            for (i in 0 until 64) {
                if (gray[i] >= avg) {
                    hash = hash or (1L shl i)
                }
            }
            hash
        } catch (e: Exception) {
            0L
        }
    }

    // Hamming distance to calculate similarity
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        var diff = hash1 xor hash2
        var count = 0
        while (diff != 0L) {
            count++
            diff = diff and (diff - 1)
        }
        return count
    }
}
