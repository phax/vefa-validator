package no.difi.vefa.validator.build.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;

import no.difi.vefa.validator.api.IValidation;
import no.difi.xsd.vefa.validator._1.Configurations;

public class Build
{
  private final Map <String, String> setting = new HashMap <> ();

  private final Path projectPath;
  private final Path [] sourcePath;
  private final Path targetFolder;

  private Configurations configurations;

  private final List <Path> testFolders = new ArrayList <> ();
  private final List <IValidation> testValidations = new ArrayList <> ();

  public static Build of (final String arg, final CommandLine cmd)
  {
    final Build build = new Build (Paths.get (arg),
                                   cmd.getOptionValue ("source", ""),
                                   cmd.getOptionValue ("target",
                                                       cmd.hasOption ("profile") ? String.format ("target-%s",
                                                                                                  cmd.getOptionValue ("profile"))
                                                                                 : "target"));
    build.setSetting ("config",
                      cmd.getOptionValue ("config",
                                          cmd.hasOption ("profile") ? String.format ("buildconfig-%s.xml",
                                                                                     cmd.getOptionValue ("profile"))
                                                                    : "buildconfig.xml"));
    build.setSetting ("name", cmd.getOptionValue ("name", "rules"));
    build.setSetting ("build", cmd.getOptionValue ("build", UUID.randomUUID ().toString ()));
    build.setSetting ("weight", cmd.getOptionValue ("weight", "0"));

    return build;
  }

  public Build (final Path projectPath)
  {
    this (projectPath, "", "target");
  }

  public Build (final Path projectPath, final String sourceFolder, final String targetFolder)
  {
    this.projectPath = projectPath;

    final String [] sfs = sourceFolder.split (",");
    this.sourcePath = new Path [sfs.length];
    for (int i = 0; i < sfs.length; i++)
      sourcePath[i] = projectPath.resolve (sfs[i]);

    this.targetFolder = projectPath.resolve (targetFolder);
  }

  public Build (final Path projectPath, final Path [] sourceFolder, final Path targetFolder)
  {
    this.projectPath = projectPath;
    this.sourcePath = sourceFolder;
    this.targetFolder = targetFolder;
  }

  public Configurations getConfigurations ()
  {
    if (configurations == null)
    {
      configurations = new Configurations ();
      configurations.setName (getSetting ("name"));
      configurations.setTimestamp (System.currentTimeMillis ());
    }

    return configurations;
  }

  public void setSetting (final String key, final String value)
  {
    setting.put (key, value);
  }

  public String getSetting (final String key)
  {
    return setting.get (key);
  }

  public void addTestFolder (final File testFolder)
  {
    addTestFolder (testFolder.toPath ());
  }

  public void addTestFolder (final Path testFolder)
  {
    testFolders.add (testFolder);
  }

  public void addTestValidation (final IValidation validation)
  {
    testValidations.add (validation);
  }

  public Path getProjectPath ()
  {
    return projectPath;
  }

  public Path [] getSourcePath ()
  {
    return sourcePath;
  }

  public Path getTargetFolder ()
  {
    return targetFolder;
  }

  public List <Path> getTestFolders ()
  {
    return testFolders;
  }

  public List <IValidation> getTestValidations ()
  {
    return testValidations;
  }
}
