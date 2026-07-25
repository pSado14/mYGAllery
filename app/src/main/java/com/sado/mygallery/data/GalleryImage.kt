package com.sado.mygallery.data

import android.net.Uri

data class GalleryImage(
    val id: Long,
    val uri: Uri,
    val dateAdded: Long,
    val path: String = "",
    val albumName: String = "",
    val isVideo: Boolean = false,
    val isFavorite: Boolean = false
)
