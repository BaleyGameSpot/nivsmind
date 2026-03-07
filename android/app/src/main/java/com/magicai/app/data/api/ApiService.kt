package com.magicai.app.data.api

import com.magicai.app.data.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========================= AUTH =========================

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @GET("api/auth/social-login")
    suspend fun getSupportedLoginMethods(): Response<Map<String, Boolean>>

    @POST("api/auth/google-login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>

    @GET("api/auth/logo")
    suspend fun getLogo(): Response<Map<String, String>>

    // ========================= USER PROFILE =========================

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<User>

    @PATCH("api/auth/profile")
    suspend fun updateProfile(@Body request: UserUpdateRequest): Response<User>

    @DELETE("api/auth/profile")
    suspend fun deleteProfile(): Response<MessageResponse>

    // ========================= APP SETTINGS =========================

    @GET("api/app/get-setting")
    suspend fun getAppSettings(): Response<AppSettings>

    @GET("api/app/usage-data")
    suspend fun getUsageData(): Response<UsageData>

    @GET("api/app/email-confirmation-setting")
    suspend fun getEmailConfirmationSetting(): Response<Map<String, Boolean>>

    // ========================= AI CHAT =========================

    @GET("api/aichat/history/{cat_slug}")
    suspend fun getChatHistory(@Path("cat_slug") catSlug: String): Response<Map<String, Any>>

    @DELETE("api/aichat/history")
    suspend fun deleteChat(@Body request: DeleteChatRequest): Response<MessageResponse>

    @PATCH("api/aichat/history")
    suspend fun renameChat(@Body request: RenameChatRequest): Response<MessageResponse>

    @GET("api/aichat/chat/{conver_id}")
    suspend fun getConversation(@Path("conver_id") converId: String): Response<ChatConversation>

    @GET("api/aichat/chat/{conver_id}/messages")
    suspend fun getConversationMessages(@Path("conver_id") converId: String): Response<List<ChatMessage>>

    @POST("api/aichat/new-chat")
    suspend fun startNewChat(@Body request: NewChatRequest): Response<StartChatResponse>

    @GET("api/aichat/chat-send")
    suspend fun getChatSend(
        @Query("chat_id") chatId: String?,
        @Query("message") message: String,
        @Query("category_slug") categorySlug: String?
    ): Response<ResponseBody>

    @POST("api/aichat/chat-send")
    suspend fun sendChatMessage(@Body request: ChatSendRequest): Response<ResponseBody>

    @PATCH("api/aichat/change-chat-title")
    suspend fun changeChatTitle(@Body request: ChangeTitleRequest): Response<MessageResponse>

    @GET("api/aichat/recent-chats")
    suspend fun getRecentChats(): Response<List<ChatConversation>>

    @POST("api/aichat/search-recent-chats")
    suspend fun searchRecentChats(@Body request: SearchRequest): Response<List<ChatConversation>>

    @POST("api/aichat/search-history")
    suspend fun searchChatHistory(@Body request: SearchRequest): Response<List<ChatConversation>>

    // ========================= CHAT TEMPLATES =========================

    @GET("api/aichat/chat-templates/{id}")
    suspend fun getChatTemplates(@Path("id") id: Int? = null): Response<List<ChatTemplate>>

    @PATCH("api/aichat/chat-templates")
    suspend fun updateChatTemplate(@Body request: ChatTemplate): Response<ChatTemplate>

    @DELETE("api/aichat/chat-templates/{id}")
    suspend fun deleteChatTemplate(@Path("id") id: Int): Response<MessageResponse>

    // ========================= AI WRITER =========================

    @GET("api/aiwriter/openai-list")
    suspend fun getOpenAIWriterList(): Response<List<OpenAIGenerator>>

    @GET("api/aiwriter/generator/{slug}")
    suspend fun getGeneratorInfo(@Path("slug") slug: String): Response<Map<String, Any>>

    @POST("api/aiwriter/generate-output")
    suspend fun generateTextOutput(@Body request: GenerateRequest): Response<ResponseBody>

    @GET("api/aiwriter/favorite-openai-list")
    suspend fun getFavoriteOpenAIList(): Response<List<Int>>

    @POST("api/aiwriter/favorite-openai-list-add")
    suspend fun addToFavorites(@Body body: Map<String, Int>): Response<MessageResponse>

    @POST("api/aiwriter/favorite-openai-list-remove")
    suspend fun removeFromFavorites(@Body body: Map<String, Int>): Response<MessageResponse>

    // ========================= AI IMAGE =========================

    @GET("api/aiimage/versions")
    suspend fun getImageVersions(): Response<Map<String, Any>>

    @POST("api/aiimage/generate-image")
    suspend fun generateImage(@Body request: ImageGenerateRequest): Response<ImageResponse>

    @GET("api/aiimage/recent-images")
    suspend fun getRecentImages(): Response<List<GeneratedImage>>

    // ========================= VOICE OVER (TTS) =========================

    @POST("api/aivoiceover/generate")
    suspend fun generateVoiceOver(@Body request: TTSRequest): Response<TTSResponse>

    @POST("api/aivoiceover/preview")
    suspend fun previewVoiceOver(@Body request: TTSRequest): Response<TTSResponse>

    // ========================= DOCUMENTS =========================

    @GET("api/documents")
    suspend fun getDocuments(): Response<DocumentsResponse>

    @GET("api/documents/doc/{id}")
    suspend fun getDocument(@Path("id") id: Int): Response<Document>

    @POST("api/documents/doc/{id}")
    suspend fun saveDocument(@Path("id") id: Int, @Body body: Map<String, String>): Response<Document>

    @DELETE("api/documents/doc/{id}")
    suspend fun deleteDocument(@Path("id") id: Int): Response<MessageResponse>

    @GET("api/documents/recent")
    suspend fun getRecentDocuments(): Response<List<Document>>

    // ========================= PAYMENT =========================

    @GET("api/payment")
    suspend fun getCurrentPlan(): Response<Map<String, Any>>

    @GET("api/payment/plans")
    suspend fun getPlans(): Response<List<Plan>>

    @GET("api/payment/orders")
    suspend fun getOrders(): Response<List<Map<String, Any>>>

    @POST("api/payment/subscriptions/cancel-current")
    suspend fun cancelSubscription(): Response<MessageResponse>

    // ========================= SUPPORT =========================

    @GET("api/support")
    suspend fun getSupportTickets(): Response<List<SupportTicket>>

    @POST("api/support")
    suspend fun createTicket(@Body request: NewTicketRequest): Response<SupportTicket>

    @GET("api/support/ticket/{ticket_id}")
    suspend fun getTicket(@Path("ticket_id") ticketId: Int): Response<SupportTicket>

    @GET("api/support/ticket/{ticket_id}/last-message")
    suspend fun getTicketLastMessage(@Path("ticket_id") ticketId: Int): Response<SupportMessage>

    @POST("api/support/send-message")
    suspend fun sendSupportMessage(@Body request: SendMessageRequest): Response<SupportMessage>

    // ========================= AFFILIATES =========================

    @GET("api/affiliates")
    suspend fun getAffiliates(): Response<AffiliateData>

    @GET("api/affiliates/withdrawals")
    suspend fun getWithdrawals(): Response<List<Map<String, Any>>>

    @POST("api/affiliates/request-withdrawal")
    suspend fun requestWithdrawal(@Body request: WithdrawalRequest): Response<MessageResponse>

    // ========================= GENERAL =========================

    @GET("api/general/recent-documents")
    suspend fun getRecentDocumentsGeneral(): Response<RecentDocumentsResponse>

    @GET("api/general/favorite-openai")
    suspend fun getFavoriteOpenAI(): Response<List<OpenAIGenerator>>

    @POST("api/general/search")
    suspend fun search(@Body request: SearchRequest): Response<Map<String, Any>>
}
