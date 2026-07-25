package com.sado.mygallery.data;

import android.content.Context;
import com.sado.mygallery.data.local.AlbumDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class GalleryRepository_Factory implements Factory<GalleryRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<AlbumDao> albumDaoProvider;

  public GalleryRepository_Factory(Provider<Context> contextProvider,
      Provider<AlbumDao> albumDaoProvider) {
    this.contextProvider = contextProvider;
    this.albumDaoProvider = albumDaoProvider;
  }

  @Override
  public GalleryRepository get() {
    return newInstance(contextProvider.get(), albumDaoProvider.get());
  }

  public static GalleryRepository_Factory create(Provider<Context> contextProvider,
      Provider<AlbumDao> albumDaoProvider) {
    return new GalleryRepository_Factory(contextProvider, albumDaoProvider);
  }

  public static GalleryRepository newInstance(Context context, AlbumDao albumDao) {
    return new GalleryRepository(context, albumDao);
  }
}
