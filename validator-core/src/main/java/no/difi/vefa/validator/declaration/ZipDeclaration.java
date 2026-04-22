package no.difi.vefa.validator.declaration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.stream.NonClosingInputStream;
import com.helger.base.io.stream.StreamHelper;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.IDeclarationWithChildren;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.util.StreamUtils;

@Type ("zip")
public class ZipDeclaration implements IDeclarationWithChildren
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
      try (final ZipInputStream zipInputStream = new ZipInputStream (new NonBlockingByteArrayInputStream (content)))
      {
        final ZipEntry entry = zipInputStream.getNextEntry ();

        if ("mimetype".equals (entry.getName ()))
        {
          return Collections.singletonList (StreamHelper.getAllBytesAsString (zipInputStream,
                                                                              StandardCharsets.ISO_8859_1));
        }
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
    try (final ZipInputStream zipInputStream = new ZipInputStream (inputStream))
    {
      final List <CachedFile> ret = new ArrayList <> ();

      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry ()) != null)
      {
        ret.add (CachedFile.of (zipEntry.getName (),
                                StreamHelper.getAllBytes (new NonClosingInputStream (zipInputStream))));
      }

      return ret;
    }
    catch (final IOException e)
    {
      return null;
    }
  }
}
