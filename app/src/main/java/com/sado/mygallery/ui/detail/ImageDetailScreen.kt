package com.sado.mygallery.ui.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sado.mygallery.utils.StorageManager

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
    var showAiSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Zoom state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Fotoğraf silindi", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
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
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Black.copy(alpha = 0.8f),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(uriString))
                            type = "image/*"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Fotoğrafı Paylaş"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Paylaş", tint = Color.White)
                    }
                    
                    IconButton(onClick = { showAiSheet = true }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Yapay Zeka Analizi", tint = Color.White)
                    }

                    IconButton(onClick = {
                        val intentSender = StorageManager.getTrashIntentSender(context, listOf(Uri.parse(uriString)))
                        if (intentSender != null) {
                            deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        } else {
                            Toast.makeText(context, "Silme işlemi bu cihazda desteklenmiyor", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = Color.Red)
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Use padding to prevent bars overlapping with the image
                .padding(paddingValues)
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
        }
        
        if (showAiSheet) {
            ModalBottomSheet(onDismissRequest = { showAiSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Yapay Zeka Analizi", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    when (val state = analysisState) {
                        is AnalysisState.Idle -> {
                            Button(onClick = { viewModel.analyzeImage(uriString) }) {
                                Text("Resmi İncele")
                            }
                        }
                        is AnalysisState.Loading -> {
                            CircularProgressIndicator()
                            Text("İnceleniyor...", modifier = Modifier.padding(top = 8.dp))
                        }
                        is AnalysisState.Success -> {
                            Text("Yüz Sayısı: ${state.faceCount}", style = MaterialTheme.typography.titleMedium)
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
                    Spacer(modifier = Modifier.height(32.dp))
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
