package no.difi.vefa.validator.source;

import org.junit.Test;
import org.mockito.Mockito;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.lang.VefaValidatorException;

public class DirectorySourceTest
{

  @Test (expected = VefaValidatorException.class)
  public void triggerException () throws VefaValidatorException
  {
    final DirectorySource source = new DirectorySource (null);
    source.createInstance (Mockito.mock (IProperties.class));
  }
}
