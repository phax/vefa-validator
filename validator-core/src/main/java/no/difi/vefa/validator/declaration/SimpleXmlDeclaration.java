package no.difi.vefa.validator.declaration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.ValidationException;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.StreamUtils;
import no.difi.vefa.validator.util.XmlUtils;

public class SimpleXmlDeclaration extends AbstractXmlDeclaration
{

  protected String namespace;

  protected String localName;

  public SimpleXmlDeclaration (final String namespace, final String localName)
  {
    this.namespace = namespace;
    this.localName = localName;
  }

  @Override
  public boolean verify (final byte [] content, final List <String> parent) throws ValidatorException
  {
    final String c = new String (content);
    return namespace.equals (XmlUtils.extractRootNamespace (c)) &&
           (localName == null || localName.equals (XmlUtils.extractLocalName (c)));
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent) throws ValidatorException
  {

    try
    {
      final byte [] bytes = StreamUtils.read50KAndReset (contentStream);
      return Collections.singletonList (String.format ("%s::%s",
                                                       namespace,
                                                       localName == null ? XmlUtils.extractLocalName (new String (bytes))
                                                                         : localName));
    }
    catch (final IOException e)
    {
      new ValidationException ("Couldn't detect SimpleXmlDeclaration", e);
    }

    return null;

  }
}
