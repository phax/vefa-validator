package no.difi.vefa.validator.util;

import java.util.Objects;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VefaSaxonErrorListener implements ErrorListener
{
  public static final VefaSaxonErrorListener INSTANCE = new VefaSaxonErrorListener ();

  private static final Logger log = LoggerFactory.getLogger (VefaSaxonErrorListener.class);

  @Override
  public void warning (final TransformerException exception)
  {
    if (exception.getMessage ()
                 .contains ("The expression can succeed only if the supplied value is an empty sequence."))
      log.info (exception.getMessage ());
    else
      if (exception.getMessage ().contains ("will never select anything"))
        log.info (exception.getMessage ());
      else
        log.warn (exception.getMessage ());
  }

  @Override
  public void error (final TransformerException exception) throws TransformerException
  {
    if (exception.getMessage ().contains ("Ambiguous rule match for"))
      log.info (exception.getMessage (), exception);
    else
      log.error (exception.getMessage (), exception);
  }

  @Override
  public void fatalError (final TransformerException exception)
  {
    if (Objects.nonNull (exception.getMessage ()) &&
      exception.getMessage ().startsWith ("Exception thrown by URIResolver"))
      log.error (exception.getCause ().getMessage ());
    else
      log.error (exception.getMessage (), exception);
  }
}
