package no.difi.vefa.validator.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import no.difi.xsd.vefa.validator._1.Configurations;

public class JAXBHelperTest
{

  @Test
  public void simpleContructor ()
  {
    new JAXBHelper ();
  }

  @Test
  public void simpleSuccess ()
  {
    assertNotNull (JAXBHelper.context (Configurations.class));
  }

  @Test (expected = RuntimeException.class)
  @SuppressWarnings ("all")
  public void simpleError ()
  {
    JAXBHelper.context (null);
  }
}
