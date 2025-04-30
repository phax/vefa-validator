package no.difi.vefa.validator.api;

import java.util.List;

import no.difi.xsd.vefa.validator._1.Report;

/**
 * Result of a validation.
 */
public interface IValidation
{
  /**
   * Document used for validation as represented in the validator.
   *
   * @return Document object.
   */
  VefaDocument getDocument ();

  /**
   * Report is the result of validation.
   *
   * @return Report
   */
  Report getReport ();

  /**
   * Nested validations of validation.
   *
   * @return List of validations or null if none available.
   */
  List <IValidation> getChildren ();

}
