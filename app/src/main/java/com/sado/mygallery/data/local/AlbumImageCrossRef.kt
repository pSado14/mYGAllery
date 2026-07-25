package com.sado.mygallery.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "album_image_cross_ref",
    primaryKeys = ["albumId", "imageUri"],
    indices = [Index("imageUri")]
)
data class AlbumImageCrossRef(
    val albumId: Long,
    val imageUri: String // We use URI as ID for images since MediaStore ID might not be stable across reboots/changes
)
