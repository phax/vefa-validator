package no.difi.vefa.validator.api;

import java.io.InputStream;

import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;

/**
 * @author erlend
 */
public final class CachedFile
{
  private final byte [] m_aContent;
  private final String m_sFilename;

  private CachedFile (final String filename, final byte [] content)
  {
    m_aContent = content;
    m_sFilename = filename;
  }

  public String getFilename ()
  {
    return m_sFilename;
  }

  public byte [] getContent ()
  {
    return m_aContent;
  }

  public InputStream getContentStream ()
  {
    return new NonBlockingByteArrayInputStream (m_aContent);
  }

  public static CachedFile of (final byte [] content)
  {
    return new CachedFile (null, content);
  }

  public static CachedFile of (final String filename, final byte [] content)
  {
    return new CachedFile (filename, content);
  }
}
