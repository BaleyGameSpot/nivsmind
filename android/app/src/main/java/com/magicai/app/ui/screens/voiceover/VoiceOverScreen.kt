package com.magicai.app.ui.screens.voiceover

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicai.app.data.api.ApiService
import com.magicai.app.data.models.TTSRequest
import com.magicai.app.ui.components.GradientButton
import com.magicai.app.ui.components.MagicTopBar
import com.magicai.app.ui.theme.Purple600
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceOverUiState(
    val isLoading: Boolean = false,
    val text: String = "",
    val selectedVoice: String = "alloy",
    val speed: Float = 1.0f,
    val generatedAudioUrl: String? = null,
    val isPlaying: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class VoiceOverViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceOverUiState())
    val uiState: StateFlow<VoiceOverUiState> = _uiState.asStateFlow()

    val voices = listOf("alloy", "echo", "fable", "onyx", "nova", "shimmer")

    fun generateVoiceOver() {
        val text = _uiState.value.text
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter text to convert") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = apiService.generateVoiceOver(
                    TTSRequest(
                        text = text,
                        voice = _uiState.value.selectedVoice,
                        speed = _uiState.value.speed
                    )
                )
                if (response.isSuccessful) {
                    val audioUrl = response.body()?.path
                    _uiState.update { it.copy(isLoading = false, generatedAudioUrl = audioUrl) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Generation failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateText(text: String) = _uiState.update { it.copy(text = text) }
    fun updateVoice(voice: String) = _uiState.update { it.copy(selectedVoice = voice) }
    fun updateSpeed(speed: Float) = _uiState.update { it.copy(speed = speed) }
    fun setPlaying(playing: Boolean) = _uiState.update { it.copy(isPlaying = playing) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceOverScreen(
    onBack: () -> Unit,
    viewModel: VoiceOverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    Scaffold(
        topBar = { MagicTopBar(title = "Voice Over (TTS)", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Purple600.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Purple600)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Convert text to natural-sounding speech using AI voices",
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            OutlinedTextField(
                value = uiState.text,
                onValueChange = viewModel::updateText,
                label = { Text("Enter text to convert") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                minLines = 4,
                maxLines = 10,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple600),
                supportingText = { Text("${uiState.text.length} characters") }
            )

            // Voice Selection
            Text("Select Voice", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.voices.chunked(3).forEach { rowVoices ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowVoices.forEach { voice ->
                            FilterChip(
                                selected = uiState.selectedVoice == voice,
                                onClick = { viewModel.updateVoice(voice) },
                                label = { Text(voice.replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (uiState.selectedVoice == voice) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                        // Fill remaining slots
                        repeat(3 - rowVoices.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Speed Control
            Column {
                Text(
                    "Speed: ${String.format("%.1f", uiState.speed)}x",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = uiState.speed,
                    onValueChange = viewModel::updateSpeed,
                    valueRange = 0.25f..4.0f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = Purple600, activeTrackColor = Purple600)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0.25x", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("4.0x", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

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
                text = "Generate Voice Over",
                onClick = { viewModel.generateVoiceOver() },
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading
            )

            // Audio Player
            if (uiState.generatedAudioUrl != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Generated Audio",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Play / Pause Button
                            FilledIconButton(
                                onClick = {
                                    if (uiState.isPlaying) {
                                        mediaPlayer?.pause()
                                        viewModel.setPlaying(false)
                                    } else {
                                        if (mediaPlayer == null) {
                                            mediaPlayer = MediaPlayer().apply {
                                                setDataSource(uiState.generatedAudioUrl)
                                                prepare()
                                                setOnCompletionListener { viewModel.setPlaying(false) }
                                            }
                                        }
                                        mediaPlayer?.start()
                                        viewModel.setPlaying(true)
                                    }
                                },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Purple600)
                            ) {
                                Icon(
                                    if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Stop Button
                            FilledIconButton(
                                onClick = {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    viewModel.setPlaying(false)
                                },
                                modifier = Modifier.size(56.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Voice: ${uiState.selectedVoice.replaceFirstChar { it.uppercase() }} | Speed: ${uiState.speed}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
