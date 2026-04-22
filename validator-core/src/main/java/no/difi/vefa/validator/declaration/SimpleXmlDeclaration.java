package no.difi.vefa.validator.declaration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.StreamUtils;
import no.difi.vefa.validator.util.XmlUtils;

public class SimpleXmlDeclaration extends AbstractXmlDeclaration
{
  protected String m_sNamespace;
  protected String m_sLocalName;

  public SimpleXmlDeclaration (final String namespace, final String localName)
  {
    this.m_sNamespace = namespace;
    this.m_sLocalName = localName;
  }

  @Override
  public boolean verify (final byte [] content, final List <String> parent) throws VefaValidatorException
  {
    final String c = new String (content);
    return m_sNamespace.equals (XmlUtils.extractRootNamespace (c)) &&
           (m_sLocalName == null || m_sLocalName.equals (XmlUtils.extractLocalName (c)));
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent)
                                                                                            throws VefaValidatorException
  {
    try
    {
      final byte [] bytes = StreamUtils.read50KAndReset (contentStream);
      return Collections.singletonList (m_sNamespace +
                                        "::" +
                                        (m_sLocalName == null ? XmlUtils.extractLocalName (new String (bytes))
                                                           : m_sLocalName));
    }
    catch (final IOException e)
    {
      throw new VefaValidatorException ("Couldn't detect SimpleXmlDeclaration", e);
    }
  }
}
