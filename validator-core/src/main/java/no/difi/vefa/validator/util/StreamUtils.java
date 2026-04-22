package no.difi.vefa.validator.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.helger.base.io.stream.StreamHelper;

public class StreamUtils
{
  /**
   * Will read all from stream and reset it.
   *
   * @param inputStream
   *        Input stream to read from (must support mark)
   * @return Read data
   * @throws IOException
   */
  public static byte [] readAllAndReset (final InputStream inputStream) throws IOException
  {
    final byte [] bytes = StreamHelper.getAllBytes (inputStream);
    inputStream.reset ();
    return bytes;
  }

  public static byte [] read50KAndReset (final InputStream inputStream) throws IOException
  {
    // Read at most 50K
    return readAndReset (inputStream, 50 * 1024);
  }

  /**
   * Will read parts from stream and reset it.
   *
   * @param inputStream
   *        A markable stream
   * @param length
   *        Read bytes
   * @return ready bytes
   * @throws IOException
   *         in case of IO error
   */
  public static byte [] readAndReset (final InputStream inputStream, final int length) throws IOException
  {
    byte [] bytes = new byte [length];

    inputStream.mark (length);
    final int numberOfReadBytes = inputStream.read (bytes);
    inputStream.reset ();

    if (numberOfReadBytes == -1)
    {
      throw new IOException ("Empty file");
    }

    bytes = Arrays.copyOfRange (bytes, 0, numberOfReadBytes);
    return bytes;
  }
}
