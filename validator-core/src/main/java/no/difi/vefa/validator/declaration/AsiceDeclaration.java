package no.difi.vefa.validator.declaration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.asic.AsicReaderFactory;
import com.helger.asic.IAsicReader;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.stream.NonClosingInputStream;
import com.helger.base.io.stream.StreamHelper;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.IDeclarationWithChildren;
import no.difi.vefa.validator.api.IDeclarationWithConverter;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.lang.VefaValidatorException;

@Type ("zip.asice")
public class AsiceDeclaration extends AbstractXmlDeclaration implements
                              IDeclarationWithChildren,
                              IDeclarationWithConverter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AsiceDeclaration.class);
  private static final String MIME = "application/vnd.etsi.asic-e+zip";

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    if (content.length < 29 || content[28] != 0)
    {
      return false;
    }

    try (final ZipInputStream zipInputStream = new ZipInputStream (new NonBlockingByteArrayInputStream (content)))
    {
      final ZipEntry entry = zipInputStream.getNextEntry ();

      if ("mimetype".equals (entry.getName ()))
      {
        return MIME.equals (StreamHelper.getAllBytesAsString (zipInputStream, StandardCharsets.ISO_8859_1));
      }
    }
    catch (final IOException e)
    {
      LOGGER.error ("Error reading Asice as ZIP", e);
    }

    return false;
  }

  public List <String> detect (final InputStream contentStream, final List <String> parent)
  {
    return Collections.singletonList (MIME);
  }

  @Override
  public IExpectation expectations (final byte [] content)
  {
    return null;
  }

  public void convert (final InputStream inputStream, final OutputStream outputStream) throws VefaValidatorException
  {
    StreamHelper.copyByteStream ().from (inputStream).closeFrom (false).to (outputStream).closeTo (false).build ();
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
        files.add (CachedFile.of (filename,
                                  StreamHelper.getAllBytes (new NonClosingInputStream (asicReader.inputStream ()))));
      }

      return files;
    }
    catch (final IOException e)
    {
      return null;
    }
  }
}
