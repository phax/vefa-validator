package no.difi.vefa.validator.checker;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.base.timing.StopWatch;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
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

  private final Processor m_aProcessor;
  private final XsltExecutable m_aXsltExecutable;

  @Inject
  @Named ("schematron-svrl-parser")
  private Provider <XsltExecutable> parser;

  public SchematronXsltChecker (final Processor processor, final XsltExecutable xsltExecutable)
  {
    this.m_aProcessor = processor;
    this.m_aXsltExecutable = xsltExecutable;
  }

  @Override
  public void check (final VefaDocument document, final Section section) throws VefaValidatorException
  {
    LOGGER.info ("Running Schematron validation");
    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      final NonBlockingByteArrayOutputStream baos = new NonBlockingByteArrayOutputStream ();
      {
        final XsltTransformer parser = this.parser.get ().load ();
        final XsltTransformer schematron = m_aXsltExecutable.load ();

        schematron.setErrorListener (VefaSaxonErrorListener.INSTANCE);
        schematron.setMessageListener (VefaSaxonMessageListener.INSTANCE);
        schematron.setSource (new StreamSource (document.getInputStream ()));
        schematron.setDestination (parser);

        parser.setErrorListener (VefaSaxonErrorListener.INSTANCE);
        parser.setMessageListener (VefaSaxonMessageListener.INSTANCE);
        parser.setDestination (m_aProcessor.newSerializer (baos));

        schematron.transform ();

        schematron.close ();
        parser.close ();
      }

      aSW.stop ();

      final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();
      final SectionType sectionType = unmarshaller.unmarshal (new StreamSource (new NonBlockingByteArrayInputStream (baos.toByteArray ())),
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
