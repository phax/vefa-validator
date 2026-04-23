/*
 * Copyright (C) 2013-2025 Philip Helger
 * philip[at]helger[dot]com
 *
 * Licensed under the Mozilla Public License version 2.0.
 */
package no.difi.vefa.validator.security;

import static org.junit.Assert.assertTrue;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.junit.Test;

import no.difi.vefa.validator.util.BlockingURIResolver;

/**
 * Tests verifying URI resolver security behavior. Relates to finding F-05 (HolderURIResolver
 * incomplete path traversal) in the security audit.
 */
public class UriResolverPathTraversalTest
{
  /**
   * Verifies that BlockingURIResolver blocks URLs containing ":/".
   */
  @Test
  public void testBlockingResolverBlocksUrls ()
  {
    final BlockingURIResolver resolver = new BlockingURIResolver ();

    // Should block http:// URLs
    try
    {
      resolver.resolve ("http://evil.com/payload.xsl", "");
      assertTrue ("BlockingURIResolver should throw for http:// URLs", false);
    }
    catch (final TransformerException e)
    {
      // Expected
    }

    // Should block file:// URLs
    try
    {
      resolver.resolve ("file:///etc/passwd", "");
      assertTrue ("BlockingURIResolver should throw for file:// URLs", false);
    }
    catch (final TransformerException e)
    {
      // Expected
    }
  }

  /**
   * Verifies that BlockingURIResolver allows relative paths (including traversals). This documents
   * a weakness: "../" paths without ":" are not blocked.
   */
  @Test
  public void testBlockingResolverAllowsRelativeTraversal () throws TransformerException
  {
    final BlockingURIResolver resolver = new BlockingURIResolver ();

    // Relative path traversal does NOT contain ":/" so it passes through
    final Source result = resolver.resolve ("../../etc/passwd", "");

    // BlockingURIResolver returns null for relative paths — it does not block them.
    // This means the caller's default resolver may handle the traversal.
    // The test documents this as a potential gap.
    assertTrue ("BlockingURIResolver allows relative path traversal (returns null, deferring to default)",
                result == null);
  }

  /**
   * Tests that BlockingURIResolver properly handles edge cases.
   */
  @Test
  public void testBlockingResolverEdgeCases () throws TransformerException
  {
    final BlockingURIResolver resolver = new BlockingURIResolver ();

    // ftp:/ should be blocked
    try
    {
      resolver.resolve ("ftp:/some/path", "");
      assertTrue ("BlockingURIResolver should block ftp:/", false);
    }
    catch (final TransformerException e)
    {
      // Expected
    }

    // Normal relative paths should pass (return null)
    final Source normalResult = resolver.resolve ("schema.xsd", "");
    assertTrue ("Normal relative paths should return null", normalResult == null);
  }
}
