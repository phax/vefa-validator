package no.difi.vefa.validator.module;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import no.difi.vefa.validator.ValidatorDefaults;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.properties.CombinedProperties;

/**
 * @author erlend
 */
public class PropertiesModule extends AbstractModule
{
  private final IProperties m_aProps;

  public PropertiesModule ()
  {
    this (null);
  }

  public PropertiesModule (@Nullable final IProperties properties)
  {
    m_aProps = properties;
  }

  @Provides
  @Singleton
  @NonNull
  public IProperties getProperties ()
  {
    // Create config combined with default values.
    return new CombinedProperties (m_aProps, ValidatorDefaults.PROPERTIES);
  }
}
