package com.sado.mygallery.ui.detail;

import com.sado.mygallery.ai.AiAnalyzer;
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
public final class ImageDetailViewModel_Factory implements Factory<ImageDetailViewModel> {
  private final Provider<AiAnalyzer> aiAnalyzerProvider;

  private final Provider<GalleryRepository> repositoryProvider;

  public ImageDetailViewModel_Factory(Provider<AiAnalyzer> aiAnalyzerProvider,
      Provider<GalleryRepository> repositoryProvider) {
    this.aiAnalyzerProvider = aiAnalyzerProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ImageDetailViewModel get() {
    return newInstance(aiAnalyzerProvider.get(), repositoryProvider.get());
  }

  public static ImageDetailViewModel_Factory create(Provider<AiAnalyzer> aiAnalyzerProvider,
      Provider<GalleryRepository> repositoryProvider) {
    return new ImageDetailViewModel_Factory(aiAnalyzerProvider, repositoryProvider);
  }

  public static ImageDetailViewModel newInstance(AiAnalyzer aiAnalyzer,
      GalleryRepository repository) {
    return new ImageDetailViewModel(aiAnalyzer, repository);
  }
}
