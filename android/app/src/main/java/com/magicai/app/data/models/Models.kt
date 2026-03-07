package com.magicai.app.data.models

import com.google.gson.annotations.SerializedName

// ========================= AUTH MODELS =========================

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    @SerializedName("affiliate_code") val affiliateCode: String? = null
)

data class AuthResponse(
    val token: String?,
    val type: String? = "Bearer",
    val message: String? = null,
    val error: Any? = null,
    val status: String? = null
)

data class ForgotPasswordRequest(
    val email: String
)

data class GoogleLoginRequest(
    val token: String
)

// ========================= USER MODELS =========================

data class User(
    val id: Int,
    val name: String,
    val surname: String?,
    val email: String,
    val avatar: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("email_verified_at") val emailVerifiedAt: String?,
    @SerializedName("affiliate_code") val affiliateCode: String?,
    val plan: Plan? = null
)

data class UserUpdateRequest(
    val name: String,
    val surname: String,
    val email: String? = null,
    val phone: String? = null,
    val country: String? = null
)

// ========================= PLAN MODELS =========================

data class Plan(
    val id: Int,
    val name: String,
    val price: Double?,
    val description: String?,
    @SerializedName("ai_words") val aiWords: Long?,
    @SerializedName("ai_images") val aiImages: Int?,
    @SerializedName("storage") val storage: Long?,
    @SerializedName("plan_type") val planType: String?,
    val features: List<String>? = null
)

data class PlanResponse(
    val plans: List<Plan>?,
    val activePlan: ActivePlan?
)

data class ActivePlan(
    val id: Int,
    val name: String,
    @SerializedName("remaining_words") val remainingWords: Long?,
    @SerializedName("remaining_images") val remainingImages: Int?,
    @SerializedName("total_words") val totalWords: Long?,
    @SerializedName("total_images") val totalImages: Int?
)

// ========================= USAGE MODELS =========================

data class UsageData(
    @SerializedName("total_words") val totalWords: Long?,
    @SerializedName("total_images") val totalImages: Int?,
    @SerializedName("remaining_words") val remainingWords: Long?,
    @SerializedName("remaining_images") val remainingImages: Int?,
    @SerializedName("plan_name") val planName: String?,
    @SerializedName("plan_type") val planType: String?
)

// ========================= CHAT MODELS =========================

data class ChatCategory(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String?,
    val image: String?,
    val color: String?,
    @SerializedName("short_name") val shortName: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("instructions") val instructions: String?
)

data class ChatConversation(
    val id: String,
    val title: String?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("openai_chat_category_id") val categoryId: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class ChatMessage(
    val id: Int?,
    val input: String?,
    val output: String?,
    val role: String?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("openai_chat_id") val chatId: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class NewChatRequest(
    @SerializedName("category") val category: String,
    @SerializedName("title") val title: String? = null
)

data class ChatSendRequest(
    @SerializedName("chat_id") val chatId: String?,
    val message: String,
    @SerializedName("category_slug") val categorySlug: String? = null,
    val images: List<String>? = null
)

data class StartChatResponse(
    val id: String,
    val title: String?,
    @SerializedName("openai_chat_category_id") val categoryId: Int?,
    @SerializedName("user_id") val userId: Int?
)

data class ChangeTitleRequest(
    @SerializedName("chat_id") val chatId: String,
    val title: String
)

data class DeleteChatRequest(
    @SerializedName("chat_id") val chatId: String
)

data class RenameChatRequest(
    @SerializedName("chat_id") val chatId: String,
    val title: String
)

// ========================= CHAT TEMPLATE MODELS =========================

data class ChatTemplate(
    val id: Int?,
    val name: String?,
    val instructions: String?,
    val role: String?,
    @SerializedName("is_default") val isDefault: Boolean? = false
)

// ========================= AI WRITER MODELS =========================

data class OpenAIGenerator(
    val id: Int,
    val title: String,
    val description: String?,
    val slug: String,
    val color: String?,
    val image: String?,
    val type: String?,
    @SerializedName("active") val active: Boolean?,
    @SerializedName("short_name") val shortName: String?
)

data class GenerateRequest(
    val openai_id: Int,
    val title: String? = null,
    val description: String? = null,
    val keywords: String? = null,
    val tone: String? = null,
    val language: String? = null,
    val maximum_length: Int? = 500,
    val number_of_results: Int? = 1
)

data class GeneratedDocument(
    val id: Int,
    val title: String?,
    val input: String?,
    val output: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("openai_id") val openaiId: Int?
)

// ========================= AI IMAGE MODELS =========================

data class ImageGenerateRequest(
    val prompt: String,
    @SerializedName("negative_prompt") val negativePrompt: String? = null,
    val type: String? = "generate",
    val style: String? = null,
    val size: String? = "1024x1024",
    val quality: String? = "standard",
    @SerializedName("number_of_images") val numberOfImages: Int? = 1
)

data class GeneratedImage(
    val id: Int?,
    val url: String?,
    val storage: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("openai_id") val openaiId: Int?
)

data class ImageResponse(
    val images: List<GeneratedImage>?,
    val status: String?
)

// ========================= VOICE OVER MODELS =========================

data class TTSRequest(
    val text: String,
    val voice: String,
    val speed: Float? = 1.0f,
    val language: String? = null
)

data class TTSResponse(
    val path: String?,
    val status: String?,
    val message: String?
)

// ========================= DOCUMENTS MODELS =========================

data class Document(
    val id: Int,
    val title: String?,
    val input: String?,
    val output: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("generator") val generator: OpenAIGenerator?
)

data class DocumentsResponse(
    val docs: List<Document>?,
    val total: Int?
)

// ========================= SUPPORT MODELS =========================

data class SupportTicket(
    val id: Int,
    val subject: String?,
    val status: String?,
    val priority: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("last_message") val lastMessage: SupportMessage?
)

data class SupportMessage(
    val id: Int,
    val message: String?,
    @SerializedName("is_admin") val isAdmin: Boolean?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("user_id") val userId: Int?
)

data class NewTicketRequest(
    val subject: String,
    val message: String,
    val priority: String = "medium"
)

data class SendMessageRequest(
    @SerializedName("ticket_id") val ticketId: Int,
    val message: String
)

// ========================= APP SETTINGS MODELS =========================

data class AppSettings(
    @SerializedName("site_name") val siteName: String?,
    @SerializedName("site_description") val siteDescription: String?,
    val logo: String?,
    @SerializedName("email_confirmation") val emailConfirmation: String?,
    @SerializedName("registration_active") val registrationActive: Boolean?,
    @SerializedName("google_active") val googleActive: Boolean?,
    @SerializedName("apple_active") val appleActive: Boolean?
)

// ========================= AFFILIATE MODELS =========================

data class AffiliateData(
    @SerializedName("affiliate_code") val affiliateCode: String?,
    @SerializedName("total_earnings") val totalEarnings: Double?,
    @SerializedName("pending_earnings") val pendingEarnings: Double?,
    @SerializedName("paid_earnings") val paidEarnings: Double?,
    @SerializedName("total_referrals") val totalReferrals: Int?
)

data class WithdrawalRequest(
    val amount: Double,
    val method: String,
    val details: String
)

// ========================= GENERIC RESPONSES =========================

data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val status: String? = null,
    val error: Any? = null
)

data class MessageResponse(
    val message: String?,
    val status: String?,
    val error: Any? = null
)

data class SearchRequest(
    val query: String
)

data class RecentDocumentsResponse(
    val documents: List<Document>?
)
