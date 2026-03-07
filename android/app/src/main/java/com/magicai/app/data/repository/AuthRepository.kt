package com.magicai.app.data.repository

import com.magicai.app.data.api.ApiService
import com.magicai.app.data.local.TokenManager
import com.magicai.app.data.models.*
import com.magicai.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun login(email: String, password: String): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token
                if (token != null) {
                    tokenManager.saveToken(token)
                    tokenManager.saveUserEmail(email)
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error("Invalid credentials"))
                }
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun register(
        name: String,
        surname: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        affiliateCode: String? = null
    ): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.register(
                RegisterRequest(name, surname, email, password, passwordConfirmation, affiliateCode)
            )
            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token
                if (token != null) {
                    tokenManager.saveToken(token)
                    tokenManager.saveUserEmail(email)
                    tokenManager.saveUserName("$name $surname")
                }
                emit(Resource.Success(body!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun logout(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            apiService.logout()
            tokenManager.clearToken()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            tokenManager.clearToken()
            emit(Resource.Success(true))
        }
    }

    fun forgotPassword(email: String): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to send reset email"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun googleLogin(token: String): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.googleLogin(GoogleLoginRequest(token))
            if (response.isSuccessful) {
                val body = response.body()
                val authToken = body?.token
                if (authToken != null) {
                    tokenManager.saveToken(authToken)
                }
                emit(Resource.Success(body!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Google login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun isLoggedIn() = tokenManager.isLoggedIn()
}
