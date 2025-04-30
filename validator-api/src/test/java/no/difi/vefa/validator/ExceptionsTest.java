package no.difi.vefa.validator;

import org.junit.Test;

import no.difi.vefa.validator.lang.UnknownDocumentTypeException;
import no.difi.vefa.validator.lang.VefaValidatorException;

public class ExceptionsTest {

  @Test(expected = UnknownDocumentTypeException.class)
  public void unknownDocumentType() throws UnknownDocumentTypeException {
    throw new UnknownDocumentTypeException("test");
  }

  @Test(expected = VefaValidatorException.class)
  public void validator1() throws VefaValidatorException {
    throw new VefaValidatorException("test");
  }

  @Test(expected = VefaValidatorException.class)
  public void validator2() throws VefaValidatorException {
    throw new VefaValidatorException("test", null);
  }
}
