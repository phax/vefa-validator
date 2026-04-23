package no.difi.vefa.validator.checker;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.stream.StreamHelper;

import net.sf.saxon.lib.ErrorReporterToListener;
import net.sf.saxon.s9api.XsltCompiler;
import no.difi.vefa.validator.ValidatorFactory;
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
  private static final Logger LOGGER = LoggerFactory.getLogger (SchematronXsltCheckerFactory.class);

  @Override
  public IChecker prepare (final IArtifactHolder artifactHolder, final String path) throws VefaValidatorException
  {
    try (final InputStream inputStream = artifactHolder.getInputStream (path))
    {
      final XsltCompiler xsltCompiler = ValidatorFactory.SAXON_PROCESSOR.newXsltCompiler ();
      xsltCompiler.setErrorReporter (new ErrorReporterToListener (VefaSaxonErrorListener.INSTANCE));

      final IChecker checker;
      if (false)
      {
        // Add some debug logging
        final byte [] aContent = StreamHelper.getAllBytes (inputStream);
        LOGGER.info ("Compiling: " + new String (aContent, StandardCharsets.UTF_8));
        checker = new SchematronXsltChecker (xsltCompiler.compile (new StreamSource (new NonBlockingByteArrayInputStream (aContent))));
      }
      else
      {
        checker = new SchematronXsltChecker (xsltCompiler.compile (new StreamSource (inputStream)));
      }
      return checker;
    }
    catch (final Exception e)
    {
      throw new VefaValidatorException (e.getMessage (), e);
    }
  }
}
