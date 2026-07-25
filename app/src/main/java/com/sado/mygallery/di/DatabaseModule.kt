package com.sado.mygallery.di

import android.content.Context
import androidx.room.Room
import com.sado.mygallery.data.local.AlbumDao
import com.sado.mygallery.data.local.GalleryDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGalleryDatabase(@ApplicationContext context: Context): GalleryDatabase {
        return Room.databaseBuilder(
            context,
            GalleryDatabase::class.java,
            "gallery_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideAlbumDao(database: GalleryDatabase): AlbumDao {
        return database.albumDao()
    }

    @Provides
    @Singleton
    fun provideRuleDao(database: GalleryDatabase): com.sado.mygallery.data.local.RuleDao {
        return database.ruleDao()
    }
    
    @Provides
    @Singleton
    fun provideFavoriteDao(database: GalleryDatabase): com.sado.mygallery.data.local.FavoriteDao {
        return database.favoriteDao()
    }
}
