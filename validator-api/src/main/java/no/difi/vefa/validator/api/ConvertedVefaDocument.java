package no.difi.vefa.validator.api;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

/**
 * Representation of validation document where the document is converted before performing
 * validation.
 */
public class ConvertedVefaDocument extends VefaDocument
{
  /**
   * Holding the original document.
   */
  private final ByteArrayInputStream m_aSource;

  /**
   * @param inputStream
   *        InputStream containing the document used during validation.
   * @param source
   *        InputStream containing the original document before converting.
   * @param declaration
   *        Declaration identifier used to recognize rules.
   * @param expectation
   *        Expectations when performing validation of triggered rules.
   */
  public ConvertedVefaDocument (final ByteArrayInputStream inputStream,
                                final ByteArrayInputStream source,
                                final String declaration,
                                final IExpectation expectation)
  {
    this (inputStream, source, Collections.singletonList (declaration), expectation);
  }

  /**
   * @param inputStream
   *        InputStream containing the document used during validation.
   * @param source
   *        InputStream containing the original document before converting.
   * @param declarations
   *        Declaration identifiers used to recognize rules.
   * @param expectation
   *        Expectations when performing validation of triggered rules.
   */
  public ConvertedVefaDocument (final ByteArrayInputStream inputStream,
                                final ByteArrayInputStream source,
                                final List <String> declarations,
                                final IExpectation expectation)
  {
    super (inputStream, declarations, expectation);
    this.m_aSource = source;
  }

  /**
   * Returns the original document.
   *
   * @return Original document.
   */
  public ByteArrayInputStream getSource ()
  {
    return m_aSource;
  }
}
