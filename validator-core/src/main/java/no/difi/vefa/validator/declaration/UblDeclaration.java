package no.difi.vefa.validator.declaration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.jspecify.annotations.NonNull;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.cache.regex.RegExHelper;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import no.difi.vefa.validator.ValidatorFactory;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.StreamUtils;

/**
 * Document declaration for OASIS Universal Business Language (UBL).
 */
@Type ("xml.ubl")
public class UblDeclaration extends AbstractXmlDeclaration
{
  private static final Gson GSON = new Gson ();

  private XsltExecutable xsltExecutable;

  @Inject
  private void init () throws VefaValidatorException
  {
    try (final InputStream inputStream = getClass ().getResourceAsStream ("/vefa-validator/xslt/ubl-detect.xslt"))
    {
      xsltExecutable = ValidatorFactory.SAXON_PROCESSOR.newXsltCompiler ().compile (new StreamSource (inputStream));
    }
    catch (SaxonApiException | IOException e)
    {
      throw new VefaValidatorException ("Unable to load detector for UBL.", e);
    }
  }

  @Override
  public boolean verify (final byte [] content, @NonNull final List <String> parent)
  {
    return RegExHelper.stringMatchesPattern ("urn:oasis:names:specification:ubl:schema:xsd:(.+)-2::(.+)",
                                             parent.get (0));
  }

  @SuppressWarnings ("unchecked")
  @Override
  public List <String> detect (final InputStream streamContent, final List <String> parent)
                                                                                            throws VefaValidatorException
  {
    final NonBlockingByteArrayOutputStream baos = new NonBlockingByteArrayOutputStream ();
    try (final InputStream is = new NonBlockingByteArrayInputStream (StreamUtils.readAllAndReset (streamContent)))
    {
      final XsltTransformer xsltTransformer = xsltExecutable.load ();
      xsltTransformer.setSource (new StreamSource (is));
      xsltTransformer.setDestination (xsltExecutable.getProcessor ().newSerializer (baos));
      xsltTransformer.transform ();
    }
    catch (final Exception e)
    {
      throw new VefaValidatorException ("Unable to detect UBL information.", e);
    }

    return GSON.fromJson (baos.getAsString (StandardCharsets.UTF_8), List.class);
  }
}
