package com.sado.mygallery.di;

import android.content.Context;
import com.sado.mygallery.data.local.GalleryDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DatabaseModule_ProvideGalleryDatabaseFactory implements Factory<GalleryDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideGalleryDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GalleryDatabase get() {
    return provideGalleryDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideGalleryDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideGalleryDatabaseFactory(contextProvider);
  }

  public static GalleryDatabase provideGalleryDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideGalleryDatabase(context));
  }
}
