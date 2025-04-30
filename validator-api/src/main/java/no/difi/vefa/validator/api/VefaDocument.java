package no.difi.vefa.validator.api;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

/**
 * Representation of validation document.
 */
public class VefaDocument
{
  /**
   * Document as #ByteArrayInputStream.
   */
  private final ByteArrayInputStream m_aBAIS;

  /**
   * Declaration identifier used to recognize rules.
   */
  private final List <String> m_aDeclarations;

  /**
   * Expectations when performing validation of triggered rules.
   */
  private final IExpectation m_aExpectation;

  public VefaDocument (final ByteArrayInputStream inputStream)
  {
    this (inputStream, (String) null, null);
  }

  /**
   * @param inputStream
   *        InputStream containing the document used during validation.
   * @param declaration
   *        Declaration identifier used to recognize rules.
   * @param expectation
   *        Expectations when performing validation of triggered rules.
   */
  public VefaDocument (final ByteArrayInputStream inputStream, final String declaration, final IExpectation expectation)
  {
    this (inputStream, Collections.singletonList (declaration), expectation);
  }

  /**
   * @param inputStream
   *        InputStream containing the document used during validation.
   * @param declarations
   *        Declaration identifiers used to recognize rules.
   * @param expectation
   *        Expectations when performing validation of triggered rules.
   */
  public VefaDocument (final ByteArrayInputStream inputStream,
                       final List <String> declarations,
                       final IExpectation expectation)
  {
    this.m_aBAIS = inputStream;
    this.m_aDeclarations = declarations;
    this.m_aExpectation = expectation;
  }

  /**
   * Declaration detected in document for validation.
   *
   * @return Declaration
   */
  public List <String> getDeclarations ()
  {
    return m_aDeclarations;
  }

  /**
   * Expectations detected in document for validation.
   *
   * @return Expectations
   */
  public IExpectation getExpectation ()
  {
    return m_aExpectation;
  }

  /**
   * Helper returning validated document as ByteArrayInputStream ready for use.
   *
   * @return Validated document
   */
  public ByteArrayInputStream getInputStream ()
  {
    m_aBAIS.reset ();
    return m_aBAIS;
  }
}
