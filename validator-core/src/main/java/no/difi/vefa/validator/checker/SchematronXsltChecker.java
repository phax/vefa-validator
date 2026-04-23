package no.difi.vefa.validator.checker;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.stream.StreamSource;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.base.timing.StopWatch;
import com.helger.io.resource.ClassPathResource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import no.difi.vefa.validator.ValidatorFactory;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.api.VefaDocument;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.JAXBHelper;
import no.difi.vefa.validator.util.VefaSaxonErrorListener;
import no.difi.vefa.validator.util.VefaSaxonMessageListener;
import no.difi.xsd.vefa.validator._1.SectionType;

public class SchematronXsltChecker implements IChecker
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SchematronXsltChecker.class);
  private static final JAXBContext JAXB_CONTEXT = JAXBHelper.context (SectionType.class);

  private static XsltExecutable s_aSvrlParser;

  private final XsltExecutable m_aXsltExecutable;

  public SchematronXsltChecker (final XsltExecutable xsltExecutable)
  {
    m_aXsltExecutable = xsltExecutable;
    if (s_aSvrlParser == null)
    {
      LOGGER.info ("Compiling SVRL Parser");
      try (InputStream inputStream = ClassPathResource.getInputStream ("/vefa-validator/xslt/svrl-parser.xslt",
                                                                       SchematronXsltChecker.class.getClassLoader ()))
      {
        final XsltCompiler xsltCompiler = ValidatorFactory.SAXON_PROCESSOR.newXsltCompiler ();
        s_aSvrlParser = xsltCompiler.compile (new StreamSource (inputStream));
      }
      catch (final Exception e)
      {
        throw new IllegalStateException ("Unable to load parsing of Schematron reports.", e);
      }
    }
  }

  @Override
  public void check (@NonNull final VefaDocument document, @NonNull final Section section) throws VefaValidatorException
  {
    LOGGER.info ("Running Schematron validation");
    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      final NonBlockingByteArrayOutputStream baos = new NonBlockingByteArrayOutputStream ();
      {
        final XsltTransformer parser = s_aSvrlParser.load ();
        parser.setErrorListener (VefaSaxonErrorListener.INSTANCE);
        parser.setMessageHandler (VefaSaxonMessageListener.INSTANCE);
        parser.setDestination (ValidatorFactory.SAXON_PROCESSOR.newSerializer (baos));

        final XsltTransformer schematron = m_aXsltExecutable.load ();
        schematron.setErrorListener (VefaSaxonErrorListener.INSTANCE);
        schematron.setMessageHandler (VefaSaxonMessageListener.INSTANCE);
        schematron.setSource (new StreamSource (document.getInputStream ()));
        schematron.setDestination (parser);

        schematron.transform ();

        schematron.close ();
        parser.close ();
      }

      aSW.stop ();

      if (false)
        LOGGER.info ("Inbetween: " + baos.getAsString (StandardCharsets.UTF_8));

      final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();
      final SectionType sectionType = unmarshaller.unmarshal (new StreamSource (baos.getAsInputStream ()),
                                                              SectionType.class).getValue ();

      section.setTitle (sectionType.getTitle ());
      section.add (sectionType.getAssertion ());
      section.setRuntime (aSW.getMillis () + "ms");
    }
    catch (final Exception e)
    {
      throw new VefaValidatorException ("Unable to perform check: " + e.getMessage (), e);
    }
  }
}
