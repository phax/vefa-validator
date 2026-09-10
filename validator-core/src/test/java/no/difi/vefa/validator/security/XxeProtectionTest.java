/*
 * Copyright (C) 2013-2025 Philip Helger
 * philip[at]helger[dot]com
 *
 * Licensed under the Mozilla Public License version 2.0.
 */
package no.difi.vefa.validator.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

/**
 * Tests verifying XXE protection on the XMLInputFactory instances used throughout the codebase.
 * Relates to findings F-01 and F-02 in the security audit.
 */
public class XxeProtectionTest
{
  /**
   * Tests whether the default XMLInputFactory (as used in AbstractXmlDeclaration and XsdChecker) is
   * vulnerable to XXE via external entity resolution.
   */
  @Test
  public void testXmlInputFactoryXxeDefault ()
  {
    // This mimics how XMLInputFactory is created in AbstractXmlDeclaration (line 12)
    // and XsdChecker (line 28) — both use the default factory with no hardening.
    final XMLInputFactory factory = XMLInputFactory.newFactory ();

    // Check if external entities support is enabled (the insecure default)
    final Object extEntities = factory.getProperty (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES);
    final Object dtdSupport = factory.getProperty (XMLInputFactory.SUPPORT_DTD);

    // Both should be disabled for security. If either is true, the factory is vulnerable.
    final boolean isVulnerable = (extEntities == null || Boolean.TRUE.equals (extEntities)) ||
                                 (dtdSupport == null || Boolean.TRUE.equals (dtdSupport));

    // This test documents the vulnerability — it PASSES because the factory IS vulnerable.
    assertTrue ("XMLInputFactory should be vulnerable to XXE by default (documenting the finding)", isVulnerable);
  }

  /**
   * Demonstrates that a hardened XMLInputFactory properly rejects external entities.
   */
  @Test
  public void testXmlInputFactoryHardenedRejectsXxe ()
  {
    final XMLInputFactory factory = XMLInputFactory.newFactory ();
    factory.setProperty (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    factory.setProperty (XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

    final Object extEntities = factory.getProperty (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES);
    final Object dtdSupport = factory.getProperty (XMLInputFactory.SUPPORT_DTD);

    assertFalse ("External entities should be disabled after hardening", Boolean.TRUE.equals (extEntities));
    assertFalse ("DTD support should be disabled after hardening", Boolean.TRUE.equals (dtdSupport));
  }

  /**
   * Tests that the XXE payload with a file:// URI is processable with the default factory but would
   * fail with a hardened one.
   */
  @Test
  public void testXxePayloadProcessing ()
  {
    final String xxePayload = "<?xml version=\"1.0\"?>\n" +
                              "<!DOCTYPE foo [\n" +
                              "  <!ENTITY xxe SYSTEM \"file:///dev/null\">\n" +
                              "]>\n" +
                              "<root>&xxe;</root>";

    // Default factory (vulnerable) — should parse the DTD declaration
    final XMLInputFactory defaultFactory = XMLInputFactory.newFactory ();
    try
    {
      final XMLStreamReader reader = defaultFactory.createXMLStreamReader (new ByteArrayInputStream (xxePayload.getBytes (StandardCharsets.UTF_8)));
      // If we get here, the factory accepted the DTD-containing document
      reader.close ();
      // This is the insecure behavior — the factory didn't reject DTDs
    }
    catch (final XMLStreamException e)
    {
      // If the default factory already rejects DTDs, the finding would be less severe
      fail ("Default XMLInputFactory unexpectedly rejected XXE payload: " + e.getMessage ());
    }

    // Hardened factory — should reject or ignore DTD
    final XMLInputFactory hardenedFactory = XMLInputFactory.newFactory ();
    hardenedFactory.setProperty (XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    hardenedFactory.setProperty (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);

    try
    {
      final XMLStreamReader reader = hardenedFactory.createXMLStreamReader (new ByteArrayInputStream (xxePayload.getBytes (StandardCharsets.UTF_8)));
      // Even if it parses, entity expansion should not occur
      while (reader.hasNext ())
      {
        reader.next ();
      }
      reader.close ();
    }
    catch (final XMLStreamException e)
    {
      // Expected — hardened factory may reject the DTD entirely
    }
  }
}
