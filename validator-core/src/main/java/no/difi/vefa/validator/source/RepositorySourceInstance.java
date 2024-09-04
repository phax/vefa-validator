package no.difi.vefa.validator.source;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.asic.IAsicReader;

import jakarta.xml.bind.Unmarshaller;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.xsd.vefa.validator._1.ArtifactType;
import no.difi.xsd.vefa.validator._1.Artifacts;

class RepositorySourceInstance extends AbstractSourceInstance
{
  private static final Logger log = LoggerFactory.getLogger (RepositorySourceInstance.class);

  public RepositorySourceInstance (final IProperties properties, final List <URI> rootUris) throws ValidatorException
  {
    super (properties);

    try
    {
      for (final URI rootUri : rootUris)
      {
        final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();
        final URI artifactsUri = rootUri.resolve ("artifacts.xml");
        log.info (String.format ("Fetching %s", artifactsUri));
        final Artifacts artifactsType = (Artifacts) unmarshaller.unmarshal (artifactsUri.toURL ());

        for (final ArtifactType artifact : artifactsType.getArtifact ())
        {
          final URI artifactUri = rootUri.resolve (artifact.getFilename ());
          log.info (String.format ("Fetching %s", artifactUri));
          try (IAsicReader asicReader = ASIC_READER_FACTORY.open (artifactUri.toURL ().openStream ()))
          {
            unpackContainer (asicReader, artifact.getFilename ());
          }
        }
      }
    }
    catch (final Exception e)
    {
      log.warn (e.getMessage (), e);
      throw new ValidatorException (e.getMessage (), e);
    }
  }
}
