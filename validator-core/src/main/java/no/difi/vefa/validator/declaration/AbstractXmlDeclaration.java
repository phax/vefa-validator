package no.difi.vefa.validator.declaration;

import no.difi.vefa.validator.api.IDeclaration;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.expectation.XmlExpectation;
import no.difi.vefa.validator.lang.VefaValidatorException;

abstract class AbstractXmlDeclaration implements IDeclaration
{
  @Override
  public IExpectation expectations (final byte [] content) throws VefaValidatorException
  {
    return new XmlExpectation (content);
  }
}
