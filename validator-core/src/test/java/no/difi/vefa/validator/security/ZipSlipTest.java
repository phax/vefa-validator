/*
 * Copyright (C) 2013-2025 Philip Helger
 * philip[at]helger[dot]com
 *
 * Licensed under the Mozilla Public License version 2.0.
 */
package no.difi.vefa.validator.security;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.declaration.ZipDeclaration;

/**
 * Tests demonstrating the Zip Slip vulnerability in ZipDeclaration.children().
 * Relates to finding F-03 in the security audit.
 */
public class ZipSlipTest
{
  /**
   * Creates a malicious ZIP archive containing an entry with a path traversal name.
   * Verifies that ZipDeclaration.children() does NOT sanitize the entry name,
   * thus confirming the Zip Slip vulnerability.
   */
  @Test
  public void testZipSlipPathTraversal () throws Exception
  {
    // Create a ZIP archive with a path-traversal entry name
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    try (final ZipOutputStream zos = new ZipOutputStream (baos))
    {
      // This is the malicious entry — a filename like "../../etc/passwd"
      final ZipEntry maliciousEntry = new ZipEntry ("../../etc/passwd");
      zos.putNextEntry (maliciousEntry);
      zos.write ("malicious content".getBytes ());
      zos.closeEntry ();

      // A normal entry for contrast
      final ZipEntry normalEntry = new ZipEntry ("normal.xml");
      zos.putNextEntry (normalEntry);
      zos.write ("<root/>".getBytes ());
      zos.closeEntry ();
    }

    final ZipDeclaration declaration = new ZipDeclaration ();
    final Iterable <CachedFile> children = declaration.children (new ByteArrayInputStream (baos.toByteArray ()));

    // The vulnerability: the path-traversal filename is preserved without sanitization
    boolean foundTraversalPath = false;
    for (final CachedFile child : children)
    {
      if (child.getFilename () != null && child.getFilename ().contains (".."))
      {
        foundTraversalPath = true;
      }
    }

    // This assertion PASSES, confirming the vulnerability exists.
    // After remediation, this should be changed to assertFalse.
    assertTrue ("ZipDeclaration.children() should reject path traversal entries (vulnerability confirmed)",
                foundTraversalPath);
  }
}
