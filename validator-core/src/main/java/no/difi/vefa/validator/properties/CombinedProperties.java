package no.difi.vefa.validator.properties;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import no.difi.vefa.validator.api.IProperties;

/**
 * Implementation of Properties making it easy to access multiple instances of Properties.
 */
public class CombinedProperties implements IProperties
{
  private final IProperties [] m_aProps;

  /**
   * Allow combination of configs, the most specific first.
   *
   * @param properties
   *        List containing instances of Properties to be combined.
   */
  public CombinedProperties (@NonNull final IProperties @Nullable... properties)
  {
    m_aProps = properties;
  }

  private IProperties _detect (final String key)
  {
    for (final IProperties properties : m_aProps)
      if (properties != null && properties.contains (key))
        return properties;
    return null;
  }

  @Override
  public boolean contains (final String key)
  {
    final IProperties properties = _detect (key);
    return properties != null;
  }

  @Override
  public Object get (final String key, final Object defaultValue)
  {
    final IProperties properties = _detect (key);
    return properties == null ? defaultValue : properties.get (key, defaultValue);
  }

  @Override
  public boolean getBoolean (final String key, final boolean defaultValue)
  {
    final IProperties properties = _detect (key);
    return properties == null ? defaultValue : properties.getBoolean (key, defaultValue);
  }

  @Override
  public int getInteger (final String key, final int defaultValue)
  {
    final IProperties properties = _detect (key);
    return properties == null ? defaultValue : properties.getInteger (key, defaultValue);
  }

  @Override
  public String getString (final String key, final String defaultValue)
  {
    final IProperties properties = _detect (key);
    return properties == null ? defaultValue : properties.getString (key, defaultValue);
  }
}
