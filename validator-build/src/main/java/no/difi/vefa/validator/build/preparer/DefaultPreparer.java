package no.difi.vefa.validator.build.preparer;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IPreparer;
import no.difi.vefa.validator.build.util.PreparerProvider;

@Type (PreparerProvider.DEFAULT)
public class DefaultPreparer implements IPreparer
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DefaultPreparer.class);

  public void prepare (final Path source, final Path target, final EPreparerType type) throws IOException
  {
    LOGGER.info ("Copying '" + source.toString () + "' to '" + target.toString () + "' using " + type);
    Files.copy (source.toFile (), target.toFile ());
  }
}
