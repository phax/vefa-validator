package no.difi.vefa.validator.declaration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IDeclarationWithConverter;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.expectation.ValidatorTestExpectation;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.JAXBHelper;
import no.difi.vefa.validator.util.StreamUtils;
import no.difi.xsd.vefa.validator._1.Test;

@Type ("xml.test")
public class ValidatorTestDeclaration extends SimpleXmlDeclaration implements IDeclarationWithConverter
{
  private static final Logger log = LoggerFactory.getLogger (ValidatorTestDeclaration.class);
  private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance ();
  private static final JAXBContext JAXB_CONTEXT = JAXBHelper.context (Test.class);

  public ValidatorTestDeclaration ()
  {
    super ("http://difi.no/xsd/vefa/validator/1.0", "test");
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent) throws ValidatorException
  {
    try
    {
      final byte [] content = StreamUtils.read50KAndReset (contentStream);
      final XMLStreamReader source = XML_INPUT_FACTORY.createXMLStreamReader (new ByteArrayInputStream (content));
      do
      {
        if (source.getEventType () == XMLStreamConstants.START_ELEMENT && source.getNamespaceURI ().equals (namespace))
          for (int i = 0; i < source.getAttributeCount (); i++)
            if (source.getAttributeName (i).toString ().equals ("configuration"))
              return Collections.singletonList (String.format ("configuration::%s", source.getAttributeValue (i)));
      } while (source.hasNext () && source.next () > 0);
    }
    catch (IOException | XMLStreamException e)
    {
      throw new ValidatorException (e.getMessage (), e);
    }
    return null;
  }

  @Override
  public IExpectation expectations (final byte [] content)
  {
    return new ValidatorTestExpectation (content);
  }

  @Override
  public void convert (final InputStream inputStream, final OutputStream outputStream)
  {
    try
    {
      final Test test = convertInputStream (inputStream);

      if (test.getAny () instanceof Node)
      {
        final Transformer transformer = TRANSFORMER_FACTORY.newTransformer ();
        transformer.setOutputProperty (OutputKeys.INDENT, "yes");
        transformer.transform (new DOMSource ((Node) test.getAny ()), new StreamResult (outputStream));
      }
    }
    catch (JAXBException | TransformerException e)
    {
      log.warn (e.getMessage (), e);
    }
  }

  private Test convertInputStream (final InputStream inputStream) throws JAXBException
  {
    return JAXB_CONTEXT.createUnmarshaller ().unmarshal (new StreamSource (inputStream), Test.class).getValue ();
  }
}
