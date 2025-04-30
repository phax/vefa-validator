package no.difi.vefa.validator.source;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.asic.IAsicReader;
import com.helger.commons.io.resource.ClassPathResource;

import jakarta.xml.bind.Unmarshaller;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.xsd.vefa.validator._1.ArtifactType;
import no.difi.xsd.vefa.validator._1.Artifacts;

class ClasspathSourceInstance extends AbstractArtifactsSourceInstance
{
  private static final Logger logger = LoggerFactory.getLogger (ClasspathSourceInstance.class);

  public ClasspathSourceInstance (final IProperties properties, final String sFolder) throws VefaValidatorException
  {
    super (properties);

    final String artifactsUri = sFolder + "artifacts.xml";
    try (final InputStream inputStream = ClassPathResource.getInputStream (artifactsUri))
    {
      final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();

      logger.info ("Reading classpath '" + artifactsUri + "'");
      _unpack (sFolder, unmarshaller.unmarshal (new StreamSource (inputStream), Artifacts.class).getValue ());
    }
    catch (final Exception e)
    {
      logger.warn (e.getMessage (), e);
      throw new VefaValidatorException (e.getMessage (), e);
    }
  }

  private void _unpack (final String location, final Artifacts artifactsType) throws IOException
  {
    for (final ArtifactType artifact : artifactsType.getArtifact ())
    {
      final String artifactUri = location + artifact.getFilename ();
      logger.info ("  Unpacking '" + artifactUri + "'");
      try (InputStream inputStream = getClass ().getResourceAsStream (artifactUri);
           IAsicReader asicReader = ASIC_READER_FACTORY.open (inputStream))
      {
        unpackAsic (asicReader, artifact.getFilename ());
      }
    }
  }
}
