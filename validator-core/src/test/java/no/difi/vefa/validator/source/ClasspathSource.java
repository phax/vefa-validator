package no.difi.vefa.validator.source;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IArtifactsSourceInstance;
import no.difi.vefa.validator.lang.VefaValidatorException;

public class ClasspathSource extends AbstractArtifactsSource
{
  private final String m_sFolder;

  public ClasspathSource (final String sFolder)
  {
    this.m_sFolder = sFolder;
  }

  @Override
  public IArtifactsSourceInstance createInstance (final IProperties properties) throws VefaValidatorException
  {
    return new ClasspathSourceInstance (properties, m_sFolder);
  }
}
