package no.difi.vefa.validator.dist;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Cli
{
  public static final String LOG_PREFIX = "[phax@VEFA Validator] ";
  private static final String COMMAND_BUILD = "build";
  private static final String COMMAND_TESTER = "tester";

  public static void main (final String... args) throws Exception
  {
    if (args.length == 0)
    {
      log.error (LOG_PREFIX + "No command specified.");
      System.exit (1);
    }

    final String [] realArgs = new String [args.length - 1];
    for (int i = 1; i < args.length; i++)
      realArgs[i - 1] = args[i];

    switch (args[0])
    {
      case COMMAND_BUILD:
        no.difi.vefa.validator.build.Cli.main (realArgs);
        break;
      case COMMAND_TESTER:
        no.difi.vefa.validator.tester.Cli.main (realArgs);
        break;
      default:
        log.error (LOG_PREFIX +
                   "Unknown command: '" +
                   args[0] +
                   "'. Valid commands are: " +
                   COMMAND_BUILD +
                   " and " +
                   COMMAND_TESTER);
        System.exit (1);
        break;
    }
  }
}
