package no.difi.vefa.validator.declaration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.StreamUtils;

@Type ("xml.espd")
public class EspdDeclaration extends AbstractXmlDeclaration
{

  private final static List <String> validParents = Arrays.asList ("urn:grow:names:specification:ubl:schema:xsd:ESPDRequest-1::ESPDRequest",
                                                                   "urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1::ESPDResponse");

  @Override
  public boolean verify (final byte [] content, final List <String> parent) throws ValidatorException
  {
    return validParents.contains (parent.get (0));
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent) throws ValidatorException
  {
    final List <String> results = new ArrayList <> ();

    try
    {
      final byte [] content = StreamUtils.read50KAndReset (contentStream);
      final XMLEventReader xmlEventReader = XML_INPUT_FACTORY.createXMLEventReader (new ByteArrayInputStream (content));
      while (xmlEventReader.hasNext ())
      {
        XMLEvent xmlEvent = xmlEventReader.nextEvent ();

        if (xmlEvent.isStartElement ())
        {
          if ("CustomizationID".equals (((StartElement) xmlEvent).getName ().getLocalPart ()))
          {
            xmlEvent = xmlEventReader.nextEvent ();
            if (xmlEvent instanceof Characters)
              results.add (String.format ("%s::%s", parent.get (0), ((Characters) xmlEvent).getData ()));
          }
          if ("VersionID".equals (((StartElement) xmlEvent).getName ().getLocalPart ()))
          {
            xmlEvent = xmlEventReader.nextEvent ();
            if (xmlEvent instanceof Characters)
              results.add (String.format ("%s::%s", parent.get (0), ((Characters) xmlEvent).getData ()));
          }
        }
      }
    }
    catch (final Exception e)
    {
      // No action.
    }

    return results.isEmpty () ? parent : results;
  }
}
