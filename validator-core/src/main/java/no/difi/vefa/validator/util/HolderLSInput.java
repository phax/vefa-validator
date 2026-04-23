package no.difi.vefa.validator.util;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;

public class HolderLSInput implements LSInput
{
  private final byte [] m_aContent;
  private final String m_sBaseURI;

  public HolderLSInput (final byte [] content, final String path)
  {
    m_aContent = content;
    m_sBaseURI = path;
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
    return new NonBlockingByteArrayInputStream (m_aContent);
  }

  @Override
  public void setByteStream (final InputStream byteStream)
  {
    // No action
  }

  @Override
  public String getStringData ()
  {
    return new String (m_aContent);
  }

  @Override
  public void setStringData (final String stringData)
  {
    // No action
  }

  @Override
  public String getSystemId ()
  {
    return "holder:" + m_sBaseURI;
  }

  @Override
  public void setSystemId (final String systemId)
  {
    // No action
  }

  @Override
  public String getPublicId ()
  {
    return "holder:" + m_sBaseURI;
  }

  @Override
  public void setPublicId (final String publicId)
  {
    // No action
  }

  @Override
  public String getBaseURI ()
  {
    return m_sBaseURI;
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
