package com.sado.mygallery.di;

import com.sado.mygallery.data.local.GalleryDatabase;
import com.sado.mygallery.data.local.RuleDao;
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
public final class DatabaseModule_ProvideRuleDaoFactory implements Factory<RuleDao> {
  private final Provider<GalleryDatabase> databaseProvider;

  public DatabaseModule_ProvideRuleDaoFactory(Provider<GalleryDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public RuleDao get() {
    return provideRuleDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideRuleDaoFactory create(
      Provider<GalleryDatabase> databaseProvider) {
    return new DatabaseModule_ProvideRuleDaoFactory(databaseProvider);
  }

  public static RuleDao provideRuleDao(GalleryDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideRuleDao(database));
  }
}
