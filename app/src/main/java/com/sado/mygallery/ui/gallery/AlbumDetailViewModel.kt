package com.sado.mygallery.ui.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sado.mygallery.data.GalleryImage
import com.sado.mygallery.data.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    repository: GalleryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: Long = checkNotNull(savedStateHandle["albumId"])

    val images: StateFlow<List<GalleryImage>> = repository.getImagesForAlbum(albumId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
