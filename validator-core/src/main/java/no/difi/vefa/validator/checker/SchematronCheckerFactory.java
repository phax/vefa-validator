package no.difi.vefa.validator.checker;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.io.resource.ClassPathResource;
import com.helger.schematron.sch.SchematronProviderXSLTFromSCH;

import net.sf.saxon.lib.ErrorReporterToListener;
import net.sf.saxon.lib.ResourceResolverWrappingURIResolver;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import no.difi.vefa.validator.ValidatorFactory;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IArtifactHolder;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.ClasspathURIResolver;
import no.difi.vefa.validator.util.VefaSaxonErrorListener;
import no.difi.vefa.validator.util.VefaSaxonMessageListener;

/**
 * Implementation performing step 3 (compilation) of Schematron.
 *
 * @author erlend
 */
@Type (".sch")
public class SchematronCheckerFactory implements ICheckerFactory
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SchematronCheckerFactory.class);

  private static final XsltExecutable SCH_COMPILER;
  static
  {
    LOGGER.info ("Compiling Schematron Compiler XSLT");
    try (InputStream inputStream = ClassPathResource.getInputStream (SchematronProviderXSLTFromSCH.XSLT2_STEP3,
                                                                     SchematronProviderXSLTFromSCH.class.getClassLoader ()))
    {
      final XsltCompiler xsltCompiler = ValidatorFactory.SAXON_PROCESSOR.newXsltCompiler ();
      xsltCompiler.setResourceResolver (new ResourceResolverWrappingURIResolver (new ClasspathURIResolver (SchematronProviderXSLTFromSCH.SCHEMATRON_DIRECTORY_XSLT2,
                                                                                                           SchematronProviderXSLTFromSCH.class.getClassLoader ())));
      SCH_COMPILER = xsltCompiler.compile (new StreamSource (inputStream));
    }
    catch (final Exception e)
    {
      throw new IllegalStateException ("Unable to load parsing of Schematron.", e);
    }
  }

  @Override
  public IChecker prepare (final IArtifactHolder artifactHolder, final String path) throws VefaValidatorException
  {
    LOGGER.info ("Performing Schematron compilation on '" + path + "'");
    try (final InputStream inputStream = artifactHolder.getInputStream (path))
    {
      final XdmDestination destination = new XdmDestination ();

      final XsltTransformer xsltTransformer = SCH_COMPILER.load ();
      xsltTransformer.setErrorListener (VefaSaxonErrorListener.INSTANCE);
      xsltTransformer.setMessageHandler (VefaSaxonMessageListener.INSTANCE);
      xsltTransformer.setSource (new StreamSource (inputStream));
      xsltTransformer.setDestination (destination);
      xsltTransformer.transform ();

      final XsltCompiler xsltCompiler = ValidatorFactory.SAXON_PROCESSOR.newXsltCompiler ();
      xsltCompiler.setErrorReporter (new ErrorReporterToListener (VefaSaxonErrorListener.INSTANCE));

      return new SchematronXsltChecker (xsltCompiler.compile (destination.getXdmNode ().asSource ()));
    }
    catch (final Exception e)
    {
      throw new VefaValidatorException (e.getMessage (), e);
    }
  }
}
