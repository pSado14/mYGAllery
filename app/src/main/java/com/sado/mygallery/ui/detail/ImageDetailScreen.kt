package com.sado.mygallery.ui.detail

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

import android.widget.Toast
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    uriString: String,
    onBackClick: () -> Unit,
    viewModel: ImageDetailViewModel = hiltViewModel()
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val albums by viewModel.albums.collectAsState()
    var showAlbumSheet by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Zoom state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resim Detayı") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showAlbumSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Albüme Ekle")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            // No padding so the image goes under the app bar
        ) {
            AsyncImage(
                model = Uri.parse(uriString),
                contentDescription = "Detaylı Resim",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformableState)
            )

            // Bottom AI Section
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = analysisState) {
                    is AnalysisState.Idle -> {
                        Button(onClick = { viewModel.analyzeImage(uriString) }) {
                            Text("Yapay Zeka ile Analiz Et")
                        }
                    }
                    is AnalysisState.Loading -> {
                        CircularProgressIndicator(color = Color.White)
                        Text("Resim inceleniyor...", color = Color.White, modifier = Modifier.padding(top = 8.dp))
                    }
                    is AnalysisState.Success -> {
                        Text(
                            text = "Yüz Sayısı: ${state.faceCount}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            state.labels.take(5).forEach { label ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(label) },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.analyzeImage(uriString) }) {
                            Text("Tekrar Analiz Et")
                        }
                    }
                    is AnalysisState.Error -> {
                        Text(text = "Hata: ${state.message}", color = Color.Red)
                        Button(onClick = { viewModel.analyzeImage(uriString) }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            }
        }
        
        if (showAlbumSheet) {
            ModalBottomSheet(onDismissRequest = { showAlbumSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Hangi albüme eklensin?",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (albums.isEmpty()) {
                        Text("Henüz bir albümünüz yok. Önce albüm oluşturun.")
                        Spacer(modifier = Modifier.height(32.dp))
                    } else {
                        albums.forEach { album ->
                            TextButton(
                                onClick = {
                                    viewModel.addToAlbum(album.id, uriString)
                                    showAlbumSheet = false
                                    Toast.makeText(context, "${album.name} albümüne eklendi", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(album.name, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
