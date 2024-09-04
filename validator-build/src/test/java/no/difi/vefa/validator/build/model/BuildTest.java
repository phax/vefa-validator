package no.difi.vefa.validator.build.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.mockito.Mockito;

import no.difi.vefa.validator.api.Validation;

public class BuildTest
{

  @Test
  public void simple () throws Exception
  {
    final Path path = Paths.get (getClass ().getResource ("/projectFolder").toURI ());
    final Build build = new Build (path);
    build.setSetting ("name", "Test");

    assertNotNull (build.getConfigurations ());
    assertEquals (build.getConfigurations ().getName (), "Test");

    assertEquals (build.getProjectPath (), path);
    assertEquals (build.getTargetFolder (), path.resolve ("target"));

    assertEquals (build.getTestFolders ().size (), 0);
    build.addTestFolder (path.resolve ("testFolder").toFile ());
    assertEquals (build.getTestFolders ().size (), 1);
    assertEquals (build.getTestFolders ().get (0), path.resolve ("testFolder"));

    assertEquals (build.getTestValidations ().size (), 0);
    build.addTestValidation (Mockito.mock (Validation.class));
    assertEquals (build.getTestValidations ().size (), 1);
  }
}
