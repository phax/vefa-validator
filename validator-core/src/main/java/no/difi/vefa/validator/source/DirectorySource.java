package no.difi.vefa.validator.source;

import java.nio.file.Path;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.ISourceInstance;
import no.difi.vefa.validator.lang.ValidatorException;

/**
 * Defines a directories as source for validation artifacts.
 */
public class DirectorySource extends AbstractSource
{

  private final Path [] directories;

  /**
   * Initiate the new source.
   *
   * @param directories
   *        Directories containing validation artifacts.
   */
  public DirectorySource (final Path... directories)
  {
    this.directories = directories;
  }

  @Override
  public ISourceInstance createInstance (final IProperties properties) throws ValidatorException
  {
    return new DirectorySourceInstance (properties, directories);
  }
}
