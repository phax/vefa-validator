package no.difi.vefa.validator.declaration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.google.common.base.CharMatcher;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.helger.asic.AsicReaderFactory;
import com.helger.asic.IAsicReader;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.IDeclarationWithChildren;
import no.difi.vefa.validator.api.IDeclarationWithConverter;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.lang.VefaValidatorException;

@Type ("xml.asice")
public class AsiceXmlDeclaration extends AbstractXmlDeclaration implements
                                 IDeclarationWithConverter,
                                 IDeclarationWithChildren
{
  private static final String NAMESPACE = "urn:etsi.org:specification:02918:v1.2.1::asic";
  private static final String MIME = "application/vnd.etsi.asic-e+zip";

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    return NAMESPACE.equals (parent.get (0));
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
  public void convert (final InputStream inputStream, final OutputStream outputStream) throws VefaValidatorException
  {
    try
    {
      final XMLStreamReader source = XML_INPUT_FACTORY.createXMLStreamReader (inputStream);
      try (final NonBlockingByteArrayOutputStream byteArrayOutputStream = new NonBlockingByteArrayOutputStream ())
      {
        do
        {
          if (source.getEventType () == XMLStreamConstants.CHARACTERS)
            byteArrayOutputStream.write (source.getText ().getBytes ());
        } while (source.hasNext () && source.next () > 0);

        outputStream.write (BaseEncoding.base64 ()
                                        .decode (CharMatcher.whitespace ()
                                                            .removeFrom (byteArrayOutputStream.getAsString (StandardCharsets.ISO_8859_1))));
      }
    }
    catch (IOException | XMLStreamException e)
    {
      throw new VefaValidatorException (e.getMessage (), e);
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
