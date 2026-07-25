package com.sado.mygallery.di;

import com.sado.mygallery.data.local.AlbumDao;
import com.sado.mygallery.data.local.GalleryDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DatabaseModule_ProvideAlbumDaoFactory implements Factory<AlbumDao> {
  private final Provider<GalleryDatabase> databaseProvider;

  public DatabaseModule_ProvideAlbumDaoFactory(Provider<GalleryDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AlbumDao get() {
    return provideAlbumDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideAlbumDaoFactory create(
      Provider<GalleryDatabase> databaseProvider) {
    return new DatabaseModule_ProvideAlbumDaoFactory(databaseProvider);
  }

  public static AlbumDao provideAlbumDao(GalleryDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAlbumDao(database));
  }
}
