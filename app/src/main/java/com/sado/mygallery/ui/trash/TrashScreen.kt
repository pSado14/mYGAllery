package com.sado.mygallery.ui.trash

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sado.mygallery.utils.StorageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val trashedImages by viewModel.trashedImages.collectAsState()
    
    // For selecting single item to restore for simplicity, or multi-select if wanted
    var selectedUriToRestore by remember { mutableStateOf<android.net.Uri?>(null) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loadTrashedImages()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTrashedImages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Çöp Kutusu") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Çöp kutusu özelliği Android 11 ve üzeri cihazlarda desteklenmektedir.")
            }
        } else if (trashedImages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Çöp kutusu boş.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(trashedImages, key = { it.id }) { image ->
                    Box(modifier = Modifier.aspectRatio(1f)) {
                        AsyncImage(
                            model = image.uri,
                            contentDescription = "Görsel",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                val intentSender = StorageManager.getRestoreIntentSender(context, listOf(image.uri))
                                intentSender?.let {
                                    restoreLauncher.launch(IntentSenderRequest.Builder(it).build())
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = "Geri Yükle", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
