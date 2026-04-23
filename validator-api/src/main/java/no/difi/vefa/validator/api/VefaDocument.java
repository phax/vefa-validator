package no.difi.vefa.validator.api;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;

/**
 * Representation of validation document.
 */
public class VefaDocument
{
  /**
   * Document as #ByteArrayInputStream.
   */
  private final NonBlockingByteArrayInputStream m_aBAIS;

  /**
   * Declaration identifier used to recognize rules.
   */
  private final List <String> m_aDeclarations;

  /**
   * Expectations when performing validation of triggered rules.
   */
  private final IExpectation m_aExpectation;

  public VefaDocument (final NonBlockingByteArrayInputStream inputStream)
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
  public VefaDocument (final NonBlockingByteArrayInputStream inputStream,
                       @Nullable final String declaration,
                       @Nullable final IExpectation expectation)
  {
    this (inputStream,
          declaration == null ? Collections.emptyList () : Collections.singletonList (declaration),
          expectation);
  }

  /**
   * @param inputStream
   *        InputStream containing the document used during validation.
   * @param declarations
   *        Declaration identifiers used to recognize rules.
   * @param expectation
   *        Expectations when performing validation of triggered rules.
   */
  public VefaDocument (final NonBlockingByteArrayInputStream inputStream,
                       final List <String> declarations,
                       @Nullable final IExpectation expectation)
  {
    m_aBAIS = inputStream;
    m_aDeclarations = declarations;
    m_aExpectation = expectation;
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
  public NonBlockingByteArrayInputStream getInputStream ()
  {
    m_aBAIS.reset ();
    return m_aBAIS;
  }
}
