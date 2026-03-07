package com.magicai.app.ui.screens.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.magicai.app.ui.components.GradientButton
import com.magicai.app.ui.components.MagicTopBar
import com.magicai.app.ui.theme.Purple600
import com.magicai.app.viewmodel.AIImageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIImageScreen(
    onBack: () -> Unit,
    viewModel: AIImageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = { MagicTopBar(title = "AI Image Generator", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Generate") },
                containerColor = Purple600,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(uiState.errorMessage!!, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Generated Images
            if (uiState.generatedImages.isNotEmpty()) {
                Text(
                    "Generated",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(if (uiState.recentImages.isNotEmpty()) 0.4f else 1f)
                ) {
                    items(uiState.generatedImages) { image ->
                        ImageCard(
                            imageUrl = image.url ?: image.storage ?: "",
                            modifier = Modifier.aspectRatio(1f)
                        )
                    }
                }
            }

            // Recent Images
            if (uiState.recentImages.isNotEmpty()) {
                Text(
                    "Recent Images",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.recentImages) { image ->
                        ImageCard(
                            imageUrl = image.url ?: image.storage ?: "",
                            modifier = Modifier.aspectRatio(1f)
                        )
                    }
                }
            }

            if (uiState.recentImages.isEmpty() && uiState.generatedImages.isEmpty() && !uiState.isGenerating) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No images yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tap + Generate to create your first image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            GenerateImageSheet(
                uiState = uiState,
                viewModel = viewModel,
                onGenerate = {
                    viewModel.generateImage()
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun GenerateImageSheet(
    uiState: com.magicai.app.viewmodel.ImageUiState,
    viewModel: AIImageViewModel,
    onGenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Generate Image",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = viewModel::updatePrompt,
            label = { Text("Describe your image") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple600)
        )

        OutlinedTextField(
            value = uiState.negativePrompt,
            onValueChange = viewModel::updateNegativePrompt,
            label = { Text("Negative prompt (optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple600)
        )

        Text("Image Size", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            viewModel.imageSizes.forEach { size ->
                FilterChip(
                    selected = uiState.selectedSize == size,
                    onClick = { viewModel.updateSize(size) },
                    label = { Text(size, style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text("Quality", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            viewModel.imageQualities.forEach { quality ->
                FilterChip(
                    selected = uiState.selectedQuality == quality,
                    onClick = { viewModel.updateQuality(quality) },
                    label = { Text(quality.replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        GradientButton(
            text = "Generate Image",
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth(),
            isLoading = uiState.isGenerating
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ImageCard(imageUrl: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
