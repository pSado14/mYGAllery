package com.sado.mygallery.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sado.mygallery.data.GalleryImage
import com.sado.mygallery.data.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val repository: GalleryRepository
) : ViewModel() {

    private val _trashedImages = MutableStateFlow<List<GalleryImage>>(emptyList())
    val trashedImages: StateFlow<List<GalleryImage>> = _trashedImages.asStateFlow()

    fun loadTrashedImages() {
        viewModelScope.launch {
            _trashedImages.value = repository.getTrashedImages()
        }
    }
}
