package com.sado.mygallery.ui.gallery

import android.app.Activity
import android.app.RecoverableSecurityException
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sado.mygallery.data.GalleryImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanerScreen(
    viewModel: CleanerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isScanning by viewModel.isScanning.collectAsState()
    val blurryImages by viewModel.blurryImages.collectAsState()
    val duplicateGroups by viewModel.duplicateGroups.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedUrisToDelete by remember { mutableStateOf(setOf<Uri>()) }

    // Launcher for physical deletion
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Deleted successfully
            selectedUrisToDelete = emptySet()
            viewModel.startScan() // Rescan
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }

    Scaffold(
        bottomBar = {
            if (selectedUrisToDelete.isNotEmpty()) {
                BottomAppBar {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val intentSender = MediaStore.createDeleteRequest(context.contentResolver, selectedUrisToDelete)
                                deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                            } else {
                                // For older versions
                                try {
                                    selectedUrisToDelete.forEach { uri ->
                                        context.contentResolver.delete(uri, null, null)
                                    }
                                    selectedUrisToDelete = emptySet()
                                    viewModel.startScan()
                                } catch (e: RecoverableSecurityException) {
                                    deleteLauncher.launch(IntentSenderRequest.Builder(e.userAction.actionIntent.intentSender).build())
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Seçilenleri Sil (${selectedUrisToDelete.size})")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Yinelenenler (${duplicateGroups.size} Grup)") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Bulanıklar (${blurryImages.size})") }
                )
            }

            if (isScanning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Galeriniz analiz ediliyor...")
                    }
                }
            } else {
                when (selectedTabIndex) {
                    0 -> DuplicatesContent(
                        groups = duplicateGroups,
                        selectedUris = selectedUrisToDelete,
                        onSelectionChange = { uri, selected ->
                            selectedUrisToDelete = if (selected) {
                                selectedUrisToDelete + uri
                            } else {
                                selectedUrisToDelete - uri
                            }
                        }
                    )
                    1 -> BlurryContent(
                        images = blurryImages,
                        selectedUris = selectedUrisToDelete,
                        onSelectionChange = { uri, selected ->
                            selectedUrisToDelete = if (selected) {
                                selectedUrisToDelete + uri
                            } else {
                                selectedUrisToDelete - uri
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DuplicatesContent(
    groups: List<DuplicateGroup>,
    selectedUris: Set<Uri>,
    onSelectionChange: (Uri, Boolean) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groups.forEachIndexed { groupIndex, group ->
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                Text(
                    text = "Grup ${groupIndex + 1} (${group.images.size} fotoğraf)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            items(group.images) { image ->
                val isSelected = selectedUris.contains(image.uri)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectionChange(image.uri, !isSelected) }
                ) {
                    AsyncImage(
                        model = image.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectionChange(image.uri, it) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun BlurryContent(
    images: List<GalleryImage>,
    selectedUris: Set<Uri>,
    onSelectionChange: (Uri, Boolean) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(images) { image ->
            val isSelected = selectedUris.contains(image.uri)
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectionChange(image.uri, !isSelected) }
            ) {
                AsyncImage(
                    model = image.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionChange(image.uri, it) },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}
