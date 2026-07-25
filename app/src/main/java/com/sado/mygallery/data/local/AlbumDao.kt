package com.sado.mygallery.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY createdAt DESC")
    fun getAllAlbums(): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE name = :name LIMIT 1")
    suspend fun getAlbumByName(name: String): Album?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbumImageCrossRef(crossRef: AlbumImageCrossRef)

    @Query("SELECT imageUri FROM album_image_cross_ref WHERE albumId = :albumId")
    fun getImagesForAlbum(albumId: Long): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM album_image_cross_ref WHERE albumId = :albumId")
    fun getAlbumImageCount(albumId: Long): Flow<Int>
    
    @Query("UPDATE albums SET coverImageUri = :uri WHERE id = :albumId AND coverImageUri IS NULL")
    suspend fun updateAlbumCoverIfNull(albumId: Long, uri: String)
    
    @Transaction
    suspend fun addImageToAlbum(albumId: Long, imageUri: String) {
        insertAlbumImageCrossRef(AlbumImageCrossRef(albumId, imageUri))
        updateAlbumCoverIfNull(albumId, imageUri)
    }
}
