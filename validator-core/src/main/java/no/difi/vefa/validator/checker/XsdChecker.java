package no.difi.vefa.validator.checker;

import java.io.IOException;

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

import no.difi.vefa.validator.ValidatorFactory;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.api.VefaDocument;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;

public class XsdChecker implements IChecker
{
  private static final Logger LOGGER = LoggerFactory.getLogger (XsdChecker.class);

  private final Schema m_aSchema;

  public XsdChecker (final Schema schema)
  {
    m_aSchema = schema;
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
        // cvc-complex-type.2.4.a — "Invalid content was found starting with element X. One of {...}
        // is expected."
        // cvc-complex-type.2.4.b — "The content of element X is not complete. One of {...} is
        // expected."
        // cvc-complex-type.2.4.c — "The matching wildcard is strict, but no declaration can be
        // found for element X."
        // cvc-complex-type.2.4.d — "Invalid content was found starting with element X. No child
        // element is expected at this point."
        // cvc-complex-type.2.4.e — "Element X can occur a maximum of N times in the current
        // sequence. This limit was exceeded."
        // cvc-complex-type.2.4.f — "Element X has invalid content. Expected element(s) {...}."
        try
        {
          final XMLStreamReader xmlStreamReader = ValidatorFactory.XML_INPUT_FACTORY.createXMLStreamReader (document.getInputStream ());

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
