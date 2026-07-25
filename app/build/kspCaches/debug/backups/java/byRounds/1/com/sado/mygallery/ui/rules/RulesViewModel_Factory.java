package com.sado.mygallery.ui.rules;

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
public final class RulesViewModel_Factory implements Factory<RulesViewModel> {
  private final Provider<RuleDao> ruleDaoProvider;

  public RulesViewModel_Factory(Provider<RuleDao> ruleDaoProvider) {
    this.ruleDaoProvider = ruleDaoProvider;
  }

  @Override
  public RulesViewModel get() {
    return newInstance(ruleDaoProvider.get());
  }

  public static RulesViewModel_Factory create(Provider<RuleDao> ruleDaoProvider) {
    return new RulesViewModel_Factory(ruleDaoProvider);
  }

  public static RulesViewModel newInstance(RuleDao ruleDao) {
    return new RulesViewModel(ruleDao);
  }
}
