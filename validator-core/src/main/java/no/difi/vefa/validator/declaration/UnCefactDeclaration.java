package no.difi.vefa.validator.declaration;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.cache.regex.RegExHelper;

import no.difi.vefa.validator.ValidatorXml;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.util.StreamUtils;

/**
 * Document declaration for OASIS Universal Business Language (UBL).
 */
@Type ("xml.uncefact")
public class UnCefactDeclaration extends AbstractXmlDeclaration
{

  private final static Set <String> FIELDS = Set.of ("BusinessProcessSpecifiedDocumentContextParameter",
                                                     "GuidelineSpecifiedDocumentContextParameter");

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    return RegExHelper.stringMatchesPattern ("urn:un:unece:uncefact:data:standard:(.+)::(.+)", parent.get (0));
  }

  @Override
  public List <String> detect (final InputStream aIS, final List <String> parent)
  {
    final StringBuilder aSB = new StringBuilder ();
    aSB.append (parent.get (0).split ("::")[1]);

    try
    {
      final byte [] content = StreamUtils.read50KAndReset (aIS);
      final XMLEventReader xmlEventReader = ValidatorXml.XML_INPUT_FACTORY.createXMLEventReader (new NonBlockingByteArrayInputStream (content));
      try
      {
        while (xmlEventReader.hasNext ())
        {
          XMLEvent xmlEvent = xmlEventReader.nextEvent ();
          if (xmlEvent.isStartElement ())
          {
            StartElement startElement = (StartElement) xmlEvent;
            if (FIELDS.contains (startElement.getName ().getLocalPart ()))
            {
              startElement = (StartElement) xmlEventReader.nextTag ();
              if ("ID".equals (startElement.getName ().getLocalPart ()))
              {
                xmlEvent = xmlEventReader.nextEvent ();
                if (xmlEvent instanceof final Characters aChars)
                {
                  aSB.append ("::").append (aChars.getData ());
                }
              }
            }
          }

          if (xmlEvent.isEndElement ())
          {
            final EndElement endElement = (EndElement) xmlEvent;
            if ("ExchangedDocumentContext".equals (endElement.getName ().getLocalPart ()))
            {
              return Collections.singletonList (aSB.toString ());
            }
          }
        }
      }
      finally
      {
        xmlEventReader.close ();
      }
    }
    catch (final Exception e)
    {
      // No action.
    }

    return null;
  }
}
