package no.difi.vefa.validator.checker;

import org.junit.Test;

import no.difi.vefa.validator.lang.ValidatorException;

public class XsdCheckerTest
{

  @Test (expected = ValidatorException.class)
  public void simpleTriggerException () throws Exception
  {
    new XsdCheckerFactory ().prepare (null, null);
  }
}
