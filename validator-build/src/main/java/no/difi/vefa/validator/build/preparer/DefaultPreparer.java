package no.difi.vefa.validator.build.preparer;

import java.io.IOException;
import java.nio.file.Path;

import com.google.common.io.Files;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IPreparer;
import no.difi.vefa.validator.build.util.PreparerProvider;

@Type (PreparerProvider.DEFAULT)
public class DefaultPreparer implements IPreparer
{
  @Override
  public void prepare (final Path source, final Path target, final EType type) throws IOException
  {
    Files.copy (source.toFile (), target.toFile ());
  }
}
