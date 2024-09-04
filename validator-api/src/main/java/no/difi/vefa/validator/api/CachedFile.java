package no.difi.vefa.validator.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import lombok.Getter;

/**
 * @author erlend
 */
@Getter
public class CachedFile {
  private String filename;

  private final byte[] content;

  private CachedFile(final byte[] content) {
    this.content = content;
  }

  private CachedFile(final String filename, final byte[] content) {
    this(content);
    this.filename = filename;
  }

  public InputStream getContentStream() {
    return new ByteArrayInputStream(content);
  }

  public static CachedFile of(final byte[] content) {
    return new CachedFile(content);
  }

  public static CachedFile of(final String filename, final byte[] content) {
    return new CachedFile(filename, content);
  }
}
