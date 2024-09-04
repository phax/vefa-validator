package no.difi.vefa.validator.module;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * @author erlend
 */
public class SbdhModule extends AbstractModule
{
  @Provides
  @Named ("sbdh-extractor")
  @Singleton
  public XsltExecutable getSchematronCompiler (final Processor processor)
  {
    try (InputStream inputStream = getClass ().getResourceAsStream ("/vefa-validator/xslt/sbdh-extractor.xslt"))
    {
      final XsltCompiler xsltCompiler = processor.newXsltCompiler ();
      return xsltCompiler.compile (new StreamSource (inputStream));
    }
    catch (IOException | SaxonApiException e)
    {
      throw new IllegalStateException ("Unable to load extraction of SBDH content.");
    }
  }
}
