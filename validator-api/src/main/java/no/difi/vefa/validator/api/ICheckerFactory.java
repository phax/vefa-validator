package no.difi.vefa.validator.api;

import no.difi.vefa.validator.lang.ValidatorException;

/**
 * @author erlend
 */
public interface ICheckerFactory {
  IChecker prepare(IArtifactHolder artifactHolder, String path) throws ValidatorException;

}
