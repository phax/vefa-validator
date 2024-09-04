package no.difi.vefa.validator.source;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.asic.IAsicReader;

import jakarta.xml.bind.Unmarshaller;
import no.difi.vefa.validator.api.Properties;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.xsd.vefa.validator._1.ArtifactType;
import no.difi.xsd.vefa.validator._1.Artifacts;

class ClasspathSourceInstance extends AbstractSourceInstance
{

  private static Logger logger = LoggerFactory.getLogger (ClasspathSourceInstance.class);

  public ClasspathSourceInstance (final Properties properties, final String location) throws ValidatorException
  {
    super (properties);

    final String artifactsUri = location + "artifacts.xml";

    try (InputStream inputStream = getClass ().getResourceAsStream (artifactsUri))
    {
      final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();

      logger.info (String.format ("Fetching %s", artifactsUri));
      unpack (location, unmarshaller.unmarshal (new StreamSource (inputStream), Artifacts.class).getValue ());
    }
    catch (final Exception e)
    {
      logger.warn (e.getMessage (), e);
      throw new ValidatorException (e.getMessage (), e);
    }
  }

  private void unpack (final String location, final Artifacts artifactsType) throws IOException
  {
    for (final ArtifactType artifact : artifactsType.getArtifact ())
    {
      final String artifactUri = location + artifact.getFilename ();
      logger.info (String.format ("Fetching %s", artifactUri));
      try (InputStream inputStream = getClass ().getResourceAsStream (artifactUri);
          IAsicReader asicReader = ASIC_READER_FACTORY.open (inputStream))
      {
        unpackContainer (asicReader, artifact.getFilename ());
      }
    }
  }
}
