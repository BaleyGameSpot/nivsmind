package com.magicai.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magicai.app.ui.components.GradientButton
import com.magicai.app.ui.components.MagicTextField
import com.magicai.app.ui.theme.Purple600
import com.magicai.app.ui.theme.Slate900
import com.magicai.app.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.clearSuccess()
            onRegisterSuccess()
        }
    }

    fun validate(): Boolean {
        nameError = name.isBlank()
        emailError = email.isBlank() || !email.contains("@")
        passwordError = password.length < 8 || password != confirmPassword
        return !nameError && !emailError && !passwordError
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate900, Color(0xFF1E1B4B))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Purple600.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("M", color = Purple600, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Join MagicAI and unleash creativity",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MagicTextField(
                            value = name,
                            onValueChange = { name = it; nameError = false },
                            label = "First Name",
                            leadingIcon = Icons.Default.Person,
                            isError = nameError,
                            errorMessage = if (nameError) "Required" else null,
                            modifier = Modifier.weight(1f)
                        )
                        MagicTextField(
                            value = surname,
                            onValueChange = { surname = it },
                            label = "Last Name",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MagicTextField(
                        value = email,
                        onValueChange = { email = it; emailError = false },
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        isError = emailError,
                        errorMessage = if (emailError) "Enter valid email" else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MagicTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = false },
                        label = "Password",
                        isPassword = true,
                        leadingIcon = Icons.Default.Lock,
                        isError = passwordError,
                        errorMessage = if (passwordError && password.length < 8) "Min 8 characters" else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MagicTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        isPassword = true,
                        leadingIcon = Icons.Default.Lock,
                        isError = passwordError && password != confirmPassword,
                        errorMessage = if (passwordError && password != confirmPassword) "Passwords don't match" else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    GradientButton(
                        text = "Create Account",
                        onClick = {
                            viewModel.clearError()
                            if (validate()) {
                                viewModel.register(name, surname, email, password, confirmPassword)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = uiState.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text("Already have an account? ", color = Color.White.copy(alpha = 0.6f))
                Text(
                    "Sign In",
                    color = Purple600,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
