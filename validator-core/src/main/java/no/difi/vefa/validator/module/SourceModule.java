package no.difi.vefa.validator.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.ISource;
import no.difi.vefa.validator.api.ISourceInstance;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.source.RepositorySource;

/**
 * @author erlend
 */
public class SourceModule extends AbstractModule
{
  private final ISource m_aSource;

  public SourceModule ()
  {
    this (null);
  }

  public SourceModule (final ISource source)
  {
    m_aSource = source;
  }

  @Provides
  @Singleton
  public ISourceInstance getSource (final IProperties properties) throws VefaValidatorException
  {
    // Make sure to default to repository source if no source is set.
    return (m_aSource != null ? m_aSource : RepositorySource.forProduction ()).createInstance (properties);
  }
}
