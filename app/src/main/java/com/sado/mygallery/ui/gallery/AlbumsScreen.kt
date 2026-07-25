package com.sado.mygallery.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp, start = 16.dp, end = 16.dp)
            ) {

                
                // Albums Section Header
                item {
                    SectionHeader(title = "Albümler", trailingIcon = Icons.Default.ChevronRight)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Albums Grid (3 columns)
                val chunkedAlbums = albums.chunked(3)
                items(chunkedAlbums) { rowAlbums ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (album in rowAlbums) {
                            Box(modifier = Modifier.weight(1f)) {
                                val isSelected = selectedAlbumIds.contains(album.id)
                                val albumPhotos = allImages.count { it.albumName == album.name }
                                AlbumItem(
                                    album = album,
                                    photoCount = albumPhotos,
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
                        // Fill empty spots in the last row to keep sizing correct
                        val emptySpots = 3 - rowAlbums.size
                        repeat(emptySpots) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                

                
                // More Section
                item {
                    SectionHeader(title = "Daha fazla", trailingIcon = Icons.Default.ChevronRight)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        MoreListItem(icon = Icons.Default.DeleteOutline, title = "Son silinenler", count = "0")
                        MoreListItem(icon = Icons.Default.Description, title = "Belgeler", count = "2")
                        MoreListItem(icon = Icons.Default.Person, title = "Portre", count = "1")
                        MoreListItem(icon = Icons.Default.Face, title = "Özçekimler", count = "33")
                        MoreListItem(icon = Icons.Default.Gif, title = "GIF", count = "19")
                        MoreListItem(icon = Icons.Default.SlowMotionVideo, title = "Ağır çekim", count = "1")
                        MoreListItem(icon = Icons.Default.HighQuality, title = "Yüksek çözünürlük", count = "4")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { /* Customize */ },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("Özelleştir", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        // Selection Mode Bottom Bar or Add Fab
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
                    .padding(bottom = 100.dp, end = 16.dp),
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

@Composable
fun SectionHeader(title: String, trailingIcon: ImageVector? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (trailingIcon != null) {
            Icon(trailingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PinnedCard(modifier: Modifier = Modifier, title: String, subtitle: String, icon: ImageVector) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun MoreListItem(icon: ImageVector, title: String, count: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(text = count, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumItem(
    album: Album, 
    photoCount: Int,
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
                .clip(RoundedCornerShape(24.dp))
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
                    imageVector = Icons.Default.PhotoAlbum,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(32.dp),
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
                        .padding(4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = album.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = "$photoCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
