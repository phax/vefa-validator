package no.difi.vefa.validator.build;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import jakarta.xml.bind.JAXBException;
import no.difi.vefa.validator.build.model.Build;
import no.difi.vefa.validator.build.module.BuildModule;
import no.difi.vefa.validator.build.module.SchematronModule;
import no.difi.vefa.validator.build.task.BuildTask;
import no.difi.vefa.validator.build.task.TestTask;
import no.difi.vefa.validator.module.SaxonModule;

public class Cli
{

  @Inject
  private Provider <BuildTask> buildTask;

  @Inject
  private Provider <TestTask> testTask;

  public static void main (final String... args) throws Exception
  {
    System.exit (getInjector ().getInstance (Cli.class).perform (args));
  }

  protected static Injector getInjector ()
  {
    return Guice.createInjector (new SaxonModule (), new BuildModule (), new SchematronModule ());
  }

  public int perform (final String... args) throws IOException, JAXBException, ParseException
  {
    final Options options = new Options ();
    options.addOption ("c", "config", true, "Config file");
    options.addOption ("t", "test", false, "Run tests");
    options.addOption ("b", "build", true, "Build identifier");
    options.addOption ("n", "name", true, "Name");
    options.addOption ("w", "weight", true, "Weight");
    options.addOption ("x", "exitcode", false, "Status in exit code - DEPRECATED");
    options.addOption ("p", "profile", true, "Buildconfig profile");
    options.addOption ("a", "source", true, "Source folder");
    options.addOption ("s", "site", true, "Create site - DEPRECATED");
    options.addOption (Option.builder ("target").desc ("Target folder").hasArg (true).build ());

    final CommandLineParser parser = new DefaultParser ();
    final CommandLine cmd = parser.parse (options, args);

    int result = 0;

    for (final String arg : cmd.getArgs ().length > 0 ? cmd.getArgs () : new String [] { "." })
    {
      final Build build = Build.of (arg, cmd);

      buildTask.get ().build (build);

      if (cmd.hasOption ("test"))
        result += testTask.get ().perform (build) ? 0 : 1;
    }

    return result;
  }
}
