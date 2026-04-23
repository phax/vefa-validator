package no.difi.vefa.validator.declaration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;

import no.difi.vefa.validator.ValidatorFactory;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.StreamUtils;

@Type ("xml.espd")
public class EspdDeclaration extends AbstractXmlDeclaration
{
  private final static List <String> validParents = Arrays.asList ("urn:grow:names:specification:ubl:schema:xsd:ESPDRequest-1::ESPDRequest",
                                                                   "urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1::ESPDResponse");

  @Override
  public boolean verify (final byte [] content, final List <String> parent) throws VefaValidatorException
  {
    return validParents.contains (parent.get (0));
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent)
                                                                                            throws VefaValidatorException
  {
    final List <String> results = new ArrayList <> ();

    try
    {
      final byte [] content = StreamUtils.read50KAndReset (contentStream);
      final XMLEventReader xmlEventReader = ValidatorFactory.XML_INPUT_FACTORY.createXMLEventReader (new NonBlockingByteArrayInputStream (content));
      try
      {
        while (xmlEventReader.hasNext ())
        {
          XMLEvent xmlEvent = xmlEventReader.nextEvent ();

          if (xmlEvent.isStartElement ())
          {
            if ("CustomizationID".equals (((StartElement) xmlEvent).getName ().getLocalPart ()))
            {
              xmlEvent = xmlEventReader.nextEvent ();
              if (xmlEvent instanceof Characters)
              {
                results.add (parent.get (0) + "::" + ((Characters) xmlEvent).getData ());
              }
            }
            if ("VersionID".equals (((StartElement) xmlEvent).getName ().getLocalPart ()))
            {
              xmlEvent = xmlEventReader.nextEvent ();
              if (xmlEvent instanceof Characters)
              {
                results.add (parent.get (0) + "::" + ((Characters) xmlEvent).getData ());
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

    return results.isEmpty () ? parent : results;
  }
}
