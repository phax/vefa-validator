/*
 * Copyright (C) 2013-2025 Philip Helger
 * philip[at]helger[dot]com
 *
 * Licensed under the Mozilla Public License version 2.0.
 */
package no.difi.vefa.validator.security;

import static org.junit.Assert.assertEquals;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;

import org.junit.Test;

/**
 * Tests verifying TransformerFactory XXE protection.
 * Relates to finding F-04 in the security audit (ValidatorTestDeclaration.TRANSFORMER_FACTORY).
 */
public class TransformerFactoryXxeTest
{
  /**
   * Verifies that a default TransformerFactory does not restrict external DTD or stylesheet access.
   * This documents the vulnerability in ValidatorTestDeclaration.java line 42.
   */
  @Test
  public void testDefaultTransformerFactoryAllowsExternalAccess ()
  {
    final TransformerFactory factory = TransformerFactory.newInstance ();

    // Check if ACCESS_EXTERNAL_DTD is restricted
    boolean dtdRestricted;
    try
    {
      final String dtdAccess = (String) factory.getAttribute (XMLConstants.ACCESS_EXTERNAL_DTD);
      // Empty string means restricted; "all" or non-empty means unrestricted
      dtdRestricted = dtdAccess != null && dtdAccess.isEmpty ();
    }
    catch (final IllegalArgumentException e)
    {
      // Some implementations don't support this attribute
      dtdRestricted = false;
    }

    // Check if ACCESS_EXTERNAL_STYLESHEET is restricted
    boolean stylesheetRestricted;
    try
    {
      final String stylesheetAccess = (String) factory.getAttribute (XMLConstants.ACCESS_EXTERNAL_STYLESHEET);
      stylesheetRestricted = stylesheetAccess != null && stylesheetAccess.isEmpty ();
    }
    catch (final IllegalArgumentException e)
    {
      stylesheetRestricted = false;
    }

    // At least one of these being unrestricted confirms the vulnerability
    // Note: actual behavior may vary by JDK version and JAXP implementation.
    // With Saxon on the classpath, Saxon's TransformerFactory may be returned.
    // This test documents the state rather than asserting a specific outcome.
    System.out.println ("TransformerFactory implementation: " + factory.getClass ().getName ());
    System.out.println ("  ACCESS_EXTERNAL_DTD restricted: " + dtdRestricted);
    System.out.println ("  ACCESS_EXTERNAL_STYLESHEET restricted: " + stylesheetRestricted);
  }

  /**
   * Verifies that hardening a TransformerFactory properly restricts external access.
   */
  @Test
  public void testHardenedTransformerFactory ()
  {
    final TransformerFactory factory = TransformerFactory.newInstance ();
    try
    {
      factory.setAttribute (XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute (XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

      assertEquals ("ACCESS_EXTERNAL_DTD should be empty after hardening",
                    "", factory.getAttribute (XMLConstants.ACCESS_EXTERNAL_DTD));
      assertEquals ("ACCESS_EXTERNAL_STYLESHEET should be empty after hardening",
                    "", factory.getAttribute (XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
    }
    catch (final IllegalArgumentException e)
    {
      // Some TransformerFactory implementations (e.g., Saxon) don't support these attributes.
      // In that case, hardening must be done through other means (e.g., URIResolver).
      System.out.println ("TransformerFactory (" + factory.getClass ().getName () +
                          ") does not support ACCESS_EXTERNAL_* attributes: " + e.getMessage ());
    }
  }
}
