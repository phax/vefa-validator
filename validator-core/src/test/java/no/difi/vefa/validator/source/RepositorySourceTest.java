package no.difi.vefa.validator.source;

import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Test;
import org.mockito.Mockito;

import no.difi.vefa.validator.api.Properties;
import no.difi.vefa.validator.lang.ValidatorException;

public class RepositorySourceTest
{

  // Dump test
  @Test
  public void simple ()
  {
    assertNotNull (RepositorySource.forTest ());
    assertNotNull (RepositorySource.forProduction ());
  }

  @Test (expected = ValidatorException.class)
  public void triggerException () throws ValidatorException
  {
    final RepositorySource source = new RepositorySource ((URI) null);
    source.createInstance (Mockito.mock (Properties.class));
  }
}
