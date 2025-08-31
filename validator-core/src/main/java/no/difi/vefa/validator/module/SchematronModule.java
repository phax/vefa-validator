package no.difi.vefa.validator.module;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.helger.io.resource.ClassPathResource;
import com.helger.schematron.sch.SchematronProviderXSLTFromSCH;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import no.difi.vefa.validator.util.ClasspathURIResolver;

/**
 * @author erlend
 */
public class SchematronModule extends AbstractModule
{

  @Provides
  @Named ("schematron-step3")
  @Singleton
  public XsltExecutable getSchematronCompiler (final Processor processor)
  {
    try (InputStream inputStream = ClassPathResource.getInputStream (SchematronProviderXSLTFromSCH.XSLT2_STEP3,
                                                                     SchematronProviderXSLTFromSCH.class.getClassLoader ()))
    {
      final XsltCompiler xsltCompiler = processor.newXsltCompiler ();
      xsltCompiler.setURIResolver (new ClasspathURIResolver (SchematronProviderXSLTFromSCH.SCHEMATRON_DIRECTORY_XSLT2,
                                                             SchematronProviderXSLTFromSCH.class.getClassLoader ()));
      return xsltCompiler.compile (new StreamSource (inputStream));
    }
    catch (final Exception e)
    {
      throw new IllegalStateException ("Unable to load parsing of Schematron.", e);
    }
  }

  @Provides
  @Named ("schematron-svrl-parser")
  @Singleton
  public XsltExecutable getSchematronSvrlParser (final Processor processor)
  {
    try (InputStream inputStream = ClassPathResource.getInputStream ("/vefa-validator/xslt/svrl-parser.xslt"))
    {
      final XsltCompiler xsltCompiler = processor.newXsltCompiler ();
      return xsltCompiler.compile (new StreamSource (inputStream));
    }
    catch (final Exception e)
    {
      throw new IllegalStateException ("Unable to load parsing of Schematron reports.", e);
    }
  }
}
