package no.difi.vefa.validator.api;

import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.xsd.vefa.validator._1.StylesheetType;

/**
 * @author erlend
 */
@Deprecated
public interface IRendererFactory {

  /**
   * Method for preparing for use, can be seen as a constructor.
   *
   * @param stylesheetType Definition of the stylesheet defining the presenter.
   * @param path           Path of file used for presentation.
   */
  IRenderer prepare(StylesheetType stylesheetType, IArtifactHolder artifactHolder, String path) throws ValidatorException;

}
