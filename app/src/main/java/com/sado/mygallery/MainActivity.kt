package com.sado.mygallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sado.mygallery.ui.gallery.GalleryScreen
import dagger.hilt.android.AndroidEntryPoint

import com.sado.mygallery.ui.theme.MyGalleryTheme
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.Coil
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
        Coil.setImageLoader(imageLoader)

        setContent {
            MyGalleryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    com.sado.mygallery.ui.navigation.AppNavGraph()
                }
            }
        }
    }
}
