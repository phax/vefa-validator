package no.difi.vefa.validator.declaration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collections;

import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.helger.asic.AsicVerifierFactory;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;

public class AsiceDeclarationTest
{

  private final AsiceDeclaration declaration = new AsiceDeclaration ();

  private final AsiceXmlDeclaration xmlDeclaration = new AsiceXmlDeclaration ();

  @Test
  public void validFile () throws Exception
  {
    final NonBlockingByteArrayOutputStream byteArrayOutputStream = new NonBlockingByteArrayOutputStream ();

    try (InputStream inputStream = getClass ().getResourceAsStream ("/documents/asic-cades-test-valid.asice"))
    {
      ByteStreams.copy (inputStream, byteArrayOutputStream);
    }

    assertTrue (declaration.verify (byteArrayOutputStream.toByteArray (), null));
  }

  @Test
  public void invalidFile () throws Exception
  {
    final NonBlockingByteArrayOutputStream byteArrayOutputStream = new NonBlockingByteArrayOutputStream ();

    try (InputStream inputStream = getClass ().getResourceAsStream ("/documents/peppol-bis-invoice-sbdh.zip"))
    {
      ByteStreams.copy (inputStream, byteArrayOutputStream);
    }

    assertFalse (declaration.verify (byteArrayOutputStream.toByteArray (), null));
  }

  @Test
  public void simpleXmlFile () throws Exception
  {
    final NonBlockingByteArrayOutputStream byteArrayOutputStream = new NonBlockingByteArrayOutputStream ();

    try (InputStream inputStream = getClass ().getResourceAsStream ("/documents/asic-xml.xml"))
    {
      ByteStreams.copy (inputStream, byteArrayOutputStream);
    }

    assertTrue (xmlDeclaration.verify (byteArrayOutputStream.toByteArray (),
                                       Collections.singletonList ("urn:etsi.org:specification:02918:v1.2.1::asic")));

    final NonBlockingByteArrayOutputStream converted = new NonBlockingByteArrayOutputStream ();
    xmlDeclaration.convert (new NonBlockingByteArrayInputStream (byteArrayOutputStream.toByteArray ()), converted);

    AsicVerifierFactory.newFactory ().verify (new NonBlockingByteArrayInputStream (converted.toByteArray ()));
  }
}
