package no.difi.vefa.validator.properties;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.difi.vefa.validator.api.IProperties;

/**
 * Simple implementation of Properties using a HashMap to store values.
 */
public class SimpleProperties implements IProperties
{
  private static final Logger log = LoggerFactory.getLogger (SimpleProperties.class);

  private final Map <String, Object> values = new HashMap <> ();

  public SimpleProperties ()
  {}

  @NonNull
  public SimpleProperties set (final String key, final boolean value)
  {
    return set (key, Boolean.valueOf (value));
  }

  @NonNull
  public SimpleProperties set (final String key, final int value)
  {
    return set (key, Integer.valueOf (value));
  }

  @NonNull
  public SimpleProperties set (final String key, final Object value)
  {
    values.put (key, value);
    return this;
  }

  @Override
  public boolean contains (final String key)
  {
    return values.containsKey (key);
  }

  @Override
  public Object get (final String key, final Object defaultValue)
  {
    return values.containsKey (key) ? values.get (key) : defaultValue;
  }

  @Override
  public boolean getBoolean (final String key, final boolean defaultValue)
  {
    return values.containsKey (key) ? Boolean.parseBoolean (String.valueOf (values.get (key))) : defaultValue;
  }

  @Override
  public int getInteger (final String key, final int defaultValue)
  {
    try
    {
      if (values.containsKey (key))
        return Integer.parseInt (String.valueOf (values.get (key)));
    }
    catch (final NumberFormatException e)
    {
      log.error ("Error while casting '" + values.get (key) + "' to integer for key '" + key + "'.");
    }
    return defaultValue;
  }

  @Override
  public String getString (final String key, final String defaultValue)
  {
    return values.containsKey (key) ? String.valueOf (values.get (key)) : defaultValue;
  }
}
