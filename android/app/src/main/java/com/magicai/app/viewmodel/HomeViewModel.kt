package com.magicai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicai.app.data.api.ApiService
import com.magicai.app.data.models.UsageData
import com.magicai.app.data.models.Document
import com.magicai.app.data.models.ChatConversation
import com.magicai.app.data.repository.UserRepository
import com.magicai.app.data.repository.ChatRepository
import com.magicai.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val usageData: UsageData? = null,
    val recentChats: List<ChatConversation> = emptyList(),
    val recentDocuments: List<Document> = emptyList(),
    val userName: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load usage data
            userRepository.getUsageData().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update { it.copy(usageData = result.data, isLoading = false) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    is Resource.Loading -> {}
                }
            }
        }

        viewModelScope.launch {
            // Load recent chats
            chatRepository.getRecentChats().collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { it.copy(recentChats = result.data?.take(5) ?: emptyList()) }
                }
            }
        }

        viewModelScope.launch {
            // Load profile
            try {
                val profile = apiService.getProfile()
                if (profile.isSuccessful) {
                    val user = profile.body()
                    _uiState.update { it.copy(userName = "${user?.name} ${user?.surname ?: ""}".trim()) }
                }
            } catch (e: Exception) { /* ignore */ }
        }

        viewModelScope.launch {
            // Load recent documents
            try {
                val docs = apiService.getRecentDocuments()
                if (docs.isSuccessful) {
                    _uiState.update { it.copy(recentDocuments = docs.body() ?: emptyList()) }
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }
}
