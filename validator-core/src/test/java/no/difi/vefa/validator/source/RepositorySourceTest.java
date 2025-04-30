package no.difi.vefa.validator.source;

import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Test;
import org.mockito.Mockito;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.lang.VefaValidatorException;

public class RepositorySourceTest
{

  // Dump test
  @Test
  public void simple ()
  {
    assertNotNull (RepositorySource.forTest ());
    assertNotNull (RepositorySource.forProduction ());
  }

  @Test (expected = VefaValidatorException.class)
  public void triggerException () throws VefaValidatorException
  {
    final RepositorySource source = new RepositorySource ((URI) null);
    source.createInstance (Mockito.mock (IProperties.class));
  }
}
