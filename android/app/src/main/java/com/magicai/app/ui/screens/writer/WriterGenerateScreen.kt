package com.magicai.app.ui.screens.writer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magicai.app.data.api.ApiService
import com.magicai.app.ui.components.GradientButton
import com.magicai.app.ui.components.MagicTextField
import com.magicai.app.ui.components.MagicTopBar
import com.magicai.app.ui.theme.Purple600
import com.magicai.app.viewmodel.AIWriterViewModel

@Composable
fun WriterGenerateScreen(
    slug: String,
    onBack: () -> Unit,
    viewModel: AIWriterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf("professional") }
    var maxLength by remember { mutableStateOf(500) }

    val tones = listOf("professional", "creative", "casual", "formal", "friendly", "humorous")

    // Find generator by slug
    val generator = uiState.generators.find { it.slug == slug }

    Scaffold(
        topBar = {
            MagicTopBar(
                title = generator?.title ?: "Generate Content",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Generator Info
            generator?.description?.let { desc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Purple600.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            MagicTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title / Topic",
                modifier = Modifier.fillMaxWidth()
            )

            MagicTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )

            MagicTextField(
                value = keywords,
                onValueChange = { keywords = it },
                label = "Keywords (comma separated)",
                modifier = Modifier.fillMaxWidth()
            )

            // Tone Selection
            Text("Writing Tone", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tones.take(3).forEach { tone ->
                    FilterChip(
                        selected = selectedTone == tone,
                        onClick = { selectedTone = tone },
                        label = { Text(tone.replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tones.drop(3).forEach { tone ->
                    FilterChip(
                        selected = selectedTone == tone,
                        onClick = { selectedTone = tone },
                        label = { Text(tone.replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Length Slider
            Column {
                Text(
                    "Max Length: $maxLength words",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = maxLength.toFloat(),
                    onValueChange = { maxLength = it.toInt() },
                    valueRange = 100f..2000f,
                    steps = 18,
                    colors = SliderDefaults.colors(thumbColor = Purple600, activeTrackColor = Purple600)
                )
            }

            // Error
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            GradientButton(
                text = "Generate Content",
                onClick = {
                    val gen = uiState.generators.find { it.slug == slug }
                    if (gen != null) {
                        viewModel.generateText(
                            openaiId = gen.id,
                            title = title,
                            description = description,
                            keywords = keywords,
                            tone = selectedTone,
                            maxLength = maxLength
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isGenerating
            )

            // Generated Text Output
            if (uiState.generatedText.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Generated Content",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(uiState.generatedText))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            uiState.generatedText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
                        )
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.clearGeneratedText() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear & Generate Again")
                }
            }
        }
    }
}
