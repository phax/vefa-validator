package no.difi.vefa.validator.api;

import java.io.OutputStream;

import no.difi.vefa.validator.lang.VefaValidatorException;

/**
 * Interface for classes creating presentation of business documents. The constructor must contain
 * no parameters.
 */
@Deprecated
public interface IRenderer
{
  /**
   * Writes presentation to a OutputStream given a business document.
   *
   * @param document
   *        Document to render.
   * @param properties
   *        Configuration for the presentation.
   * @param outputStream
   *        Stream to write presentation to.
   * @throws VefaValidatorException
   */
  void render (VefaDocument document, IProperties properties, OutputStream outputStream) throws VefaValidatorException;
}
