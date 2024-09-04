package no.difi.vefa.validator.declaration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.io.ByteStreams;
import com.helger.asic.AsicReaderFactory;
import com.helger.asic.IAsicReader;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.IDeclarationWithChildren;
import no.difi.vefa.validator.api.IDeclarationWithConverter;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.lang.ValidatorException;

@Type ("zip.asice")
public class AsiceDeclaration extends AbstractXmlDeclaration implements
                              IDeclarationWithChildren,
                              IDeclarationWithConverter
{

  private static final String MIME = "application/vnd.etsi.asic-e+zip";

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    if (content[28] != 0)
      return false;

    try
    {
      final ZipInputStream zipInputStream = new ZipInputStream (new ByteArrayInputStream (content));
      final ZipEntry entry = zipInputStream.getNextEntry ();

      if ("mimetype".equals (entry.getName ()))
        return MIME.equals (new String (ByteStreams.toByteArray (zipInputStream)));
    }
    catch (final IOException e)
    {
      // No action.
    }

    return false;
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent)
  {
    return Collections.singletonList (MIME);
  }

  @Override
  public IExpectation expectations (final byte [] content)
  {
    return null;
  }

  @Override
  public void convert (final InputStream inputStream, final OutputStream outputStream) throws ValidatorException
  {
    try
    {
      ByteStreams.copy (inputStream, outputStream);
    }
    catch (final IOException e)
    {
      throw new ValidatorException (e.getMessage (), e);
    }
  }

  @Override
  public Iterable <CachedFile> children (final InputStream inputStream)
  {
    try
    {
      final IAsicReader asicReader = AsicReaderFactory.newFactory ().open (inputStream);
      final List <CachedFile> files = new ArrayList <> ();

      String filename;
      while ((filename = asicReader.getNextFile ()) != null)
      {
        files.add (CachedFile.of (filename, ByteStreams.toByteArray (asicReader.inputStream ())));
      }

      return files;
    }
    catch (final IOException e)
    {
      return null;
    }
  }
}
