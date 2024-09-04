package no.difi.vefa.validator.source;

import java.nio.file.Path;

import no.difi.vefa.validator.api.Properties;
import no.difi.vefa.validator.api.SourceInstance;
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
  public SourceInstance createInstance (final Properties properties) throws ValidatorException
  {
    return new DirectorySourceInstance (properties, directories);
  }
}
