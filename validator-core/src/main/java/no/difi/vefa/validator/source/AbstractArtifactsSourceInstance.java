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
import no.difi.vefa.validator.api.IArtifactsSourceInstance;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.util.ArtifactHolderImpl;
import no.difi.vefa.validator.util.JAXBHelper;
import no.difi.xsd.vefa.validator._1.Artifacts;

public abstract class AbstractArtifactsSourceInstance implements IArtifactsSourceInstance, Closeable
{
  protected static final AsicReaderFactory ASIC_READER_FACTORY = AsicReaderFactory.newFactory ();
  protected static final JAXBContext JAXB_CONTEXT = JAXBHelper.context (Artifacts.class);

  protected IProperties m_aProperties;
  protected Map <String, IArtifactHolder> m_aContent = new HashMap <> ();

  public AbstractArtifactsSourceInstance (final IProperties properties)
  {
    m_aProperties = properties;
  }

  protected void unpackAsic (final IAsicReader asicReader, final String targetName) throws IOException
  {
    m_aContent.put (targetName, ArtifactHolderImpl.loadAsic (asicReader));
  }

  @Override
  public Map <String, IArtifactHolder> getContent ()
  {
    return Collections.unmodifiableMap (m_aContent);
  }

  @Override
  public IArtifactHolder getContent (final String path)
  {
    return m_aContent.get (path);
  }

  @Override
  public void close ()
  {
    // No action.
  }
}
