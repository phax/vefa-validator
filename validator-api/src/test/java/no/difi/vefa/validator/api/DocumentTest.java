package no.difi.vefa.validator.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class DocumentTest {

  @Test
  public void simple() {
    final Document document = new Document(new ByteArrayInputStream(new byte[] {}), "identifier", null);

    assertNotNull(document.getInputStream());
    assertEquals(document.getDeclarations().get(0), "identifier");
    assertNull(document.getExpectation());
  }

}
