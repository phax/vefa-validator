package no.difi.vefa.validator.declaration;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;

import no.difi.vefa.validator.module.ValidatorModule;
import no.difi.vefa.validator.util.DeclarationDetector;

public class UnCefactDeclarationTest
{

  @Inject
  private DeclarationDetector declarationDetector;

  @Before
  public void beforeClass ()
  {
    Guice.createInjector (new ValidatorModule ()).injectMembers (this);
  }

  @Test
  public void simplePeppol () throws Exception
  {
    final InputStream inputStream = new BufferedInputStream (getClass ().getResourceAsStream ("/documents/uncefact-peppol.xml"));
    assertEquals (declarationDetector.detect (inputStream)
                                     .getIdentifier ()
                                     .get (0),
                  "CrossIndustryInvoice" +
                                               "::urn:fdc:peppol.eu:2017:poacc:billing:01:1.0" +
                                               "::urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0");
  }

  @Test
  public void simpleTC434 () throws Exception
  {
    final InputStream inputStream = new BufferedInputStream (getClass ().getResourceAsStream ("/documents/uncefact-tc434.xml"));
    assertEquals (declarationDetector.detect (inputStream).getIdentifier ().get (0),
                  "CrossIndustryInvoice::urn:cen.eu:en16931:2017");
  }

  @Test
  public void simpleSimple () throws Exception
  {
    final InputStream inputStream = getClass ().getResourceAsStream ("/documents/uncefact-simple.xml");
    assertEquals (declarationDetector.detect (inputStream).getIdentifier ().get (0),
                  "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100::CrossIndustryInvoice");
  }
}
