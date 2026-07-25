package com.sado.mygallery.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sado.mygallery.data.local.Album

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewModel = hiltViewModel(),
    galleryViewModel: GalleryViewModel = hiltViewModel(),
    onAlbumClick: (Long, String) -> Unit
) {
    val albums by viewModel.albums.collectAsState()
    val allImages by galleryViewModel.images.collectAsState()
    val isOrganizing by galleryViewModel.isOrganizing.collectAsState()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }
    
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedAlbumIds by remember { mutableStateOf(setOf<Long>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedAlbumIds.size} Albüm Seçildi") },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedAlbumIds = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "İptal")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                if (selectedAlbumIds.size == albums.size) {
                                    selectedAlbumIds = emptySet()
                                } else {
                                    selectedAlbumIds = albums.map { it.id }.toSet()
                                }
                            }
                        ) {
                            Text(if (selectedAlbumIds.size == albums.size) "Hiçbiri" else "Tümünü Seç", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(albums) { album ->
                    val isSelected = selectedAlbumIds.contains(album.id)
                    AlbumItem(
                        album = album,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) {
                                val newSet = selectedAlbumIds.toMutableSet()
                                if (isSelected) newSet.remove(album.id) else newSet.add(album.id)
                                selectedAlbumIds = newSet
                                if (newSet.isEmpty()) isSelectionMode = false
                            } else {
                                onAlbumClick(album.id, album.name)
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedAlbumIds = setOf(album.id)
                            }
                        },
                        onSelectionChange = { checked ->
                            val newSet = selectedAlbumIds.toMutableSet()
                            if (checked) newSet.add(album.id) else newSet.remove(album.id)
                            selectedAlbumIds = newSet
                            if (newSet.isEmpty()) isSelectionMode = false
                        }
                    )
                }
            }
        }
        
        if (isSelectionMode && selectedAlbumIds.isNotEmpty()) {
            BottomAppBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            val selectedNames = albums.filter { selectedAlbumIds.contains(it.id) }.map { it.name }.toSet()
                            val imagesToOrganize = allImages.filter { selectedNames.contains(it.albumName) }
                            
                            galleryViewModel.organizeSelectedImages(imagesToOrganize) {
                                isSelectionMode = false
                                selectedAlbumIds = emptySet()
                                android.widget.Toast.makeText(context, "Albümler otomatik düzenlendi!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }.padding(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Yapay Zeka ile Düzenle")
                        Text("Düzenle", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Normally this would delete the albums, just a placeholder for UI consistency
                            android.widget.Toast.makeText(context, "Albüm silme işlemi yapılmadı.", android.widget.Toast.LENGTH_SHORT).show()
                            isSelectionMode = false
                            selectedAlbumIds = emptySet()
                        }.padding(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                        Text("Sil", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        } else if (!isSelectionMode) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Album")
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
                    Text("Albümler Düzenleniyor...", color = Color.White)
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Yeni Albüm") },
            text = {
                OutlinedTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it },
                    label = { Text("Albüm Adı") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newAlbumName.isNotBlank()) {
                            viewModel.createAlbum(newAlbumName)
                            showCreateDialog = false
                            newAlbumName = ""
                        }
                    }
                ) {
                    Text("Oluştur")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumItem(
    album: Album, 
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectionChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (album.coverImageUri != null) {
                AsyncImage(
                    model = album.coverImageUri,
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            if (isSelectionMode) {
                if (isSelected) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                }
                
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChange,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = album.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
