package com.sado.mygallery.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Album::class, AlbumImageCrossRef::class, Rule::class, Favorite::class],
    version = 3,
    exportSchema = false
)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun ruleDao(): RuleDao
    abstract fun favoriteDao(): FavoriteDao
}
