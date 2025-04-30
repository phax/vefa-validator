package no.difi.vefa.validator.source;

import java.nio.file.Path;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IArtifactsSourceInstance;
import no.difi.vefa.validator.lang.VefaValidatorException;

/**
 * Defines a directories as source for validation artifacts.
 */
public class DirectorySource extends AbstractArtifactsSource
{
  private final Path [] m_aDirectories;

  /**
   * Initiate the new source.
   *
   * @param directories
   *        Directories containing validation artifacts.
   */
  public DirectorySource (final Path... directories)
  {
    this.m_aDirectories = directories;
  }

  @Override
  public IArtifactsSourceInstance createInstance (final IProperties properties) throws VefaValidatorException
  {
    return new DirectorySourceInstance (properties, m_aDirectories);
  }
}
