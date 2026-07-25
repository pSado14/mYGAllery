package com.sado.mygallery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val coverImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
