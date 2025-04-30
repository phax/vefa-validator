package no.difi.vefa.validator.declaration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.helger.commons.regex.RegExHelper;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.StreamUtils;

/**
 * Document declaration for OASIS Universal Business Language (UBL).
 */
@Type ("xml.ubl")
public class UblDeclaration extends AbstractXmlDeclaration
{
  private static final Gson gson = new Gson ();

  private XsltExecutable xsltExecutable;

  @Inject
  private void init (final Processor processor) throws VefaValidatorException
  {
    try (InputStream inputStream = getClass ().getResourceAsStream ("/vefa-validator/xslt/ubl-detect.xslt"))
    {
      xsltExecutable = processor.newXsltCompiler ().compile (new StreamSource (inputStream));
    }
    catch (SaxonApiException | IOException e)
    {
      throw new VefaValidatorException ("Unable to load detector for UBL.", e);
    }
  }

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    return RegExHelper.stringMatchesPattern ("urn:oasis:names:specification:ubl:schema:xsd:(.+)-2::(.+)",
                                             parent.get (0));
  }

  @Override
  public List <String> detect (final InputStream streamContent, final List <String> parent)
                                                                                            throws VefaValidatorException
  {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();

    try (InputStream is = new ByteArrayInputStream (StreamUtils.readAllAndReset (streamContent)))
    {
      final XsltTransformer xsltTransformer = xsltExecutable.load ();
      xsltTransformer.setSource (new StreamSource (is));
      xsltTransformer.setDestination (xsltExecutable.getProcessor ().newSerializer (baos));
      xsltTransformer.transform ();
    }
    catch (SaxonApiException | IOException e)
    {
      throw new VefaValidatorException ("Unable to detect UBL information.", e);
    }

    // noinspection unchecked
    return gson.fromJson (baos.toString (), List.class);
  }
}
