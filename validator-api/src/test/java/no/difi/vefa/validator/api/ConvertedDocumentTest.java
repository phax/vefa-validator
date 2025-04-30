package no.difi.vefa.validator.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;

public class ConvertedDocumentTest
{

  @Test
  public void simple ()
  {
    final ConvertedVefaDocument document = new ConvertedVefaDocument (new NonBlockingByteArrayInputStream (new byte [] {}),
                                                                      new NonBlockingByteArrayInputStream (new byte [] {}),
                                                                      "identifier",
                                                                      null);

    assertNotNull (document.getInputStream ());
    assertNotNull (document.getSource ());
    assertEquals (document.getDeclarations ().get (0), "identifier");
    assertNull (document.getExpectation ());
  }

}
