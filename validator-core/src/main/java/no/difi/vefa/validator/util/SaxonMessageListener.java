package no.difi.vefa.validator.util;

import javax.xml.transform.SourceLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.MessageListener2;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

public class SaxonMessageListener implements MessageListener2
{
  private static final Logger log = LoggerFactory.getLogger (SaxonMessageListener.class);

  public static final MessageListener2 INSTANCE = new SaxonMessageListener ();

  @Override
  public void message (final XdmNode content,
                       final QName errorCode,
                       final boolean terminate,
                       final SourceLocator locator)
  {
    if (terminate)
      log.warn ("{} - {}", errorCode, content.getStringValue ());
    else
      log.debug ("{} - {}", errorCode, content.getStringValue ());
  }
}
