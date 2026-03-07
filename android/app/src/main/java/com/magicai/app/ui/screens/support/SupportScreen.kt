package com.magicai.app.ui.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magicai.app.ui.components.EmptyState
import com.magicai.app.ui.components.GradientButton
import com.magicai.app.ui.components.LoadingScreen
import com.magicai.app.ui.components.MagicTopBar
import com.magicai.app.ui.theme.Purple600
import com.magicai.app.viewmodel.SupportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit,
    onNavigateToTicket: (Int) -> Unit,
    viewModel: SupportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNewTicketDialog by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("medium") }

    Scaffold(
        topBar = { MagicTopBar(title = "Support", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewTicketDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Ticket") },
                containerColor = Purple600,
                contentColor = Color.White
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen()
            uiState.tickets.isEmpty() -> {
                EmptyState(
                    title = "No Support Tickets",
                    subtitle = "Create a ticket if you need help with anything",
                    icon = Icons.Default.SupportAgent,
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tickets) { ticket ->
                        Card(
                            onClick = { onNavigateToTicket(ticket.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        ticket.subject ?: "Support Request",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TicketStatusChip(status = ticket.status ?: "open")
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PriorityChip(priority = ticket.priority ?: "medium")
                                    Text(
                                        ticket.createdAt?.take(10) ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                ticket.lastMessage?.message?.let { lastMsg ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        lastMsg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Ticket Dialog
    if (showNewTicketDialog) {
        AlertDialog(
            onDismissRequest = { showNewTicketDialog = false },
            title = { Text("Create Support Ticket") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("low", "medium", "high").forEach { priority ->
                            FilterChip(
                                selected = selectedPriority == priority,
                                onClick = { selectedPriority = priority },
                                label = { Text(priority.replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (subject.isNotBlank() && message.isNotBlank()) {
                        viewModel.createTicket(subject, message, selectedPriority)
                        showNewTicketDialog = false
                        subject = ""
                        message = ""
                    }
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTicketDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TicketStatusChip(status: String) {
    val (color, bg) = when (status.lowercase()) {
        "open" -> Pair(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f))
        "closed" -> Pair(Color(0xFF6B7280), Color(0xFF6B7280).copy(alpha = 0.15f))
        "pending" -> Pair(Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f))
        else -> Pair(Purple600, Purple600.copy(alpha = 0.15f))
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            status.replaceFirstChar { it.uppercase() },
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PriorityChip(priority: String) {
    val (color, bg) = when (priority.lowercase()) {
        "high" -> Pair(Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f))
        "medium" -> Pair(Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f))
        "low" -> Pair(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f))
        else -> Pair(Color(0xFF6B7280), Color(0xFF6B7280).copy(alpha = 0.15f))
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            priority.replaceFirstChar { it.uppercase() },
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
