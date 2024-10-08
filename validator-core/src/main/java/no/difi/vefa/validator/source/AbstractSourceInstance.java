package no.difi.vefa.validator.source;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.helger.asic.AsicReaderFactory;
import com.helger.asic.IAsicReader;

import jakarta.xml.bind.JAXBContext;
import no.difi.vefa.validator.api.IArtifactHolder;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.ISourceInstance;
import no.difi.vefa.validator.util.ArtifactHolderImpl;
import no.difi.vefa.validator.util.JAXBHelper;
import no.difi.xsd.vefa.validator._1.Artifacts;

public abstract class AbstractSourceInstance implements ISourceInstance, Closeable
{
  protected static final AsicReaderFactory ASIC_READER_FACTORY = AsicReaderFactory.newFactory ();
  protected static final JAXBContext JAXB_CONTEXT = JAXBHelper.context (Artifacts.class);

  protected IProperties properties;

  protected Map <String, IArtifactHolder> content = new HashMap <> ();

  public AbstractSourceInstance (final IProperties properties)
  {
    this.properties = properties;
  }

  protected void unpackContainer (final IAsicReader asicReader, final String targetName) throws IOException
  {
    content.put (targetName, ArtifactHolderImpl.load (asicReader));
  }

  @Override
  public Map <String, IArtifactHolder> getContent ()
  {
    return Collections.unmodifiableMap (content);
  }

  @Override
  public IArtifactHolder getContent (final String path)
  {
    return content.get (path);
  }

  @Override
  public void close ()
  {
    // No action.
  }
}
