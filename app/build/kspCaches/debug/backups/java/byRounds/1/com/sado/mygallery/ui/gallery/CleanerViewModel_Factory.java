package com.sado.mygallery.ui.gallery;

import android.app.Application;
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
public final class CleanerViewModel_Factory implements Factory<CleanerViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<GalleryRepository> repositoryProvider;

  public CleanerViewModel_Factory(Provider<Application> applicationProvider,
      Provider<GalleryRepository> repositoryProvider) {
    this.applicationProvider = applicationProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CleanerViewModel get() {
    return newInstance(applicationProvider.get(), repositoryProvider.get());
  }

  public static CleanerViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<GalleryRepository> repositoryProvider) {
    return new CleanerViewModel_Factory(applicationProvider, repositoryProvider);
  }

  public static CleanerViewModel newInstance(Application application,
      GalleryRepository repository) {
    return new CleanerViewModel(application, repository);
  }
}
