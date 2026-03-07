package com.magicai.app.data.repository

import com.magicai.app.data.api.ApiService
import com.magicai.app.data.models.*
import com.magicai.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService,
    private val okHttpClient: OkHttpClient
) {
    fun getRecentChats(): Flow<Resource<List<ChatConversation>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getRecentChats()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body() ?: emptyList()))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to load chats"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getChatHistory(categorySlug: String): Flow<Resource<Map<String, Any>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getChatHistory(categorySlug)
            if (response.isSuccessful) {
                emit(Resource.Success(response.body() ?: emptyMap()))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to load history"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getConversationMessages(converId: String): Flow<Resource<List<ChatMessage>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getConversationMessages(converId)
            if (response.isSuccessful) {
                emit(Resource.Success(response.body() ?: emptyList()))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to load messages"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun startNewChat(categorySlug: String, title: String? = null): Flow<Resource<StartChatResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.startNewChat(NewChatRequest(categorySlug, title))
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to start chat"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun deleteChat(chatId: String): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteChat(DeleteChatRequest(chatId))
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to delete chat"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun renameChat(chatId: String, title: String): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.renameChat(RenameChatRequest(chatId, title))
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to rename chat"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun changeChatTitle(chatId: String, title: String): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.changeChatTitle(ChangeTitleRequest(chatId, title))
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to change title"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun searchRecentChats(query: String): Flow<Resource<List<ChatConversation>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.searchRecentChats(SearchRequest(query))
            if (response.isSuccessful) {
                emit(Resource.Success(response.body() ?: emptyList()))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Search failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }
}
