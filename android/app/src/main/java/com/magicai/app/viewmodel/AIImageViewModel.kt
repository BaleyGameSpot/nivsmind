package com.magicai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicai.app.data.api.ApiService
import com.magicai.app.data.models.GeneratedImage
import com.magicai.app.data.models.ImageGenerateRequest
import com.magicai.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageUiState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedSize: String = "1024x1024",
    val selectedQuality: String = "standard",
    val numberOfImages: Int = 1,
    val generatedImages: List<GeneratedImage> = emptyList(),
    val recentImages: List<GeneratedImage> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AIImageViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageUiState())
    val uiState: StateFlow<ImageUiState> = _uiState.asStateFlow()

    val imageSizes = listOf("512x512", "1024x1024", "1792x1024", "1024x1792")
    val imageQualities = listOf("standard", "hd")

    init {
        loadRecentImages()
    }

    fun loadRecentImages() {
        viewModelScope.launch {
            try {
                val response = apiService.getRecentImages()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(recentImages = response.body() ?: emptyList()) }
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }

    fun generateImage() {
        val prompt = _uiState.value.prompt
        if (prompt.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a prompt") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, errorMessage = null) }
            try {
                val request = ImageGenerateRequest(
                    prompt = prompt,
                    negativePrompt = _uiState.value.negativePrompt.ifBlank { null },
                    size = _uiState.value.selectedSize,
                    quality = _uiState.value.selectedQuality,
                    numberOfImages = _uiState.value.numberOfImages
                )
                val response = apiService.generateImage(request)
                if (response.isSuccessful) {
                    val images = response.body()?.images ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatedImages = images,
                            recentImages = images + it.recentImages
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isGenerating = false, errorMessage = "Image generation failed")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, errorMessage = e.message) }
            }
        }
    }

    fun updatePrompt(prompt: String) = _uiState.update { it.copy(prompt = prompt) }
    fun updateNegativePrompt(prompt: String) = _uiState.update { it.copy(negativePrompt = prompt) }
    fun updateSize(size: String) = _uiState.update { it.copy(selectedSize = size) }
    fun updateQuality(quality: String) = _uiState.update { it.copy(selectedQuality = quality) }
    fun updateNumberOfImages(count: Int) = _uiState.update { it.copy(numberOfImages = count) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
