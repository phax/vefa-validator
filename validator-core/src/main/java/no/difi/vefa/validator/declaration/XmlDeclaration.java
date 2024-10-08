package no.difi.vefa.validator.declaration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.StreamUtils;
import no.difi.vefa.validator.util.XmlUtils;

@Type ("xml")
public class XmlDeclaration extends AbstractXmlDeclaration
{

  @Override
  public boolean verify (final byte [] content, final List <String> parent) throws ValidatorException
  {
    return XmlUtils.extractRootNamespace (new String (content)) != null;
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent) throws ValidatorException
  {

    try
    {
      final byte [] content = StreamUtils.read50KAndReset (contentStream);
      final String c = new String (content);
      return Collections.singletonList (String.format ("%s::%s",
                                                       XmlUtils.extractRootNamespace (c),
                                                       XmlUtils.extractLocalName (c)));
    }
    catch (final IOException e)
    {
      // Simply ignore
    }

    return null;
  }

  @Override
  public IExpectation expectations (final byte [] content) throws ValidatorException
  {
    return null;
  }
}
