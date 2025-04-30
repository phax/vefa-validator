package no.difi.vefa.validator.checker;

import org.junit.Test;

import no.difi.vefa.validator.lang.VefaValidatorException;

public class SchematronXsltCheckerTest
{

  @Test (expected = VefaValidatorException.class)
  public void simpleTriggerException () throws Exception
  {
    new SchematronXsltCheckerFactory ().prepare (null, null);
  }
}
