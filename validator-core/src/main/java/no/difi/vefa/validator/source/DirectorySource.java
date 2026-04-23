package no.difi.vefa.validator.source;

import java.nio.file.Path;

import org.jspecify.annotations.NonNull;

import no.difi.vefa.validator.api.IArtifactsSourceInstance;
import no.difi.vefa.validator.api.IProperties;
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
    m_aDirectories = directories;
  }

  @NonNull
  public IArtifactsSourceInstance createInstance (final IProperties properties) throws VefaValidatorException
  {
    return new DirectorySourceInstance (properties, m_aDirectories);
  }
}
