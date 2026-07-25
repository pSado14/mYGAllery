package com.sado.mygallery.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Album::class, AlbumImageCrossRef::class, Rule::class],
    version = 2,
    exportSchema = false
)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun ruleDao(): RuleDao
}
