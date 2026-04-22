package no.difi.vefa.validator.util;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;

public class HolderLSInput implements LSInput
{

  private final String file;

  private final byte [] content;

  public HolderLSInput (final byte [] content, final String path)
  {
    this.content = content;
    this.file = path;
  }

  @Override
  public Reader getCharacterStream ()
  {
    return null;
  }

  @Override
  public void setCharacterStream (final Reader characterStream)
  {
    // No action
  }

  @Override
  public InputStream getByteStream ()
  {
    return new NonBlockingByteArrayInputStream (content);
  }

  @Override
  public void setByteStream (final InputStream byteStream)
  {
    // No action
  }

  @Override
  public String getStringData ()
  {
    return new String (content);
  }

  @Override
  public void setStringData (final String stringData)
  {
    // No action
  }

  @Override
  public String getSystemId ()
  {
    return "holder:" + file;
  }

  @Override
  public void setSystemId (final String systemId)
  {
    // No action
  }

  @Override
  public String getPublicId ()
  {
    return "holder:" + file;
  }

  @Override
  public void setPublicId (final String publicId)
  {
    // No action
  }

  @Override
  public String getBaseURI ()
  {
    return file;
  }

  @Override
  public void setBaseURI (final String baseURI)
  {
    // No action
  }

  @Override
  public String getEncoding ()
  {
    return null;
  }

  @Override
  public void setEncoding (final String encoding)
  {
    // No action
  }

  @Override
  public boolean getCertifiedText ()
  {
    return false;
  }

  @Override
  public void setCertifiedText (final boolean certifiedText)
  {
    // No action
  }
}
