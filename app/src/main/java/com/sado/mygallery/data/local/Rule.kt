package com.sado.mygallery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val aiLabel: String,
    val targetFolderName: String,
    val isActive: Boolean = true
)
