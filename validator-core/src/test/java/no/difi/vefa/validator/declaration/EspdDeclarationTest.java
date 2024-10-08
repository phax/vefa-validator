package no.difi.vefa.validator.declaration;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;

import no.difi.vefa.validator.module.ValidatorModule;
import no.difi.vefa.validator.util.DeclarationDetector;
import no.difi.vefa.validator.util.DeclarationIdentifier;

public class EspdDeclarationTest
{

  @Inject
  private DeclarationDetector declarationDetector;

  @Before
  public void beforeClass ()
  {
    Guice.createInjector (new ValidatorModule ()).injectMembers (this);
  }

  @Test
  public void simple () throws Exception
  {

    try (
        InputStream inputStream = new BufferedInputStream (getClass ().getResourceAsStream ("/documents/ESPDResponse-2.xml")))
    {
      final DeclarationIdentifier declarationIdentifier = declarationDetector.detect (inputStream);
      assertEquals (declarationIdentifier.getIdentifier ().get (0),
                    "urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1::ESPDResponse::SomeCustomization");
    }
  }

  @Test
  public void simpleCustomization () throws Exception
  {
    try (
        InputStream inputStream = new BufferedInputStream (getClass ().getResourceAsStream ("/documents/ESPDResponse.xml")))
    {
      final DeclarationIdentifier declarationIdentifier = declarationDetector.detect (inputStream);
      assertEquals (declarationIdentifier.getIdentifier ().get (0),
                    "urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1::ESPDResponse::1");
    }
  }

  @Test
  public void simpleVeryShort () throws Exception
  {
    final String xml = "<espd:ESPDResponse " +
                       "xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" " +
                       "xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" " +
                       "xmlns:cev-cbc=\"urn:isa:names:specification:ubl:schema:xsd:CEV-CommonBasicComponents-1\" " +
                       "xmlns:ccv-cbc=\"urn:isa:names:specification:ubl:schema:xsd:CCV-CommonBasicComponents-1\" " +
                       "xmlns:cev=\"urn:isa:names:specification:ubl:schema:xsd:CEV-CommonAggregateComponents-1\" " +
                       "xmlns:espd-cac=\"urn:grow:names:specification:ubl:schema:xsd:ESPD-CommonAggregateComponents-1\" " +
                       "xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\" " +
                       "xmlns:espd-cbc=\"urn:grow:names:specification:ubl:schema:xsd:ESPD-CommonBasicComponents-1\" " +
                       "xmlns:ccv=\"urn:isa:names:specification:ubl:schema:xsd:CCV-CommonAggregateComponents-1\" " +
                       "xmlns:espd-res=\"urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1\" " +
                       "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                       "xmlns:espd=\"urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1\">";

    final DeclarationIdentifier declarationIdentifier = declarationDetector.detect (new ByteArrayInputStream (xml.getBytes ()));
    assertEquals (declarationIdentifier.getIdentifier ().get (0),
                  "urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1::ESPDResponse");
  }

  @Test
  public void simpleEmptyVersion () throws Exception
  {
    final String xml = "<espd:ESPDResponse " +
                       "xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" " +
                       "xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" " +
                       "xmlns:cev-cbc=\"urn:isa:names:specification:ubl:schema:xsd:CEV-CommonBasicComponents-1\" " +
                       "xmlns:ccv-cbc=\"urn:isa:names:specification:ubl:schema:xsd:CCV-CommonBasicComponents-1\" " +
                       "xmlns:cev=\"urn:isa:names:specification:ubl:schema:xsd:CEV-CommonAggregateComponents-1\" " +
                       "xmlns:espd-cac=\"urn:grow:names:specification:ubl:schema:xsd:ESPD-CommonAggregateComponents-1\" " +
                       "xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\" " +
                       "xmlns:espd-cbc=\"urn:grow:names:specification:ubl:schema:xsd:ESPD-CommonBasicComponents-1\" " +
                       "xmlns:ccv=\"urn:isa:names:specification:ubl:schema:xsd:CCV-CommonAggregateComponents-1\" " +
                       "xmlns:espd-res=\"urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1\" " +
                       "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                       "xmlns:espd=\"urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1\">" +
                       "<VersionID></VersionID>";

    final DeclarationIdentifier declarationIdentifier = declarationDetector.detect (new ByteArrayInputStream (xml.getBytes ()));
    assertEquals (declarationIdentifier.getIdentifier ().get (0),
                  "urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1::ESPDResponse");
  }

  @Test
  public void simpleWithoutVersion () throws Exception
  {
    final String xml = "<espd:ESPDResponse " +
                       "xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" " +
                       "xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" " +
                       "xmlns:cev-cbc=\"urn:isa:names:specification:ubl:schema:xsd:CEV-CommonBasicComponents-1\" " +
                       "xmlns:ccv-cbc=\"urn:isa:names:specification:ubl:schema:xsd:CCV-CommonBasicComponents-1\" " +
                       "xmlns:cev=\"urn:isa:names:specification:ubl:schema:xsd:CEV-CommonAggregateComponents-1\" " +
                       "xmlns:espd-cac=\"urn:grow:names:specification:ubl:schema:xsd:ESPD-CommonAggregateComponents-1\" " +
                       "xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\" " +
                       "xmlns:espd-cbc=\"urn:grow:names:specification:ubl:schema:xsd:ESPD-CommonBasicComponents-1\" " +
                       "xmlns:ccv=\"urn:isa:names:specification:ubl:schema:xsd:CCV-CommonAggregateComponents-1\" " +
                       "xmlns:espd-res=\"urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1\" " +
                       "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                       "xmlns:espd=\"urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1\">" +
                       "</espd:ESPDResponse>";

    final DeclarationIdentifier declarationIdentifier = declarationDetector.detect (new ByteArrayInputStream (xml.getBytes ()));
    assertEquals (declarationIdentifier.getIdentifier ().get (0),
                  "urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1::ESPDResponse");
  }
}
