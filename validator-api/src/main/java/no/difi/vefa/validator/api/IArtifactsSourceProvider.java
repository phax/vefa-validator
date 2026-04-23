package no.difi.vefa.validator.api;

import no.difi.vefa.validator.lang.VefaValidatorException;

/**
 * Source for validation artifacts.
 */
public interface IArtifactsSourceProvider
{
  /**
   * Instance of source with validation artifacts ready for use.
   *
   * @param properties
   *        props
   * @throws VefaValidatorException
   * @return Instance containing validation artifacts.
   */
  IArtifactsSourceInstance createInstance (IProperties properties) throws VefaValidatorException;
}
