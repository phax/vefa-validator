package no.difi.vefa.validator.checker;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IArtifactHolder;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.HolderLSResolveResource;

/**
 * @author erlend
 */
@Type (".xsd")
public class XsdCheckerFactory implements ICheckerFactory
{
  @Override
  public IChecker prepare (final IArtifactHolder artifactHolder, final String path) throws VefaValidatorException
  {
    try (final InputStream inputStream = artifactHolder.getInputStream (path))
    {
      final SchemaFactory schemaFactory = SchemaFactory.newInstance (XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver (new HolderLSResolveResource (artifactHolder, path));
      return new XsdChecker (schemaFactory.newSchema (new StreamSource (inputStream)));
    }
    catch (final Exception e)
    {
      throw new VefaValidatorException (e.getMessage (), e);
    }
  }
}
