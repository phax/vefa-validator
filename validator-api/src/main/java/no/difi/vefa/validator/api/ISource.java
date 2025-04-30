package no.difi.vefa.validator.api;

import no.difi.vefa.validator.lang.VefaValidatorException;

/**
 * Source for validation artifacts.
 */
public interface ISource {

    /**
     * Instance of source with validation artifacts ready for use.
     *
     * @throws VefaValidatorException
     * @return Instance containing validation artifacts.
     */
    ISourceInstance createInstance(IProperties properties) throws VefaValidatorException;

}
