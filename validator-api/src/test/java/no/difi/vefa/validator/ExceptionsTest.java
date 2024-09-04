package no.difi.vefa.validator;

import org.junit.Test;

import no.difi.vefa.validator.lang.UnknownDocumentTypeException;
import no.difi.vefa.validator.lang.ValidatorException;

public class ExceptionsTest {

  @Test(expected = UnknownDocumentTypeException.class)
  public void unknownDocumentType() throws UnknownDocumentTypeException {
    throw new UnknownDocumentTypeException("test");
  }

  @Test(expected = ValidatorException.class)
  public void validator1() throws ValidatorException {
    throw new ValidatorException("test");
  }

  @Test(expected = ValidatorException.class)
  public void validator2() throws ValidatorException {
    throw new ValidatorException("test", null);
  }
}
