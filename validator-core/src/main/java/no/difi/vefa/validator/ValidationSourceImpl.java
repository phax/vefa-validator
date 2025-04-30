package no.difi.vefa.validator;

import java.io.InputStream;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IValidationSource;

final class ValidationSourceImpl implements IValidationSource
{
  private final InputStream inputStream;
  private final IProperties properties;

  public ValidationSourceImpl (final InputStream inputStream, final IProperties properties)
  {
    this.inputStream = inputStream;
    this.properties = properties;
  }

  @Override
  public InputStream getInputStream ()
  {
    return inputStream;
  }

  @Override
  public IProperties getProperties ()
  {
    return properties;
  }
}
