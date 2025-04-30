package no.difi.vefa.validator.source;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IArtifactsSourceInstance;
import no.difi.vefa.validator.lang.VefaValidatorException;

/**
 * Defines a repository as source for validation artifacts.
 */
public class RepositorySource extends AbstractArtifactsSource
{

  private final List <URI> rootUri;

  /**
   * Helper method to allow using string when initiating the new source.
   *
   * @param uris
   *        Uri used to fetch validation artifacts.
   */
  public RepositorySource (final String... uris)
  {
    rootUri = new ArrayList <> (uris.length);
    for (final String uri : uris)
      rootUri.add (URI.create (uri));
  }

  /**
   * Initiate the new source.
   *
   * @param uri
   *        Uri used to fetch validation artifacts.
   */
  public RepositorySource (final URI... uri)
  {
    this.rootUri = Arrays.asList (uri);
  }

  public RepositorySource (final List <URI> uris)
  {
    this.rootUri = uris;
  }

  @Override
  public IArtifactsSourceInstance createInstance (final IProperties properties) throws VefaValidatorException
  {
    return new RepositorySourceInstance (properties, rootUri);
  }

  public static RepositorySource forTest ()
  {
    return create ("https://anskaffelser.dev/repo/validator/draft/");
  }

  public static RepositorySource forProduction ()
  {
    return create ("https://anskaffelser.dev/repo/validator/current/");
  }

  public static RepositorySource of (final String... uris)
  {
    return new RepositorySource (uris);
  }

  static RepositorySource create (final String uri)
  {
    return new RepositorySource (uri);
  }
}
