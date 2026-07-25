package com.sado.mygallery.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sado.mygallery.ai.AiAnalyzer
import com.sado.mygallery.ai.AnalysisResult
import com.sado.mygallery.data.GalleryRepository
import com.sado.mygallery.data.local.Album
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageDetailViewModel @Inject constructor(
    private val aiAnalyzer: AiAnalyzer,
    private val repository: GalleryRepository
) : ViewModel() {

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    fun analyzeImage(uriString: String) {
        _analysisState.value = AnalysisState.Loading
        viewModelScope.launch {
            val result = aiAnalyzer.analyzeImage(uriString)
            _analysisState.value = when (result) {
                is AnalysisResult.Success -> AnalysisState.Success(result.labels, result.faceCount)
                is AnalysisResult.Error -> AnalysisState.Error(result.message)
            }
        }
    }

    val albums: StateFlow<List<Album>> = repository.getAllAlbums()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addToAlbum(albumId: Long, imageUri: String) {
        viewModelScope.launch {
            repository.addImageToAlbum(albumId, imageUri)
        }
    }
}

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val labels: List<String>, val faceCount: Int) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}
