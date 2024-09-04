package no.difi.vefa.validator.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import no.difi.vefa.validator.api.Properties;
import no.difi.vefa.validator.properties.SimpleProperties;

public class SimplePropertiesTest
{

  @Test
  public void simple ()
  {
    final Properties properties = new SimpleProperties ().set ("some.string", "Hello World!")
                                                         .set ("some.number", 1)
                                                         .set ("some.boolean", true)
                                                         .set ("some.object", Long.valueOf (123));

    assertTrue (properties.contains ("some.string"));
    assertTrue (properties.contains ("some.number"));
    assertTrue (properties.contains ("some.boolean"));
    assertTrue (properties.contains ("some.object"));

    assertFalse (properties.contains ("some.other.string"));
    assertFalse (properties.contains ("some.other.number"));
    assertFalse (properties.contains ("some.other.boolean"));
    assertFalse (properties.contains ("some.other.object"));

    assertEquals (properties.getString ("some.string"), "Hello World!");
    assertEquals (properties.getInteger ("some.number"), 1);
    assertEquals (properties.getBoolean ("some.boolean"), true);
    assertEquals (properties.get ("some.object").getClass (), Long.class);

    assertEquals (properties.getString ("some.string", "No no!"), "Hello World!");
    assertEquals (properties.getInteger ("some.number", 0), 1);
    assertEquals (properties.getBoolean ("some.boolean", false), true);
    assertEquals (properties.get ("some.object", null).getClass (), Long.class);

    assertEquals (properties.getString ("some.other.string"), null);
    assertEquals (properties.getInteger ("some.other.number"), 0);
    assertEquals (properties.getBoolean ("some.other.boolean"), false);
    assertEquals (properties.get ("some.other.object"), null);

    assertEquals (properties.getString ("some.other.string", "No no!"), "No no!");
    assertEquals (properties.getInteger ("some.other.number", 0), 0);
    assertEquals (properties.getBoolean ("some.other.boolean", true), true);
    assertEquals (properties.get ("some.other.object", null), null);

    assertEquals (properties.getInteger ("some.boolean", 100), 100);
  }

}
