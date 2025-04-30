package no.difi.vefa.validator.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.WillClose;

import com.google.common.io.ByteStreams;
import com.helger.asic.IAsicReader;

import no.difi.vefa.validator.api.IArtifactHolder;

/**
 * @author erlend
 */
public class ArtifactHolderImpl implements IArtifactHolder
{
  private final Map <String, byte []> content;

  private ArtifactHolderImpl (final Map <String, byte []> content)
  {
    this.content = content;
  }

  @Override
  public boolean exists (final String path)
  {
    return content.containsKey (path);
  }

  @Override
  public byte [] get (final String path)
  {
    return content.get (path);
  }

  @Override
  public InputStream getInputStream (final String path)
  {
    return new ByteArrayInputStream (content.get (path));
  }

  @Override
  public Set <String> getFilenames ()
  {
    return content.keySet ();
  }

  public static IArtifactHolder loadAsic (@WillClose final IAsicReader asicReader) throws IOException
  {
    final Map <String, byte []> content = new HashMap <> ();

    String filename;
    while ((filename = asicReader.getNextFile ()) != null)
    {
      // Keep source stream open
      content.put (filename, ByteStreams.toByteArray (asicReader.inputStream ()));
    }

    // Close asice-file
    asicReader.close ();

    return new ArtifactHolderImpl (content);
  }
}
