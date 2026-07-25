package com.sado.mygallery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey
    val imageUri: String,
    val addedAt: Long = System.currentTimeMillis()
)
