package no.difi.vefa.validator.declaration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.io.zip.ZipWalkerSettings;

import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.lang.VefaValidatorException;

/**
 * Test class for class {@link ZipDeclaration}.
 */
public class ZipDeclarationTest
{
  private static byte [] _zip (final String... aNamesAndContents) throws IOException
  {
    final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ();
    try (final ZipOutputStream aZOS = new ZipOutputStream (aBAOS))
    {
      for (int i = 0; i < aNamesAndContents.length; i += 2)
      {
        aZOS.putNextEntry (new ZipEntry (aNamesAndContents[i]));
        aZOS.write (aNamesAndContents[i + 1].getBytes (StandardCharsets.UTF_8));
        aZOS.closeEntry ();
      }
    }
    return aBAOS.toByteArray ();
  }

  private static List <CachedFile> _children (final byte [] aZip) throws VefaValidatorException
  {
    final List <CachedFile> ret = new ArrayList <> ();
    new ZipDeclaration ().children (new NonBlockingByteArrayInputStream (aZip)).forEach (ret::add);
    return ret;
  }

  private static void _assertFails (final byte [] aZip, final String sExpectedText)
  {
    try
    {
      _children (aZip);
      fail ();
    }
    catch (final VefaValidatorException ex)
    {
      assertTrue (ex.getMessage (), ex.getMessage ().contains (sExpectedText));
    }
  }

  @Test
  public void testChildren () throws Exception
  {
    final List <CachedFile> aChildren = _children (_zip ("a.xml", "<a/>", "dir/b.xml", "<b/>"));
    assertEquals (2, aChildren.size ());
    assertEquals ("a.xml", aChildren.get (0).getFilename ());
    assertEquals ("dir/b.xml", aChildren.get (1).getFilename ());
    assertArrayEquals ("<b/>".getBytes (StandardCharsets.UTF_8), aChildren.get (1).getContentStream ().readAllBytes ());
  }

  @Test
  public void testNestedZipIsAChild () throws Exception
  {
    // A nested ZIP is validated on the next nesting level, so it is returned as is
    final byte [] aInner = _zip ("inner.xml", "<i/>");
    final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ();
    try (final ZipOutputStream aZOS = new ZipOutputStream (aBAOS))
    {
      aZOS.putNextEntry (new ZipEntry ("inner.zip"));
      aZOS.write (aInner);
      aZOS.closeEntry ();
    }
    final List <CachedFile> aChildren = _children (aBAOS.toByteArray ());
    assertEquals (1, aChildren.size ());
    assertEquals ("inner.zip", aChildren.get (0).getFilename ());
    assertArrayEquals (aInner, aChildren.get (0).getContentStream ().readAllBytes ());
  }

  @Test
  public void testNotAZip ()
  {
    _assertFails ("not a zip".getBytes (StandardCharsets.UTF_8), "not a ZIP archive");
  }

  @Test
  public void testLimits () throws Exception
  {
    final byte [] aZip = _zip ("a.xml", "<a/>", "b.xml", "<b/>");
    final ZipWalkerSettings aOld = ZipDeclaration.getSettings ();
    try
    {
      ZipDeclaration.setSettings (aOld.getClone ().setMaxEntryCount (1));
      _assertFails (aZip, "more than 1 entries");

      ZipDeclaration.setSettings (aOld.getClone ().setMaxTotalUncompressedBytes (7));
      _assertFails (aZip, "exceeds 7 bytes");
    }
    finally
    {
      ZipDeclaration.setSettings (aOld);
    }
    assertEquals (2, _children (aZip).size ());
  }
}
