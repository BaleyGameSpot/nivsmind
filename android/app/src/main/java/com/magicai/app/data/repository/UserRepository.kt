package com.magicai.app.data.repository

import com.magicai.app.data.api.ApiService
import com.magicai.app.data.models.*
import com.magicai.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getProfile(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to load profile"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun updateProfile(request: UserUpdateRequest): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateProfile(request)
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getUsageData(): Flow<Resource<UsageData>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getUsageData()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to load usage data"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getPlans(): Flow<Resource<List<Plan>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getPlans()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body() ?: emptyList()))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to load plans"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getSupportTickets(): Flow<Resource<List<SupportTicket>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getSupportTickets()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body() ?: emptyList()))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to load tickets"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getTicketMessages(ticketId: Int): Flow<Resource<SupportTicket>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTicket(ticketId)
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to load ticket"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun createTicket(subject: String, message: String, priority: String = "medium"): Flow<Resource<SupportTicket>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createTicket(NewTicketRequest(subject, message, priority))
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to create ticket"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun sendSupportMessage(ticketId: Int, message: String): Flow<Resource<SupportMessage>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.sendSupportMessage(SendMessageRequest(ticketId, message))
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to send message"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getAffiliates(): Flow<Resource<AffiliateData>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getAffiliates()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to load affiliate data"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }
}
