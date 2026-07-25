package com.sado.mygallery.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sado.mygallery.ui.detail.ImageDetailScreen
import com.sado.mygallery.ui.gallery.GalleryScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "gallery") {
        composable("gallery") {
            GalleryScreen(
                onImageClick = { uriString ->
                    val encodedUri = Uri.encode(uriString)
                    navController.navigate("detail/$encodedUri")
                },
                onAlbumClick = { albumId, albumName ->
                    val encodedName = Uri.encode(albumName)
                    navController.navigate("album_detail/$albumId/$encodedName")
                },
                onSettingsClick = {
                    navController.navigate("rules")
                },
                onTrashClick = {
                    navController.navigate("trash")
                }
            )
        }
        composable("rules") {
            com.sado.mygallery.ui.rules.RulesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("trash") {
            com.sado.mygallery.ui.trash.TrashScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = "detail/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("uri") ?: ""
            ImageDetailScreen(
                uriString = Uri.decode(uri),
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = "album_detail/{albumId}/{albumName}",
            arguments = listOf(
                navArgument("albumId") { type = NavType.LongType },
                navArgument("albumName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
            val albumName = Uri.decode(backStackEntry.arguments?.getString("albumName") ?: "")
            com.sado.mygallery.ui.gallery.AlbumDetailScreen(
                albumId = albumId,
                albumName = albumName,
                onBackClick = { navController.popBackStack() },
                onImageClick = { uriString ->
                    val encodedUri = Uri.encode(uriString)
                    navController.navigate("detail/$encodedUri")
                }
            )
        }
    }
}
