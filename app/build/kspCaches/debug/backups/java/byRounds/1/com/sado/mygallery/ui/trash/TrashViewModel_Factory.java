package com.sado.mygallery.ui.trash;

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
public final class TrashViewModel_Factory implements Factory<TrashViewModel> {
  private final Provider<GalleryRepository> repositoryProvider;

  public TrashViewModel_Factory(Provider<GalleryRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TrashViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static TrashViewModel_Factory create(Provider<GalleryRepository> repositoryProvider) {
    return new TrashViewModel_Factory(repositoryProvider);
  }

  public static TrashViewModel newInstance(GalleryRepository repository) {
    return new TrashViewModel(repository);
  }
}
