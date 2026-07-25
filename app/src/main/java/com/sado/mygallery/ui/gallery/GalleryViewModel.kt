package com.sado.mygallery.ui.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sado.mygallery.ai.AiAnalyzer
import com.sado.mygallery.ai.AnalysisResult
import com.sado.mygallery.data.GalleryImage
import com.sado.mygallery.data.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    application: Application,
    private val repository: GalleryRepository,
    private val aiAnalyzer: AiAnalyzer,
    private val ruleDao: com.sado.mygallery.data.local.RuleDao
) : AndroidViewModel(application) {

    private val _images = MutableStateFlow<List<GalleryImage>>(emptyList())
    val images: StateFlow<List<GalleryImage>> = _images.asStateFlow()

    private val _isOrganizing = MutableStateFlow(false)
    val isOrganizing: StateFlow<Boolean> = _isOrganizing.asStateFlow()

    fun loadImages() {
        viewModelScope.launch {
            val result = repository.getImages()
            _images.value = result
        }
    }

    fun organizeSelectedImages(selectedImages: List<GalleryImage>, onComplete: () -> Unit) {
        if (selectedImages.isEmpty()) return
        
        viewModelScope.launch {
            _isOrganizing.value = true
            try {
                val context = getApplication<android.app.Application>()
                val rules = ruleDao.getAllRules().firstOrNull() ?: emptyList()
                val activeRules = rules.filter { it.isActive }

                for (image in selectedImages) {
                    val analysis = aiAnalyzer.analyzeImage(image.uri.toString())
                    if (analysis is AnalysisResult.Success) {
                        var albumName = "Diğer"
                        var matched = false
                        
                        // Check custom rules first
                        val detectedLabels = analysis.labels.map { it.lowercase() }
                        for (rule in activeRules) {
                            if (rule.aiLabel.lowercase() in detectedLabels || (rule.aiLabel.lowercase() == "insan" && analysis.faceCount > 0)) {
                                albumName = rule.targetFolderName
                                matched = true
                                break
                            }
                        }
                        
                        // Fallback to default logic if no rule matched
                        if (!matched) {
                            if (analysis.faceCount > 0) {
                                albumName = "İnsanlar"
                            } else if (analysis.labels.isNotEmpty()) {
                                albumName = analysis.labels.first()
                            }
                        }

                        // Physically move the file
                        if (image.path.isNotEmpty()) {
                            com.sado.mygallery.utils.StorageManager.moveFileToAlbum(context, image.path, image.uri, albumName)
                        }
                    }
                }
                // Reload images after moving
                loadImages()
            } finally {
                _isOrganizing.value = false
                onComplete()
            }
        }
    }
}
