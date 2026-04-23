package no.difi.vefa.validator.declaration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;

import no.difi.vefa.validator.ValidatorFactory;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.util.StreamUtils;

@Type ("xml.nobl")
public class NoblDeclaration extends AbstractXmlDeclaration
{
  private static final Pattern PATTERN = Pattern.compile ("urn:fdc:difi.no:2018:nobl:(.+)-1::(.+)");

  private static final Set <String> FIELDS = Set.of ("CustomizationID", "ProfileID");

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    return PATTERN.matcher (parent.get (0)).matches ();
  }

  @Override
  public List <String> detect (final InputStream aIS, final List <String> parent)
  {
    final List <String> results = new ArrayList <> ();

    final String type = parent.get (0).split ("::")[1];

    final StringBuilder aSB = new StringBuilder ();
    aSB.append (type);

    try
    {
      final byte [] content = StreamUtils.read50KAndReset (aIS);
      final XMLEventReader xmlEventReader = ValidatorFactory.XML_INPUT_FACTORY.createXMLEventReader (new NonBlockingByteArrayInputStream (content));
      try
      {
        while (xmlEventReader.hasNext ())
        {
          XMLEvent xmlEvent = xmlEventReader.nextEvent ();
          if (xmlEvent.isStartElement ())
          {
            final StartElement startElement = (StartElement) xmlEvent;
            if (FIELDS.contains (startElement.getName ().getLocalPart ()))
            {
              xmlEvent = xmlEventReader.nextEvent ();
              if (xmlEvent instanceof final Characters aChars)
              {
                aSB.append ("::").append (aChars.getData ());
                results.add (type + "::" + aChars.getData ());
              }
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

    results.add (aSB.toString ());

    return results;
  }
}
