package no.difi.vefa.validator.util;

import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Message;

public class VefaSaxonMessageListener implements Consumer <Message>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (VefaSaxonMessageListener.class);

  public static final VefaSaxonMessageListener INSTANCE = new VefaSaxonMessageListener ();

  protected VefaSaxonMessageListener ()
  {}

  public void accept (@NonNull final Message aMsg)
  {
    if (aMsg.isTerminate ())
      LOGGER.error (aMsg.getErrorCode () + " - " + aMsg.getContent ().getStringValue ());
    else
      LOGGER.info (aMsg.getErrorCode () + " - " + aMsg.getContent ().getStringValue ());
  }
}
