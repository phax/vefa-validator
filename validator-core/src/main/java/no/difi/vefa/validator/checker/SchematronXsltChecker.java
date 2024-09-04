package no.difi.vefa.validator.checker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.transform.stream.StreamSource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import no.difi.vefa.validator.api.Document;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.JAXBHelper;
import no.difi.vefa.validator.util.SaxonErrorListener;
import no.difi.vefa.validator.util.SaxonMessageListener;
import no.difi.xsd.vefa.validator._1.SectionType;

public class SchematronXsltChecker implements IChecker
{
  private static final JAXBContext JAXB_CONTEXT = JAXBHelper.context (SectionType.class);

  private final Processor processor;

  private final XsltExecutable xsltExecutable;

  @Inject
  @Named ("schematron-svrl-parser")
  private Provider <XsltExecutable> parser;

  public SchematronXsltChecker (final Processor processor, final XsltExecutable xsltExecutable)
  {
    this.processor = processor;
    this.xsltExecutable = xsltExecutable;
  }

  @Override
  public void check (final Document document, final Section section) throws ValidatorException
  {
    final long tsStart = System.currentTimeMillis ();
    try
    {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream ();

      final XsltTransformer parser = this.parser.get ().load ();
      final XsltTransformer schematron = xsltExecutable.load ();

      schematron.setErrorListener (SaxonErrorListener.INSTANCE);
      schematron.setMessageListener (SaxonMessageListener.INSTANCE);
      schematron.setSource (new StreamSource (document.getInputStream ()));
      schematron.setDestination (parser);

      parser.setErrorListener (SaxonErrorListener.INSTANCE);
      parser.setMessageListener (SaxonMessageListener.INSTANCE);
      parser.setDestination (processor.newSerializer (baos));

      schematron.transform ();

      parser.close ();
      schematron.close ();

      final long tsEnd = System.currentTimeMillis ();

      final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();
      final SectionType sectionType = unmarshaller.unmarshal (new StreamSource (new ByteArrayInputStream (baos.toByteArray ())),
                                                              SectionType.class)
                                                  .getValue ();

      section.setTitle (sectionType.getTitle ());
      section.add (sectionType.getAssertion ());
      section.setRuntime ((tsEnd - tsStart) + "ms");
    }
    catch (final Exception e)
    {
      throw new ValidatorException (String.format ("Unable to perform check: %s", e.getMessage ()), e);
    }
  }
}
