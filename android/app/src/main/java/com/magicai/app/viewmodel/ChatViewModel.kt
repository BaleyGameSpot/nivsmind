package com.magicai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicai.app.BuildConfig
import com.magicai.app.data.local.TokenManager
import com.magicai.app.data.models.ChatConversation
import com.magicai.app.data.models.ChatMessage
import com.magicai.app.data.models.StartChatResponse
import com.magicai.app.data.repository.ChatRepository
import com.magicai.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import javax.inject.Inject

data class ChatUiMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val isStreaming: Boolean = false
)

data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<ChatUiMessage> = emptyList(),
    val conversations: List<ChatConversation> = emptyList(),
    val currentChatId: String? = null,
    val errorMessage: String? = null,
    val isStreaming: Boolean = false,
    val inputText: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun loadRecentChats() {
        viewModelScope.launch {
            chatRepository.getRecentChats().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, conversations = result.data ?: emptyList())
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun loadConversationMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.getConversationMessages(chatId).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val msgs = result.data?.flatMap { msg ->
                            val list = mutableListOf<ChatUiMessage>()
                            if (!msg.input.isNullOrEmpty()) {
                                list.add(ChatUiMessage(
                                    id = "user_${msg.id}",
                                    content = msg.input,
                                    isUser = true
                                ))
                            }
                            if (!msg.output.isNullOrEmpty()) {
                                list.add(ChatUiMessage(
                                    id = "ai_${msg.id}",
                                    content = msg.output,
                                    isUser = false
                                ))
                            }
                            list
                        } ?: emptyList()
                        _uiState.update {
                            it.copy(isLoading = false, messages = msgs, currentChatId = chatId)
                        }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun startNewChat(categorySlug: String) {
        viewModelScope.launch {
            chatRepository.startNewChat(categorySlug).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentChatId = result.data?.id,
                                messages = emptyList()
                            )
                        }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun sendMessage(message: String, categorySlug: String) {
        if (message.isBlank()) return

        val userMessage = ChatUiMessage(
            id = "user_${System.currentTimeMillis()}",
            content = message,
            isUser = true
        )
        val aiMessageId = "ai_${System.currentTimeMillis()}"
        val aiMessagePlaceholder = ChatUiMessage(
            id = aiMessageId,
            content = "",
            isUser = false,
            isStreaming = true
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + aiMessagePlaceholder,
                isStreaming = true,
                inputText = ""
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken().first()
                val chatId = _uiState.value.currentChatId

                // Step 1: POST to save message and receive conver_id + message_id
                val postBodyBuilder = okhttp3.FormBody.Builder()
                    .add("prompt", message)
                if (chatId != null) {
                    postBodyBuilder.add("conver_id", chatId)
                }
                val postRequest = Request.Builder()
                    .url("${BuildConfig.BASE_URL}api/aichat/chat-send")
                    .post(postBodyBuilder.build())
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .build()

                val postResponse = okHttpClient.newCall(postRequest).execute()
                if (!postResponse.isSuccessful) {
                    _uiState.update { state ->
                        val updatedMessages = state.messages.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(content = "Error: Unable to get response", isStreaming = false)
                            } else msg
                        }
                        state.copy(messages = updatedMessages, isStreaming = false, errorMessage = "Failed to send message")
                    }
                    return@launch
                }

                val postJson = com.google.gson.JsonParser.parseString(postResponse.body!!.string()).asJsonObject
                val converId = postJson.get("conver_id").asString
                val messageId = postJson.get("message_id").asString

                // Update chatId if this was a new chat
                if (chatId == null) {
                    _uiState.update { it.copy(currentChatId = converId) }
                }

                // Step 2: GET streaming response using conver_id and message_id
                val getRequest = Request.Builder()
                    .url("${BuildConfig.BASE_URL}api/aichat/chat-send?conver_id=$converId&message_id=$messageId")
                    .get()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "text/event-stream")
                    .build()

                val getResponse = okHttpClient.newCall(getRequest).execute()
                if (getResponse.isSuccessful) {
                    val source: BufferedSource = getResponse.body!!.source()
                    val sb = StringBuilder()

                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        // Server sends raw text chunks, ends with "data: [DONE]"
                        if (line.trimEnd() == "data: [DONE]") break
                        if (line.isNotEmpty()) {
                            sb.append(line)
                            val currentContent = sb.toString()
                            _uiState.update { state ->
                                val updatedMessages = state.messages.map { msg ->
                                    if (msg.id == aiMessageId) {
                                        msg.copy(content = currentContent)
                                    } else msg
                                }
                                state.copy(messages = updatedMessages)
                            }
                        }
                    }

                    // Finalize streaming
                    _uiState.update { state ->
                        val updatedMessages = state.messages.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(content = sb.toString(), isStreaming = false)
                            } else msg
                        }
                        state.copy(messages = updatedMessages, isStreaming = false)
                    }
                } else {
                    _uiState.update { state ->
                        val updatedMessages = state.messages.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(content = "Error: Unable to get response", isStreaming = false)
                            } else msg
                        }
                        state.copy(messages = updatedMessages, isStreaming = false, errorMessage = "Failed to get response")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    val updatedMessages = state.messages.map { msg ->
                        if (msg.id == aiMessageId) {
                            msg.copy(content = "Error: ${e.message}", isStreaming = false)
                        } else msg
                    }
                    state.copy(messages = updatedMessages, isStreaming = false, errorMessage = e.message)
                }
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId).collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { state ->
                        state.copy(
                            conversations = state.conversations.filter { it.id != chatId },
                            currentChatId = if (state.currentChatId == chatId) null else state.currentChatId,
                            messages = if (state.currentChatId == chatId) emptyList() else state.messages
                        )
                    }
                }
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
