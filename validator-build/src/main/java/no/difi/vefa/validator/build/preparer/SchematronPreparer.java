package no.difi.vefa.validator.build.preparer;

import java.io.IOException;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import no.difi.commons.schematron.SchematronCompiler;
import no.difi.commons.schematron.SchematronException;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IPreparer;

@Type ({ ".sch", ".scmt" })
public class SchematronPreparer implements IPreparer
{
  @Inject
  @Named ("compile")
  private Provider <SchematronCompiler> schematronCompile;

  @Inject
  @Named ("prepare")
  private Provider <SchematronCompiler> schematronPrepare;

  @Override
  public void prepare (final Path source, final Path target, final EType type) throws IOException
  {
    try
    {
      if (target.toString ().endsWith (".sch"))
        schematronPrepare.get ().compile (source, target);
      else
        schematronCompile.get ().compile (source, target);
    }
    catch (final SchematronException e)
    {
      throw new IOException ("Unable to handle Schematron.", e);
    }
  }
}
