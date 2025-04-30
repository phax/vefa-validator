package no.difi.vefa.validator.checker;

import org.junit.Test;

import no.difi.vefa.validator.lang.VefaValidatorException;

public class XsdCheckerTest
{

  @Test (expected = VefaValidatorException.class)
  public void simpleTriggerException () throws Exception
  {
    new XsdCheckerFactory ().prepare (null, null);
  }
}
