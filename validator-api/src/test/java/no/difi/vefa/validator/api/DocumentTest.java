package no.difi.vefa.validator.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;

public class DocumentTest
{

  @Test
  public void simple ()
  {
    final VefaDocument document = new VefaDocument (new NonBlockingByteArrayInputStream (new byte [] {}),
                                                    "identifier",
                                                    null);

    assertNotNull (document.getInputStream ());
    assertEquals (document.getDeclarations ().get (0), "identifier");
    assertNull (document.getExpectation ());
  }

}
