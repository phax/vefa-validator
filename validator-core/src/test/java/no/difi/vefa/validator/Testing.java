package no.difi.vefa.validator;

import static org.junit.Assert.assertEquals;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.difi.vefa.validator.api.IValidation;
import no.difi.vefa.validator.properties.SimpleProperties;
import no.difi.vefa.validator.source.ClasspathSource;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.SectionType;

public class Testing
{
  private static final Logger log = LoggerFactory.getLogger (Testing.class);

  private static Validator validator;

  @BeforeClass
  public static void beforeClass () throws Exception
  {
    validator = ValidatorBuilder.newValidator ()
                                .setProperties (new SimpleProperties ().set ("feature.expectation", true))
                                .setSource (new ClasspathSource ("/rules/"))
                                .build ();
  }

  @AfterClass
  public static void afterClass () throws Exception
  {
    validator.close ();
    validator = null;
  }

  @Test
  public void simpleError () throws Exception
  {
    final IValidation validation = validator.validate (getClass ().getResourceAsStream ("/documents/T10-hode-feilkoder.xml"));

    for (final SectionType sectionType : validation.getReport ().getSection ())
    {
      log.info (sectionType.getTitle () + ": " + sectionType.getRuntime ());
      for (final AssertionType assertion : sectionType.getAssertion ())
        log.info (String.format ("- [%s] %s (%s)",
                                 assertion.getIdentifier (),
                                 assertion.getText (),
                                 assertion.getFlag ()));
    }

    try (final OutputStream outputStream = new FileOutputStream ("target/test-simple-feilkoder.html"))
    {
      validation.render (outputStream);
    }

    assertEquals (validation.getReport ().getFlag (), FlagType.ERROR);
    assertEquals (validation.getReport ().getSection ().get (5).getAssertion ().size (), 5);
    assertEquals (validation.getDocument ()
                            .getDeclarations ()
                            .get (0),
                  "xml.ubl::urn:www.cenbii.eu:profile:bii04:ver2.0#" +
                                      "urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:" +
                                      "urn:www.peppol.eu:bis:peppol4a:ver2.0:extended:" +
                                      "urn:www.difi.no:ehf:faktura:ver2.0");
  }

  @Test
  public void simpleOk () throws Exception
  {
    final IValidation validation = validator.validate (getClass ().getResourceAsStream ("/documents/ehf-invoice-2.0.xml"));

    for (final SectionType sectionType : validation.getReport ().getSection ())
    {
      log.info (sectionType.getTitle () + ": " + sectionType.getRuntime ());
      for (final AssertionType assertion : sectionType.getAssertion ())
        log.info (String.format ("- [%s] %s (%s)",
                                 assertion.getIdentifier (),
                                 assertion.getText (),
                                 assertion.getFlag ()));
    }

    final OutputStream outputStream = new FileOutputStream ("target/test-simple-invoice.html");
    validation.render (outputStream);
    outputStream.close ();

    assertEquals (validation.getReport ().getFlag (), FlagType.OK);
    assertEquals (validation.getDocument ()
                            .getDeclarations ()
                            .get (0),
                  "xml.ubl::urn:www.cenbii.eu:profile:bii05:ver2.0#" +
                                      "urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:" +
                                      "urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:" +
                                      "urn:www.difi.no:ehf:faktura:ver2.0");
  }

  @Test
  public void simpleValidatorTest ()
  {
    final IValidation validation = validator.validate (getClass ().getResourceAsStream ("/documents/NOGOV-T10-R014.xml"),
                                                       new SimpleProperties ().set ("feature.nesting", true));
    assertEquals (validation.getReport ().getFlag (), FlagType.OK);
    assertEquals (validation.getChildren ().size (), 3);
  }

  @Test
  public void billing3Test ()
  {
    final IValidation validation = validator.validate (getClass ().getResourceAsStream ("/documents/peppol-billing-3.0.xml"));
    assertEquals (validation.getReport ().getFlag (), FlagType.OK);
    assertEquals (validation.getReport ().getTitle (), "PEPPOL BIS Billing 3.0 (Profile 01)");
  }

  @Test
  public void testValidationWithLongUblExtension ()
  {
    final IValidation validation = validator.validate (getClass ().getResourceAsStream ("/documents/peppol-billing-3.0_long_ubl_extension.xml"));
    assertEquals (validation.getReport ().getFlag (), FlagType.WARNING);
    assertEquals (validation.getReport ().getTitle (), "PEPPOL BIS Billing 3.0 (Profile 01)");
  }

  @Test
  public void testValidationEmptyUbl ()
  {
    final IValidation validation = validator.validate (getClass ().getResourceAsStream ("/documents/ubl-invoice-empty.xml"));
    assertEquals (validation.getReport ().getFlag (), FlagType.UNKNOWN);
  }
}
