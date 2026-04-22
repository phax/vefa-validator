package no.difi.vefa.validator.checker;

import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.api.VefaDocument;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;

public class XsdChecker implements IChecker
{
  private static final Logger LOGGER = LoggerFactory.getLogger (XsdChecker.class);
  private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance ();

  private final Schema m_aSchema;

  public XsdChecker (final Schema schema)
  {
    this.m_aSchema = schema;
  }

  @Override
  public void check (final VefaDocument document, final Section section)
  {
    LOGGER.info ("Running XSD validation");
    section.setTitle ("XSD validation");

    final Source xmlFile = new StreamSource (document.getInputStream ());

    final long tsStart = System.currentTimeMillis ();
    try
    {
      final Validator validator = m_aSchema.newValidator ();
      validator.validate (xmlFile);
    }
    catch (final SAXParseException e)
    {
      String humanMessage = e.getMessage ();
      if (humanMessage.startsWith ("cvc-complex-type.2.4."))
      {
        try
        {
          final XMLStreamReader xmlStreamReader = XML_INPUT_FACTORY.createXMLStreamReader (document.getInputStream ());

          // Go to root element.
          while (xmlStreamReader.hasNext () && xmlStreamReader.getEventType () != XMLStreamConstants.START_ELEMENT)
            xmlStreamReader.next ();

          for (int i = 0; i < xmlStreamReader.getNamespaceCount (); i++)
          {
            if (xmlStreamReader.getNamespacePrefix (i) == null)
              humanMessage = humanMessage.replace ("\"" + xmlStreamReader.getNamespaceURI (i) + "\":", "");
            else
              humanMessage = humanMessage.replace ("\"" + xmlStreamReader.getNamespaceURI (i) + "\"",
                                                   xmlStreamReader.getNamespacePrefix (i));
          }

          xmlStreamReader.close ();
        }
        catch (final XMLStreamException ex)
        {
          // No action.
        }
      }

      if (humanMessage.startsWith ("cvc-"))
        humanMessage = humanMessage.replaceAll ("^(.*?): (.*)$", "$2");

      final AssertionType assertionType = new AssertionType ();
      assertionType.setIdentifier ("XSD");
      assertionType.setText (e.getMessage ());
      assertionType.setTextFriendly (humanMessage);
      assertionType.setLocation ("Line " + e.getLineNumber () + ", column " + e.getColumnNumber () + ".");
      assertionType.setFlag (FlagType.FATAL);
      section.add (assertionType);
    }
    catch (SAXException | IOException e)
    {
      section.add ("XSD", e.getMessage (), FlagType.FATAL);
    }

    section.setRuntime ((System.currentTimeMillis () - tsStart) + "ms");
  }
}
