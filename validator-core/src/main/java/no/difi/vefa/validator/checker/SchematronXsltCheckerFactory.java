package no.difi.vefa.validator.checker;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import com.google.inject.Inject;
import com.google.inject.Injector;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltCompiler;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IArtifactHolder;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.VefaSaxonErrorListener;

/**
 * @author erlend
 */
@Type ({ ".xsl", ".xslt", ".svrl.xsl", ".svrl.xslt", ".sch.xslt" })
public class SchematronXsltCheckerFactory implements ICheckerFactory
{
  @Inject
  private Processor processor;

  @Inject
  private Injector injector;

  @Override
  public IChecker prepare (final IArtifactHolder artifactHolder, final String path) throws VefaValidatorException
  {
    try (InputStream inputStream = artifactHolder.getInputStream (path))
    {
      final XsltCompiler xsltCompiler = processor.newXsltCompiler ();
      xsltCompiler.setErrorListener (VefaSaxonErrorListener.INSTANCE);

      final IChecker checker = new SchematronXsltChecker (processor,
                                                          xsltCompiler.compile (new StreamSource (inputStream)));
      injector.injectMembers (checker);
      return checker;

    }
    catch (final Exception e)
    {
      throw new VefaValidatorException (e.getMessage (), e);
    }
  }
}
