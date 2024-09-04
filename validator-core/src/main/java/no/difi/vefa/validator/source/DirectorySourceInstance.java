package no.difi.vefa.validator.source;

import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.asic.IAsicReader;

import jakarta.xml.bind.Unmarshaller;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.xsd.vefa.validator._1.ArtifactType;
import no.difi.xsd.vefa.validator._1.Artifacts;

/**
 * Defines a directory as source for validation artifacts.
 */
class DirectorySourceInstance extends AbstractSourceInstance
{
  private static final Logger log = LoggerFactory.getLogger (DirectorySourceInstance.class);

  /**
   * Constructor, loads validation artifacts into memory.
   *
   * @param directories
   *        Directories containing validation artifacts.
   */
  public DirectorySourceInstance (final IProperties properties, final Path... directories) throws ValidatorException
  {
    // Call #AbstractSourceInstance().
    super (properties);

    try
    {
      for (final Path directory : directories)
      {
        log.info ("Directory: {}", directory);

        // Directories containing artifacts.xml results in lower memory
        // footprint.
        if (Files.exists (directory.resolve ("artifacts.xml")))
        {
          // Create unmarshaller (XML => Java)
          final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();

          // Read artifacts.xml
          final Path artifactsPath = directory.resolve ("artifacts.xml");
          log.info ("Loading {}", artifactsPath);
          Artifacts artifactsType;

          try (InputStream inputStream = Files.newInputStream (artifactsPath))
          {
            artifactsType = unmarshaller.unmarshal (new StreamSource (inputStream), Artifacts.class).getValue ();
          }

          // Loop through artifacts.
          for (final ArtifactType artifact : artifactsType.getArtifact ())
          {
            // Load validation artifact to memory.
            final Path artifactPath = directory.resolve (artifact.getFilename ());
            log.info ("Loading {}", artifactPath);
            try (IAsicReader asicReader = ASIC_READER_FACTORY.open (artifactPath))
            {
              unpackContainer (asicReader, artifact.getFilename ());
            }
          }
        }
        else
        {
          // Detect all ASiC-E-files in the directory.
          try (DirectoryStream <Path> directoryStream = Files.newDirectoryStream (directory))
          {
            for (final Path path : directoryStream)
            {
              if (path.toString ().endsWith (".asice"))
              {
                log.info ("Loading: {}", path);
                try (IAsicReader asicReader = ASIC_READER_FACTORY.open (path))
                {
                  unpackContainer (asicReader, path.getFileName ().toString ());
                }
              }
            }
          }
        }
      }
    }
    catch (final Exception e)
    {
      // Log and throw ValidatorException.
      log.warn (e.getMessage ());
      throw new ValidatorException (e.getMessage (), e);
    }
  }
}
