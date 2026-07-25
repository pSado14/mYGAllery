package com.sado.mygallery.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlbumDetailScreen(
    albumId: Long,
    albumName: String,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
    galleryViewModel: GalleryViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val images by viewModel.images.collectAsState()
    val isOrganizing by galleryViewModel.isOrganizing.collectAsState()
    val context = LocalContext.current

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedUris by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedUris.size} Seçildi") },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedUris = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "İptal")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                if (selectedUris.size == images.size) {
                                    selectedUris = emptySet()
                                } else {
                                    selectedUris = images.map { it.uri.toString() }.toSet()
                                }
                            }
                        ) {
                            Text(if (selectedUris.size == images.size) "Hiçbiri" else "Tümünü Seç", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(albumName) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (isSelectionMode && selectedUris.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val selectedImageList = images.filter { selectedUris.contains(it.uri.toString()) }
                        galleryViewModel.organizeSelectedImages(selectedImageList) {
                            isSelectionMode = false
                            selectedUris = emptySet()
                            android.widget.Toast.makeText(context, "Otomatik düzenleme tamamlandı!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    text = { Text("Yapay Zeka ile Düzenle") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (images.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bu albümde henüz fotoğraf yok.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(images) { image ->
                        val isSelected = selectedUris.contains(image.uri.toString())
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            val newSet = selectedUris.toMutableSet()
                                            if (isSelected) newSet.remove(image.uri.toString()) else newSet.add(image.uri.toString())
                                            selectedUris = newSet
                                            if (newSet.isEmpty()) isSelectionMode = false
                                        } else {
                                            onImageClick(image.uri.toString())
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedUris = setOf(image.uri.toString())
                                        }
                                    }
                                )
                        ) {
                            AsyncImage(
                                model = image.uri,
                                contentDescription = "Album Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            if (isSelectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        val newSet = selectedUris.toMutableSet()
                                        if (checked) newSet.add(image.uri.toString()) else newSet.remove(image.uri.toString())
                                        selectedUris = newSet
                                        if (newSet.isEmpty()) isSelectionMode = false
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            if (isOrganizing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Yapay Zeka ile Düzenleniyor...", color = Color.White)
                    }
                }
            }
        }
    }
}
