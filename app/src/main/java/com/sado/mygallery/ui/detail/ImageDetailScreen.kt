package com.sado.mygallery.ui.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.sado.mygallery.utils.StorageManager
import com.sado.mygallery.utils.MediaInfo
import com.sado.mygallery.utils.MediaInfoUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImageDetailScreen(
    uriString: String,
    onBackClick: () -> Unit,
    viewModel: ImageDetailViewModel = hiltViewModel()
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val allImages by viewModel.images.collectAsState()
    
    var showAlbumSheet by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var mediaInfo by remember { mutableStateOf<MediaInfo?>(null) }
    var isExiting by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val handleBack = {
        isExiting = true
        onBackClick()
    }

    // Determine initial page
    var hasNavigatedToInitial by remember { mutableStateOf(false) }
    
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { allImages.size.coerceAtLeast(1) }
    )

    LaunchedEffect(allImages) {
        if (!hasNavigatedToInitial && allImages.isNotEmpty()) {
            val index = allImages.indexOfFirst { it.uri.toString() == uriString }
            if (index != -1) {
                pagerState.scrollToPage(index)
            }
            hasNavigatedToInitial = true
        }
    }

    val currentUriString = if (allImages.isNotEmpty()) {
        allImages[pagerState.currentPage].uri.toString()
    } else {
        uriString
    }
    
    val currentImage = allImages.find { it.uri.toString() == currentUriString }
    val isFavorite = currentImage?.isFavorite == true
    val isVideo = currentImage?.isVideo == true

    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Medya silindi", Toast.LENGTH_SHORT).show()
            if (allImages.size <= 1) {
                handleBack()
            }
        }
    }

    BackHandler(onBack = handleBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
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
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(currentUriString))
                            val mime = context.contentResolver.getType(Uri.parse(currentUriString)) ?: "image/*"
                            type = mime
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Medyayı Paylaş"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Paylaş", tint = Color.White)
                    }
                    
                    IconButton(onClick = { viewModel.toggleFavorite(currentUriString) }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                    
                    IconButton(onClick = { showAiSheet = true }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Yapay Zeka Analizi", tint = Color.White)
                    }
                    
                    IconButton(onClick = {
                        coroutineScope.launch {
                            mediaInfo = MediaInfoUtils.getMediaInfo(context, Uri.parse(currentUriString), isVideo)
                            showInfoSheet = true
                        }
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "Detaylar", tint = Color.White)
                    }

                    IconButton(onClick = {
                        val intentSender = StorageManager.getTrashIntentSender(context, listOf(Uri.parse(currentUriString)))
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
        
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 2,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            
            val mediaUriString = if (allImages.isNotEmpty()) allImages[page].uri.toString() else uriString
            val mimeType = remember(mediaUriString) { context.contentResolver.getType(Uri.parse(mediaUriString)) ?: "" }
            val isVideo = mimeType.startsWith("video/")

            if (isVideo) {
                val exoPlayer = remember { 
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(mediaUriString))
                        prepare()
                    }
                }
                
                // Play only if it is the current page
                LaunchedEffect(pagerState.currentPage) {
                    if (pagerState.currentPage == page) {
                        exoPlayer.playWhenReady = true
                    } else {
                        exoPlayer.pause()
                    }
                }
                
                if (!isExiting) {
                    AndroidView(
                        factory = {
                            PlayerView(context).apply {
                                player = exoPlayer
                                useController = true
                                setKeepContentOnPlayerReset(true)
                            }
                        },
                        onRelease = {
                            it.player?.release()
                            it.player = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                me.saket.telephoto.zoomable.coil.ZoomableAsyncImage(
                    model = coil.request.ImageRequest.Builder(context)
                        .data(Uri.parse(mediaUriString))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Detaylı Resim",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
                            Button(onClick = { viewModel.analyzeImage(currentUriString) }) {
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
                            Button(onClick = { viewModel.analyzeImage(currentUriString) }) {
                                Text("Tekrar Analiz Et")
                            }
                        }
                        is AnalysisState.Error -> {
                            Text(text = "Hata: ${state.message}", color = Color.Red)
                            Button(onClick = { viewModel.analyzeImage(currentUriString) }) {
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
                                    viewModel.addToAlbum(album.id, currentUriString)
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

    if (showInfoSheet && mediaInfo != null) {
        ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Medya Detayları", style = MaterialTheme.typography.titleLarge)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(mediaInfo!!.date, style = MaterialTheme.typography.bodyLarge)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DataUsage, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(mediaInfo!!.sizeMb, style = MaterialTheme.typography.bodyLarge)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AspectRatio, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(mediaInfo!!.resolution, style = MaterialTheme.typography.bodyLarge)
                }
                
                if (mediaInfo!!.cameraModel != "Bilinmiyor") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(mediaInfo!!.cameraModel, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
