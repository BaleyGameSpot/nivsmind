package com.magicai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicai.app.data.models.User
import com.magicai.app.data.models.UserUpdateRequest
import com.magicai.app.data.models.UsageData
import com.magicai.app.data.models.AffiliateData
import com.magicai.app.data.repository.AuthRepository
import com.magicai.app.data.repository.UserRepository
import com.magicai.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val usageData: UsageData? = null,
    val affiliateData: AffiliateData? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadUsageData()
    }

    fun loadProfile() {
        viewModelScope.launch {
            userRepository.getProfile().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, user = result.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun loadUsageData() {
        viewModelScope.launch {
            userRepository.getUsageData().collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { it.copy(usageData = result.data) }
                }
            }
        }
    }

    fun updateProfile(name: String, surname: String) {
        viewModelScope.launch {
            userRepository.updateProfile(UserUpdateRequest(name, surname)).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, user = result.data, successMessage = "Profile updated successfully")
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun loadAffiliateData() {
        viewModelScope.launch {
            userRepository.getAffiliates().collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { it.copy(affiliateData = result.data) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
