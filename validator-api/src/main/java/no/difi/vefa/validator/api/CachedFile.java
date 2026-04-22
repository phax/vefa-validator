package no.difi.vefa.validator.api;

import java.io.InputStream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;

/**
 * @author erlend
 */
public final class CachedFile
{
  private final byte [] m_aContent;
  private final String m_sFilename;

  private CachedFile (@Nullable final String filename, final byte @NonNull [] content)
  {
    m_aContent = content;
    m_sFilename = filename;
  }

  @Nullable
  public String getFilename ()
  {
    return m_sFilename;
  }

  public byte [] getContent ()
  {
    return m_aContent;
  }

  @NonNull
  public InputStream getContentStream ()
  {
    return new NonBlockingByteArrayInputStream (m_aContent);
  }

  @NonNull
  public static CachedFile of (final byte @NonNull [] content)
  {
    return new CachedFile (null, content);
  }

  @NonNull
  public static CachedFile of (@NonNull final String filename, final byte @NonNull [] content)
  {
    return new CachedFile (filename, content);
  }
}
