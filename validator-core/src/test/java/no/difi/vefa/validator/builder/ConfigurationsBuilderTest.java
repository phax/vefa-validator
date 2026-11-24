package no.difi.vefa.validator.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import no.difi.xsd.vefa.validator._1.Configurations;

public class ConfigurationsBuilderTest
{
  @Test
  public void simple ()
  {
    final Configurations cfg = ConfigurationsBuilder.instance ()
                                                    .configuration (ConfigurationBuilder.identifier ("test").build ())
                                                    .pkg ("Test")
                                                    .build ();

    assertEquals (cfg.getConfiguration ().size (), 1);
    assertNull (cfg.getPackage ().getFirst ().getUrl ());
    assertEquals (cfg.getPackage ().getFirst ().getValue (), "Test");
  }
}
