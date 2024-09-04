package no.difi.vefa.validator.source;

import org.junit.Test;
import org.mockito.Mockito;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.lang.ValidatorException;

public class DirectorySourceTest
{

  @Test (expected = ValidatorException.class)
  public void triggerException () throws ValidatorException
  {
    final DirectorySource source = new DirectorySource (null);
    source.createInstance (Mockito.mock (IProperties.class));
  }
}
