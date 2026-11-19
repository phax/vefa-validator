package no.difi.vefa.validator.declaration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.Test;

import com.helger.asic.AsicVerifierFactory;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.base.io.stream.StreamHelper;

import no.difi.vefa.validator.lang.VefaValidatorException;

public class AsiceDeclarationTest
{
  private final AsiceDeclaration declaration = new AsiceDeclaration ();
  private final AsiceXmlDeclaration xmlDeclaration = new AsiceXmlDeclaration ();

  @Test
  public void validFile () throws Exception
  {
    assertTrue (declaration.verify (StreamHelper.getAllBytes (getClass ().getResourceAsStream ("/documents/asic-cades-test-valid.asice")),
                                    null));
  }

  @Test
  public void invalidFile () throws Exception
  {
    assertFalse (declaration.verify (StreamHelper.getAllBytes (getClass ().getResourceAsStream ("/documents/peppol-bis-invoice-sbdh.zip")),
                                     null));
  }

  @Test
  public void simpleXmlFile () throws Exception
  {
    final NonBlockingByteArrayOutputStream byteArrayOutputStream = new NonBlockingByteArrayOutputStream ();

    try (InputStream inputStream = getClass ().getResourceAsStream ("/documents/asic-xml.xml"))
    {
      StreamHelper.copyByteStream ()
                  .from (inputStream)
                  .closeFrom (false)
                  .to (byteArrayOutputStream)
                  .closeTo (true)
                  .build ();
    }

    assertTrue (xmlDeclaration.verify (byteArrayOutputStream.toByteArray (),
                                       Collections.singletonList ("urn:etsi.org:specification:02918:v1.2.1::asic")));

    final NonBlockingByteArrayOutputStream converted = new NonBlockingByteArrayOutputStream ();
    xmlDeclaration.convert (byteArrayOutputStream.getAsInputStream (), converted);

    AsicVerifierFactory.newFactory ().verify (converted.getAsInputStream ());
  }

  @Test
  public void childrenOfBrokenContainer ()
  {
    try
    {
      declaration.children (new NonBlockingByteArrayInputStream ("not a zip".getBytes (StandardCharsets.ISO_8859_1)));
      fail ();
    }
    catch (final VefaValidatorException ex)
    {
      assertTrue (ex.getMessage (), ex.getMessage ().contains ("not a ZIP archive"));
    }
  }
}
