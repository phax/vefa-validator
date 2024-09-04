package no.difi.vefa.validator.declaration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.util.StreamUtils;

/**
 * Document declaration for OASIS Universal Business Language (UBL).
 */
@Type ("xml.uncefact")
public class UnCefactDeclaration extends AbstractXmlDeclaration
{

  private final static List <String> informationElements = Arrays.asList ("BusinessProcessSpecifiedDocumentContextParameter",
                                                                          "GuidelineSpecifiedDocumentContextParameter");

  private static Pattern pattern = Pattern.compile ("urn:un:unece:uncefact:data:standard:(.+)::(.+)");

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    return pattern.matcher (parent.get (0)).matches ();
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent)
  {
    final StringBuilder stringBuilder = new StringBuilder ();
    stringBuilder.append (parent.get (0).split ("::")[1]);

    try
    {
      final byte [] content = StreamUtils.read50KAndReset (contentStream);
      final XMLEventReader xmlEventReader = XML_INPUT_FACTORY.createXMLEventReader (new ByteArrayInputStream (content));
      while (xmlEventReader.hasNext ())
      {
        XMLEvent xmlEvent = xmlEventReader.nextEvent ();

        if (xmlEvent.isStartElement ())
        {
          StartElement startElement = (StartElement) xmlEvent;

          if (informationElements.contains (startElement.getName ().getLocalPart ()))
          {
            startElement = (StartElement) xmlEventReader.nextTag ();

            if ("ID".equals (startElement.getName ().getLocalPart ()))
            {
              xmlEvent = xmlEventReader.nextEvent ();

              if (xmlEvent instanceof Characters)
              {
                stringBuilder.append ("::");
                stringBuilder.append (((Characters) xmlEvent).getData ());
              }
            }
          }
        }

        if (xmlEvent.isEndElement ())
        {
          final EndElement endElement = (EndElement) xmlEvent;

          if ("ExchangedDocumentContext".equals (endElement.getName ().getLocalPart ()))
            return Collections.singletonList (stringBuilder.toString ());
        }
      }
    }
    catch (final Exception e)
    {
      // No action.
    }

    return null;
  }
}
