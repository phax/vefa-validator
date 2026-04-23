package no.difi.vefa.validator.declaration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collections;

import org.junit.Test;

import com.helger.asic.AsicVerifierFactory;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.base.io.stream.StreamHelper;

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
}
