package com.magicai.app.ui.screens.subscription

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicai.app.BuildConfig
import com.magicai.app.data.api.ApiService
import com.magicai.app.data.models.Plan
import com.magicai.app.ui.components.LoadingScreen
import com.magicai.app.ui.components.MagicTopBar
import com.magicai.app.ui.theme.Purple600
import com.magicai.app.ui.theme.Purple700
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val plans: List<Plan> = emptyList(),
    val currentPlanId: Int? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getPlans()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, plans = response.body() ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load plans") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPeriod by remember { mutableStateOf("monthly") }

    Scaffold(
        topBar = { MagicTopBar(title = "Subscription Plans", onBack = onBack) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingScreen()
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Purple600, Purple700))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Choose Your Plan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Upgrade to unlock unlimited AI features",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Period Toggle
                    Row(
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(24.dp)
                        ).padding(4.dp)
                    ) {
                        listOf("monthly", "yearly").forEach { period ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (selectedPeriod == period) Purple600 else Color.Transparent
                                    )
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .then(
                                        Modifier.clickableNoRipple { selectedPeriod = period }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    period.replaceFirstChar { it.uppercase() },
                                    color = if (selectedPeriod == period) Color.White else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (uiState.plans.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(48.dp), tint = Purple600)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Contact us for pricing information", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                items(uiState.plans) { plan ->
                    val context = LocalContext.current
                    PlanCard(
                        plan = plan,
                        isCurrentPlan = plan.id == uiState.currentPlanId,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        onSubscribe = {
                            val url = "${BuildConfig.BASE_URL}plans/${plan.id}/subscribe"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlanCard(plan: Plan, isCurrentPlan: Boolean, modifier: Modifier = Modifier, onSubscribe: () -> Unit = {}) {
    val isPopular = plan.name.contains("Pro", ignoreCase = true) ||
            plan.name.contains("Premium", ignoreCase = true)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = if (isPopular) CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(Purple600, Purple700))
        ) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isPopular) Purple600.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (plan.planType != null) {
                        Text(plan.planType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (isPopular) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Purple600),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Popular", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                if (isCurrentPlan) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Current", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "$${plan.price ?: 0}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Purple600
                )
                Text(
                    "/month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Features
            val featureList = buildList {
                plan.aiWords?.let { add("${formatNumber(it)} AI Words") }
                plan.aiImages?.let { add("$it AI Images") }
                plan.description?.let { if (it.isNotEmpty()) add(it) }
            }

            featureList.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feature, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isCurrentPlan) {
                Button(
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPopular) Purple600 else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isPopular) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Get Started", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> "${number / 1_000_000}M"
        number >= 1_000 -> "${number / 1_000}K"
        else -> number.toString()
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit) = this.clickable(
    interactionSource = MutableInteractionSource(),
    indication = null,
    onClick = onClick
)
