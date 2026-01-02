package com.example.magiceightball.core.domain.policy;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "KotlinInternalInJava",
    "cast"
})
public final class DefaultPromptPolicy_Factory implements Factory<DefaultPromptPolicy> {
  @Override
  public DefaultPromptPolicy get() {
    return newInstance();
  }

  public static DefaultPromptPolicy_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DefaultPromptPolicy newInstance() {
    return new DefaultPromptPolicy();
  }

  private static final class InstanceHolder {
    private static final DefaultPromptPolicy_Factory INSTANCE = new DefaultPromptPolicy_Factory();
  }
}
