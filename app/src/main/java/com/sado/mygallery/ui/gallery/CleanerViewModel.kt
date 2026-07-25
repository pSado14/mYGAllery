package com.sado.mygallery.ui.gallery

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sado.mygallery.data.GalleryRepository
import com.sado.mygallery.data.GalleryImage
import com.sado.mygallery.utils.CleanerUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import javax.inject.Inject

data class DuplicateGroup(
    val id: String,
    val images: List<GalleryImage>
)

@HiltViewModel
class CleanerViewModel @Inject constructor(
    application: Application,
    private val repository: GalleryRepository
) : AndroidViewModel(application) {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _blurryImages = MutableStateFlow<List<GalleryImage>>(emptyList())
    val blurryImages: StateFlow<List<GalleryImage>> = _blurryImages

    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups

    private val BLUR_THRESHOLD = 150.0 // adjust as needed
    private val SIMILARITY_THRESHOLD = 5 // max 5 bits difference for pHash

    fun startScan() {
        if (_isScanning.value) return
        _isScanning.value = true

        viewModelScope.launch(Dispatchers.Default) {
            val context = getApplication<Application>()
            val allImages = repository.getImages()
            
            val blurryList = mutableListOf<GalleryImage>()
            val hashList = mutableListOf<Pair<GalleryImage, Long>>()

            for (image in allImages) {
                yield() // prevent blocking the thread
                // Skip videos because BitmapFactory can't decode them and they will return fallback values
                if (image.uri.toString().contains("video")) {
                    continue
                }

                // 1. Calculate Blur
                val blurScore = CleanerUtils.getBlurScore(context, image.uri)
                if (blurScore < BLUR_THRESHOLD) {
                    blurryList.add(image)
                }

                // 2. Calculate Hash for duplicates
                val hash = CleanerUtils.getPHash(context, image.uri)
                hashList.add(Pair(image, hash))
            }

            // Group duplicates
            val groups = mutableListOf<DuplicateGroup>()
            val processedIndices = mutableSetOf<Int>()

            for (i in hashList.indices) {
                yield()
                if (processedIndices.contains(i)) continue
                val (img1, hash1) = hashList[i]
                
                val currentGroup = mutableListOf<GalleryImage>()
                currentGroup.add(img1)
                processedIndices.add(i)

                for (j in i + 1 until hashList.size) {
                    if (processedIndices.contains(j)) continue
                    val (img2, hash2) = hashList[j]
                    
                    val distance = CleanerUtils.hammingDistance(hash1, hash2)
                    if (distance <= SIMILARITY_THRESHOLD) {
                        currentGroup.add(img2)
                        processedIndices.add(j)
                    }
                }

                if (currentGroup.size > 1) {
                    groups.add(DuplicateGroup(id = img1.uri.toString(), images = currentGroup))
                }
            }

            _blurryImages.value = blurryList
            _duplicateGroups.value = groups
            _isScanning.value = false
        }
    }
}
