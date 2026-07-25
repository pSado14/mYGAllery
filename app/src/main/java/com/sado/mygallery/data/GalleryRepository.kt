package com.sado.mygallery.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.sado.mygallery.data.local.Album
import com.sado.mygallery.data.local.AlbumDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GalleryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val albumDao: AlbumDao
) {
    private val mediaTypeSelection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
    private val mediaTypeArgs = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )

    suspend fun getImages(): List<GalleryImage> = withContext(Dispatchers.IO) {
        val images = mutableListOf<GalleryImage>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val contentUri = MediaStore.Files.getContentUri("external")
        
        context.contentResolver.query(
            contentUri,
            projection,
            mediaTypeSelection,
            mediaTypeArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val data = cursor.getString(dataColumn) ?: ""
                val bucket = cursor.getString(bucketColumn) ?: "Bilinmeyen Albüm"
                val mediaType = cursor.getInt(typeColumn)
                
                val baseUri = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                
                val itemUri = ContentUris.withAppendedId(baseUri, id)
                images.add(GalleryImage(id, itemUri, dateAdded, data, bucket, mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO))
            }
        }
        return@withContext images
    }

    fun getAllAlbums(): Flow<List<Album>> = kotlinx.coroutines.flow.flow {
        val albums = mutableListOf<Album>()
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val contentUri = MediaStore.Files.getContentUri("external")
        
        context.contentResolver.query(
            contentUri,
            projection,
            mediaTypeSelection,
            mediaTypeArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            
            val albumMap = mutableMapOf<Long, Album>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Bilinmeyen Albüm"
                val cover = cursor.getString(dataColumn)
                
                if (!albumMap.containsKey(id)) {
                    albumMap[id] = Album(id = id, name = name, coverImageUri = cover)
                }
            }
            albums.addAll(albumMap.values)
        }
        emit(albums)
    }

    suspend fun getAlbumByName(name: String): Album? {
        var foundAlbum: Album? = null
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ? AND ($mediaTypeSelection)"
        val args = arrayOf(name, *mediaTypeArgs)
        val contentUri = MediaStore.Files.getContentUri("external")

        context.contentResolver.query(
            contentUri,
            projection,
            selection,
            args,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                foundAlbum = Album(id = cursor.getLong(idCol), name = cursor.getString(nameCol))
            }
        }
        return foundAlbum ?: Album(id = 0, name = name)
    }

    suspend fun createAlbum(name: String): Long {
        return 0L
    }

    suspend fun addImageToAlbum(albumId: Long, imageUri: String) {
        albumDao.addImageToAlbum(albumId, imageUri)
    }

    fun getImagesForAlbum(albumId: Long): Flow<List<GalleryImage>> = kotlinx.coroutines.flow.flow {
        val images = mutableListOf<GalleryImage>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )
        val selection = "${MediaStore.Images.Media.BUCKET_ID} = ? AND ($mediaTypeSelection)"
        val selectionArgs = arrayOf(albumId.toString(), *mediaTypeArgs)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val contentUri = MediaStore.Files.getContentUri("external")

        context.contentResolver.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val data = cursor.getString(dataColumn) ?: ""
                val bucket = cursor.getString(bucketColumn) ?: "Bilinmeyen Albüm"
                val mediaType = cursor.getInt(typeColumn)
                val baseUri = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val itemUri = ContentUris.withAppendedId(baseUri, id)
                images.add(GalleryImage(id, itemUri, dateAdded, data, bucket, mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO))
            }
        }
        emit(images)
    }

    suspend fun getTrashedImages(): List<GalleryImage> = withContext(Dispatchers.IO) {
        val images = mutableListOf<GalleryImage>()
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.MEDIA_TYPE
            )
            
            val args = android.os.Bundle().apply {
                putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
                putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, mediaTypeSelection)
                putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, mediaTypeArgs)
            }
            val contentUri = MediaStore.Files.getContentUri("external")
            
            context.contentResolver.query(
                contentUri,
                projection,
                args,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val data = cursor.getString(dataColumn) ?: ""
                    val bucket = cursor.getString(bucketColumn) ?: "Bilinmeyen Albüm"
                    val mediaType = cursor.getInt(typeColumn)
                    
                    val baseUri = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    val itemUri = ContentUris.withAppendedId(baseUri, id)
                    images.add(GalleryImage(id, itemUri, dateAdded, data, bucket, mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO))
                }
            }
        }
        return@withContext images
    }
}
