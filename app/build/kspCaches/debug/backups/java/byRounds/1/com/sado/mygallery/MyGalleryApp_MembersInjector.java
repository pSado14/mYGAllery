package com.sado.mygallery;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MyGalleryApp_MembersInjector implements MembersInjector<MyGalleryApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public MyGalleryApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<MyGalleryApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new MyGalleryApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(MyGalleryApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.sado.mygallery.MyGalleryApp.workerFactory")
  public static void injectWorkerFactory(MyGalleryApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
