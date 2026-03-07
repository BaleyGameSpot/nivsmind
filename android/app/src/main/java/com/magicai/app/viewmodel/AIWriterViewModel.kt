package com.magicai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicai.app.BuildConfig
import com.magicai.app.data.api.ApiService
import com.magicai.app.data.local.TokenManager
import com.magicai.app.data.models.OpenAIGenerator
import com.magicai.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class WriterUiState(
    val isLoading: Boolean = false,
    val generators: List<OpenAIGenerator> = emptyList(),
    val generatedText: String = "",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class AIWriterViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriterUiState())
    val uiState: StateFlow<WriterUiState> = _uiState.asStateFlow()

    val filteredGenerators: StateFlow<List<OpenAIGenerator>> = _uiState
        .map { state ->
            if (state.searchQuery.isEmpty()) state.generators
            else state.generators.filter {
                it.title.contains(state.searchQuery, ignoreCase = true) ||
                        it.description?.contains(state.searchQuery, ignoreCase = true) == true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadGenerators()
    }

    fun loadGenerators() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getOpenAIWriterList()
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(isLoading = false, generators = response.body() ?: emptyList())
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load generators") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun generateText(
        openaiId: Int,
        title: String = "",
        description: String = "",
        keywords: String = "",
        tone: String = "professional",
        language: String = "en",
        maxLength: Int = 500
    ) {
        _uiState.update { it.copy(isGenerating = true, generatedText = "") }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken().first()
                val body = """
                    {
                        "openai_id": $openaiId,
                        "title": "${title.replace("\"", "\\\"")}",
                        "description": "${description.replace("\"", "\\\"")}",
                        "keywords": "${keywords.replace("\"", "\\\"")}",
                        "tone": "$tone",
                        "language": "$language",
                        "maximum_length": $maxLength,
                        "number_of_results": 1
                    }
                """.trimIndent()

                val request = Request.Builder()
                    .url("${BuildConfig.BASE_URL}api/aiwriter/generate-output")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "text/event-stream")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val source = response.body!!.source()
                    val sb = StringBuilder()

                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.startsWith("data: ")) {
                            val data = line.removePrefix("data: ")
                            if (data == "[DONE]") break
                            try {
                                val json = com.google.gson.JsonParser.parseString(data).asJsonObject
                                val content = when {
                                    json.has("choices") -> {
                                        val delta = json.getAsJsonArray("choices")
                                            ?.get(0)?.asJsonObject
                                            ?.get("delta")?.asJsonObject
                                        delta?.get("content")?.asString ?: ""
                                    }
                                    json.has("text") -> json.get("text").asString
                                    else -> ""
                                }
                                if (content.isNotEmpty()) {
                                    sb.append(content)
                                    val current = sb.toString()
                                    _uiState.update { it.copy(generatedText = current) }
                                }
                            } catch (e: Exception) { /* skip */ }
                        }
                    }
                    _uiState.update { it.copy(isGenerating = false) }
                } else {
                    _uiState.update { it.copy(isGenerating = false, errorMessage = "Generation failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, errorMessage = e.message) }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearGeneratedText() {
        _uiState.update { it.copy(generatedText = "") }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
