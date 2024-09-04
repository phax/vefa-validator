package no.difi.vefa.validator.build;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;

public class SimpleProjectTest
{

  @Inject
  private Cli cli;

  @Before
  public void before ()
  {
    Cli.getInjector ().injectMembers (this);
  }

  @Test
  public void simple () throws Exception
  {
    final Path path = Paths.get (getClass ().getResource ("/project/simple").toURI ());

    // assertFalse(Files.exists(path.resolve("target")));

    assertEquals (cli.perform (path.toString ()), 0);

    assertTrue (Files.exists (path.resolve ("target")));
  }

  @Test
  public void simpleWithTests () throws Exception
  {
    final Path path = Paths.get (getClass ().getResource ("/project/simple").toURI ());

    // assertFalse(Files.exists(path.resolve("target")));

    assertEquals (cli.perform ("-test", "-x", path.toString ()), 0);

    assertTrue (Files.exists (path.resolve ("target")));
  }
}
