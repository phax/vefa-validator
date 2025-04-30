package no.difi.vefa.validator.source;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.asic.IAsicReader;

import jakarta.xml.bind.Unmarshaller;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.xsd.vefa.validator._1.ArtifactType;
import no.difi.xsd.vefa.validator._1.Artifacts;

class RepositorySourceInstance extends AbstractArtifactsSourceInstance
{
  private static final Logger log = LoggerFactory.getLogger (RepositorySourceInstance.class);

  public RepositorySourceInstance (final IProperties properties, final List <URI> rootUris)
                                                                                            throws VefaValidatorException
  {
    super (properties);

    try
    {
      for (final URI rootUri : rootUris)
      {
        final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();
        final URI artifactsUri = rootUri.resolve ("artifacts.xml");
        log.info ("Fetching repo " + artifactsUri);
        final Artifacts artifactsType = (Artifacts) unmarshaller.unmarshal (artifactsUri.toURL ());

        for (final ArtifactType artifact : artifactsType.getArtifact ())
        {
          final URI artifactUri = rootUri.resolve (artifact.getFilename ());
          log.info ("  Unpacking " + artifactUri);
          try (IAsicReader asicReader = ASIC_READER_FACTORY.open (artifactUri.toURL ().openStream ()))
          {
            unpackAsic (asicReader, artifact.getFilename ());
          }
        }
      }
    }
    catch (final Exception e)
    {
      log.warn (e.getMessage (), e);
      throw new VefaValidatorException (e.getMessage (), e);
    }
  }
}
