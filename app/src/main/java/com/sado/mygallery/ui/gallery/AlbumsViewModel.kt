package com.sado.mygallery.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sado.mygallery.data.GalleryRepository
import com.sado.mygallery.data.local.Album
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: GalleryRepository
) : ViewModel() {

    val albums: StateFlow<List<Album>> = repository.getAllAlbums()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createAlbum(name: String) {
        viewModelScope.launch {
            repository.createAlbum(name)
        }
    }
}
