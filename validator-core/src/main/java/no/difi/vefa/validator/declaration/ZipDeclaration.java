package no.difi.vefa.validator.declaration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.io.ByteStreams;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.IDeclaration;
import no.difi.vefa.validator.api.IDeclarationWithChildren;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.util.StreamUtils;

@Type ("zip")
public class ZipDeclaration implements IDeclaration, IDeclarationWithChildren
{

  private static final byte [] STARTS_WITH = { 0x50, 0x4B, 0x03, 0x04 };

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    return Arrays.equals (STARTS_WITH, Arrays.copyOfRange (content, 0, STARTS_WITH.length));
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent)
  {
    try
    {

      final byte [] content = StreamUtils.read50KAndReset (contentStream);
      final ZipInputStream zipInputStream = new ZipInputStream (new ByteArrayInputStream (content));
      final ZipEntry entry = zipInputStream.getNextEntry ();

      if ("mimetype".equals (entry.getName ()))
      {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream ();
        ByteStreams.copy (zipInputStream, byteArrayOutputStream);
        return Collections.singletonList (byteArrayOutputStream.toString ());
      }
    }
    catch (final IOException e)
    {
      // No action
    }

    return Collections.singletonList ("application/zip");
  }

  @Override
  public IExpectation expectations (final byte [] content)
  {
    return null;
  }

  @Override
  public Iterable <CachedFile> children (final InputStream inputStream)
  {
    try
    {
      final ZipInputStream zipInputStream = new ZipInputStream (inputStream);
      final List <CachedFile> files = new ArrayList <> ();

      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry ()) != null)
      {
        files.add (CachedFile.of (zipEntry.getName (), ByteStreams.toByteArray (zipInputStream)));
      }

      return files;
    }
    catch (final IOException e)
    {
      return null;
    }
  }
}
