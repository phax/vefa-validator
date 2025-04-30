package no.difi.vefa.validator.api;

public interface IProperties
{
  boolean contains (String key);

  default Object get (final String key)
  {
    return get (key, null);
  }

  Object get (String key, Object defaultValue);

  default boolean getBoolean (final String key)
  {
    return getBoolean (key, false);
  }

  boolean getBoolean (String key, boolean defaultValue);

  default int getInteger (final String key)
  {
    return getInteger (key, 0);
  }

  int getInteger (String key, int defaultValue);

  default String getString (final String key)
  {
    return getString (key, null);
  }

  String getString (String key, String defaultValue);
}
