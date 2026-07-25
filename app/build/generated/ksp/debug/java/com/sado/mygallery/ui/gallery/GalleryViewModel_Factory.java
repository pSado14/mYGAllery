package com.sado.mygallery.ui.gallery;

import android.app.Application;
import com.sado.mygallery.ai.AiAnalyzer;
import com.sado.mygallery.data.GalleryRepository;
import com.sado.mygallery.data.local.RuleDao;
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
public final class GalleryViewModel_Factory implements Factory<GalleryViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<GalleryRepository> repositoryProvider;

  private final Provider<AiAnalyzer> aiAnalyzerProvider;

  private final Provider<RuleDao> ruleDaoProvider;

  public GalleryViewModel_Factory(Provider<Application> applicationProvider,
      Provider<GalleryRepository> repositoryProvider, Provider<AiAnalyzer> aiAnalyzerProvider,
      Provider<RuleDao> ruleDaoProvider) {
    this.applicationProvider = applicationProvider;
    this.repositoryProvider = repositoryProvider;
    this.aiAnalyzerProvider = aiAnalyzerProvider;
    this.ruleDaoProvider = ruleDaoProvider;
  }

  @Override
  public GalleryViewModel get() {
    return newInstance(applicationProvider.get(), repositoryProvider.get(), aiAnalyzerProvider.get(), ruleDaoProvider.get());
  }

  public static GalleryViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<GalleryRepository> repositoryProvider, Provider<AiAnalyzer> aiAnalyzerProvider,
      Provider<RuleDao> ruleDaoProvider) {
    return new GalleryViewModel_Factory(applicationProvider, repositoryProvider, aiAnalyzerProvider, ruleDaoProvider);
  }

  public static GalleryViewModel newInstance(Application application, GalleryRepository repository,
      AiAnalyzer aiAnalyzer, RuleDao ruleDao) {
    return new GalleryViewModel(application, repository, aiAnalyzer, ruleDao);
  }
}
