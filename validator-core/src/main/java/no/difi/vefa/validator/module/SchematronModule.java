package no.difi.vefa.validator.module;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.helger.io.resource.ClassPathResource;
import com.helger.schematron.sch.SchematronProviderXSLTFromSCH;

import net.sf.saxon.lib.ResourceResolverWrappingURIResolver;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import no.difi.vefa.validator.ValidatorFactory;
import no.difi.vefa.validator.checker.SchematronXsltChecker;
import no.difi.vefa.validator.util.ClasspathURIResolver;

/**
 * @author erlend
 */
public class SchematronModule extends AbstractModule
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SchematronXsltChecker.class);

  @Provides
  @Named ("schematron-step3")
  @Singleton
  public XsltExecutable getSchematronCompiler ()
  {
    LOGGER.info ("Compiling Schemtron Compiler");
    try (InputStream inputStream = ClassPathResource.getInputStream (SchematronProviderXSLTFromSCH.XSLT2_STEP3,
                                                                     SchematronProviderXSLTFromSCH.class.getClassLoader ()))
    {
      final XsltCompiler xsltCompiler = ValidatorFactory.SAXON_PROCESSOR.newXsltCompiler ();
      xsltCompiler.setResourceResolver (new ResourceResolverWrappingURIResolver (new ClasspathURIResolver (SchematronProviderXSLTFromSCH.SCHEMATRON_DIRECTORY_XSLT2,
                                                                                                           SchematronProviderXSLTFromSCH.class.getClassLoader ())));
      return xsltCompiler.compile (new StreamSource (inputStream));
    }
    catch (final Exception e)
    {
      throw new IllegalStateException ("Unable to load parsing of Schematron.", e);
    }
  }
}
