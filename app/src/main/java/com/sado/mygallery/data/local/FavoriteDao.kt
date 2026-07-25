package com.sado.mygallery.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<Favorite>>
    
    @Query("SELECT imageUri FROM favorites")
    fun getAllFavoriteUris(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE imageUri = :uri")
    suspend fun removeFavorite(uri: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE imageUri = :uri)")
    suspend fun isFavorite(uri: String): Boolean
}
