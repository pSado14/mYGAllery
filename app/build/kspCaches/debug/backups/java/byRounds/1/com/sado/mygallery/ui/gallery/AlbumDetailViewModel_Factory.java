package com.sado.mygallery.ui.gallery;

import androidx.lifecycle.SavedStateHandle;
import com.sado.mygallery.data.GalleryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AlbumDetailViewModel_Factory implements Factory<AlbumDetailViewModel> {
  private final Provider<GalleryRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AlbumDetailViewModel_Factory(Provider<GalleryRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AlbumDetailViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static AlbumDetailViewModel_Factory create(Provider<GalleryRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AlbumDetailViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static AlbumDetailViewModel newInstance(GalleryRepository repository,
      SavedStateHandle savedStateHandle) {
    return new AlbumDetailViewModel(repository, savedStateHandle);
  }
}
