package com.sado.mygallery.ai;

import android.content.Context;
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
public final class AiAnalyzer_Factory implements Factory<AiAnalyzer> {
  private final Provider<Context> contextProvider;

  public AiAnalyzer_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AiAnalyzer get() {
    return newInstance(contextProvider.get());
  }

  public static AiAnalyzer_Factory create(Provider<Context> contextProvider) {
    return new AiAnalyzer_Factory(contextProvider);
  }

  public static AiAnalyzer newInstance(Context context) {
    return new AiAnalyzer(context);
  }
}
