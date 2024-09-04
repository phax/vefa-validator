package no.difi.vefa.validator.api;

import no.difi.vefa.validator.lang.ValidatorException;

/**
 * Source for validation artifacts.
 */
public interface ISource {

    /**
     * Instance of source with validation artifacts ready for use.
     *
     * @throws ValidatorException
     * @return Instance containing validation artifacts.
     */
    ISourceInstance createInstance(IProperties properties) throws ValidatorException;

}
