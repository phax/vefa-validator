package no.difi.vefa.validator.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import no.difi.vefa.validator.api.IArtifactsSource;
import no.difi.vefa.validator.api.IArtifactsSourceInstance;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.source.RepositorySource;

/**
 * @author erlend
 */
public class SourceModule extends AbstractModule
{
  private final IArtifactsSource m_aSource;

  public SourceModule ()
  {
    this (null);
  }

  public SourceModule (final IArtifactsSource source)
  {
    m_aSource = source;
  }

  @Provides
  @Singleton
  public IArtifactsSourceInstance getSource (final IProperties properties) throws VefaValidatorException
  {
    // Make sure to default to repository source if no source is set.
    return (m_aSource != null ? m_aSource : RepositorySource.forProduction ()).createInstance (properties);
  }
}
