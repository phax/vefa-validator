package no.difi.vefa.validator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.stream.StreamHelper;

import no.difi.vefa.validator.ValidatorFactory;

public class DeclarationDetectorTest
{
  private DeclarationDetector declarationDetector;

  @Before
  public void beforeClass ()
  {
    declarationDetector = ValidatorFactory.createDeclarationDetector ();
  }

  @Test
  public void simple () throws Exception
  {
    // noinspection ConstantConditions
    final byte [] bytes = StreamHelper.getAllBytes (getClass ().getResourceAsStream ("/documents/ehf-invoice-2.0.xml"));

    final InputStream inputStream = new NonBlockingByteArrayInputStream (bytes);
    final DeclarationIdentifier declarationIdentifier = declarationDetector.detect (inputStream);
    assertEquals (declarationIdentifier.getDeclaration ().getType (), "xml.ubl");
    assertEquals (declarationIdentifier.getIdentifier ().get (0),
                  "urn:www.cenbii.eu:profile:bii05:ver2.0#urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:urn:www.difi.no:ehf:faktura:ver2.0");
    assertNotNull (declarationIdentifier.getParent ());
  }
}
