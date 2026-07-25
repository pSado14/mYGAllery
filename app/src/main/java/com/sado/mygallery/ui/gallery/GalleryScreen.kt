package com.sado.mygallery.ui.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = hiltViewModel(),
    onImageClick: (String) -> Unit,
    onAlbumClick: (Long, String) -> Unit,
    onSettingsClick: () -> Unit = {},
    onTrashClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    var hasPermission by remember {
        mutableStateOf(
            permissionsToRequest.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        hasPermission = permissionsMap.values.all { it }
    }

    // Android 11+ All Files Access Check
    var hasAllFilesAccess by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.os.Environment.isExternalStorageManager()
            } else {
                true
            }
        )
    }

    LaunchedEffect(hasPermission, hasAllFilesAccess) {
        if (!hasPermission) {
            launcher.launch(permissionsToRequest)
        } else if (!hasAllFilesAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(intent)
            }
        }
        
        if (hasPermission && hasAllFilesAccess) {
            viewModel.loadImages()
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Fotoğraflar", "Albümler", "Temizle")
    
    // Selection state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedUris by remember { mutableStateOf(setOf<String>()) }
    val isOrganizing by viewModel.isOrganizing.collectAsState()
    val images by viewModel.images.collectAsState()

    if (hasPermission) {
        val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                if (isSelectionMode) {
                    androidx.compose.material3.TopAppBar(
                        title = { Text("${selectedUris.size} Seçildi") },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = {
                                isSelectionMode = false
                                selectedUris = emptySet()
                            }) {
                                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "İptal")
                            }
                        },
                        actions = {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    if (selectedUris.size == images.size) {
                                        selectedUris = emptySet()
                                    } else {
                                        selectedUris = images.map { it.uri.toString() }.toSet()
                                    }
                                }
                            ) {
                                Text(if (selectedUris.size == images.size) "Hiçbiri" else "Tümünü Seç", color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            }
                        },
                        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                } else {
                    androidx.compose.material3.TopAppBar(
                        title = { Text("Galeri", style = MaterialTheme.typography.titleLarge) },
                        actions = {
                            IconButton(onClick = onSettingsClick) {
                                Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                            }
                        },
                        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    )
                }
            },
            bottomBar = {
                if (isSelectionMode) {
                    BottomAppBar(
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
                                    val selectedImageList = images.filter { selectedUris.contains(it.uri.toString()) }
                                    viewModel.organizeSelectedImages(selectedImageList) {
                                        isSelectionMode = false
                                        selectedUris = emptySet()
                                        android.widget.Toast.makeText(context, "Otomatik düzenleme tamamlandı!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }.padding(8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Yapay Zeka ile Düzenle")
                                Text("Düzenle", style = MaterialTheme.typography.labelSmall)
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    // Normally this would delete, but we just trigger the onTrashClick logic or a toast
                                    onTrashClick()
                                }.padding(8.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                                Text("Sil", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        androidx.compose.material3.Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            tonalElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(48.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { selectedTabIndex = 0 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Photo, 
                                        contentDescription = "Fotoğraflar",
                                        tint = if (selectedTabIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                IconButton(
                                    onClick = { selectedTabIndex = 1 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoAlbum, 
                                        contentDescription = "Albümler",
                                        tint = if (selectedTabIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                IconButton(
                                    onClick = { selectedTabIndex = 2 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CleaningServices, 
                                        contentDescription = "Temizle",
                                        tint = if (selectedTabIndex == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (selectedTabIndex) {
                    0 -> GalleryContent(
                        viewModel = viewModel, 
                        onImageClick = onImageClick,
                        isSelectionMode = isSelectionMode,
                        selectedUris = selectedUris,
                        onSelectionChange = { uri, isSelected ->
                            val newSet = selectedUris.toMutableSet()
                            if (isSelected) newSet.add(uri) else newSet.remove(uri)
                            selectedUris = newSet
                            if (newSet.isEmpty()) isSelectionMode = false
                        },
                        onLongPress = { uri ->
                            isSelectionMode = true
                            selectedUris = setOf(uri)
                        }
                    )
                    1 -> AlbumsScreen(onAlbumClick = onAlbumClick)
                    2 -> CleanerScreen()
                }
                
                if (isOrganizing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Yapay Zeka ile Düzenleniyor...", color = Color.White)
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Fotoğrafları görmek için izne ihtiyacımız var.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { launcher.launch(permissionsToRequest) }) {
                Text(text = "İzin Ver")
            }
        }
    }
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun GalleryContent(
    viewModel: GalleryViewModel, 
    onImageClick: (String) -> Unit,
    isSelectionMode: Boolean,
    selectedUris: Set<String>,
    onSelectionChange: (String, Boolean) -> Unit,
    onLongPress: (String) -> Unit
) {
    val images by viewModel.images.collectAsState()
    
    // Group images by date
    val groupedImages = remember(images) {
        images.groupBy { image ->
            val date = java.util.Date(image.dateAdded * 1000L)
            val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("tr"))
            sdf.format(date)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4), // Xiaomi style typically uses 4 columns
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp), // Padding for bottom bar
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        groupedImages.forEach { (dateStr, imagesInDate) ->
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            items(imagesInDate) { image ->
                val isSelected = selectedUris.contains(image.uri.toString())
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (isSelectionMode) {
                                    onSelectionChange(image.uri.toString(), !isSelected)
                                } else {
                                    onImageClick(image.uri.toString())
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    onLongPress(image.uri.toString())
                                }
                            }
                        )
                ) {
                    AsyncImage(
                        model = image.uri,
                        contentDescription = "Gallery Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (isSelectionMode) {
                        // Xiaomi style subtle overlay for unselected, checkmark for selected
                        if (isSelected) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                        }
                        
                        androidx.compose.material3.Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                onSelectionChange(image.uri.toString(), checked)
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
}
