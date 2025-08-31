package no.difi.vefa.validator.trigger;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.helger.io.resource.ClassPathResource;

import no.difi.vefa.validator.Validator;
import no.difi.vefa.validator.ValidatorBuilder;
import no.difi.vefa.validator.api.IValidation;
import no.difi.vefa.validator.source.ClasspathSource;
import no.difi.xsd.vefa.validator._1.FlagType;

public class AsiceTriggerTest
{

  private static Validator validator;

  @BeforeClass
  public static void beforeClass ()
  {
    validator = ValidatorBuilder.newValidator ().setSource (new ClasspathSource ("/rules/")).build ();
  }

  @AfterClass
  public static void afterClass ()
  {
    validator.close ();
  }

  @Test
  @Ignore
  public void simpleInvalidAsice () throws IOException
  {
    final IValidation validation = validator.validate (new ClassPathResource ("/documents/asic-cades-test-invalid-signature.asice"));
    assertEquals (validation.getReport ().getFlag (), FlagType.FATAL);
  }

  @Test
  public void simpleValidAsice () throws IOException
  {
    final IValidation validation = validator.validate (new ClassPathResource ("/documents/asic-cades-test-valid.asice"));
    assertEquals (validation.getReport ().getFlag (), FlagType.OK);
  }
}
