package no.difi.vefa.validator.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class ConvertedDocumentTest {

  @Test
  public void simple() {
    final ConvertedDocument document = new ConvertedDocument(new ByteArrayInputStream(new byte[] {}),
                                                             new ByteArrayInputStream(new byte[] {}), "identifier", null);

    assertNotNull(document.getInputStream());
    assertNotNull(document.getSource());
    assertEquals(document.getDeclarations().get(0), "identifier");
    assertNull(document.getExpectation());
  }

}
