package no.difi.vefa.validator.expectation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.api.Section;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;

public abstract class AbstractExpectation implements IExpectation
{
  protected String description;
  protected List <String> scopes = new ArrayList <> ();
  protected Map <String, Integer> successes = new HashMap <> ();
  protected Map <String, Integer> warnings = new HashMap <> ();
  protected Map <String, Integer> errors = new HashMap <> ();
  protected Map <String, Integer> fatals = new HashMap <> ();

  @Override
  public String getDescription ()
  {
    return description;
  }

  private static boolean _isExpected (final String identifier, final Map <String, Integer> target)
  {
    if (!target.containsKey (identifier) || target.get (identifier) == 0)
      return false;
    target.put (identifier, target.get (identifier) - 1);
    return true;
  }

  @Override
  public void filterFlag (final AssertionType assertionType)
  {
    if (assertionType.getFlag () == null)
      return;

    if (!scopes.isEmpty () && !scopes.contains (assertionType.getIdentifier ()))
    {
      assertionType.setFlag (null);
    }
    else
      if (successes.containsKey (assertionType.getIdentifier ()))
      {
        assertionType.setFlag (FlagType.ERROR);
        successes.put (assertionType.getIdentifier (), successes.get (assertionType.getIdentifier ()) + 1);
      }
      else
      {
        switch (assertionType.getFlag ())
        {
          case FATAL:
            if (_isExpected (assertionType.getIdentifier (), fatals))
              assertionType.setFlag (FlagType.EXPECTED);
            break;
          case ERROR:
            if (_isExpected (assertionType.getIdentifier (), errors))
              assertionType.setFlag (FlagType.EXPECTED);
            break;
          case WARNING:
            if (_isExpected (assertionType.getIdentifier (), warnings))
              assertionType.setFlag (FlagType.EXPECTED);
            break;
        }
      }
  }

  @Override
  public void verify (final Section section)
  {
    for (final var e : fatals.entrySet ())
    {
      final String key = e.getKey ();
      final int nCount = e.getValue ().intValue ();
      if (nCount > 0)
        section.add ("SYSTEM-004", "Rule '" + key + "' (FATAL) not fired " + nCount + " time(s).", FlagType.ERROR);
    }
    for (final var e : errors.entrySet ())
    {
      final String key = e.getKey ();
      final int nCount = e.getValue ().intValue ();
      if (nCount > 0)
        section.add ("SYSTEM-005", "Rule '" + key + "' (ERROR) not fired " + nCount + " time(s).", FlagType.ERROR);
    }
    for (final var e : warnings.entrySet ())
    {
      final String key = e.getKey ();
      final int nCount = e.getValue ().intValue ();
      if (nCount > 0)
        section.add ("SYSTEM-006", "Rule '" + key + "' (WARNING) not fired " + nCount + " time(s).", FlagType.ERROR);
    }
    for (final var e : successes.entrySet ())
    {
      final String key = e.getKey ();
      final int nCount = e.getValue ().intValue ();
      if (nCount == 1)
        section.add (key, "Rule not fired.", FlagType.SUCCESS);
      else
        section.add ("SYSTEM-009", "Rule '" + key + "' fired " + (nCount - 1) + " time(s).", FlagType.ERROR);
    }
  }
}
