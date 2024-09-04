package no.difi.vefa.validator.declaration;

import javax.xml.stream.XMLInputFactory;

import no.difi.vefa.validator.api.IDeclaration;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.expectation.XmlExpectation;
import no.difi.vefa.validator.lang.ValidatorException;

abstract class AbstractXmlDeclaration implements IDeclaration {

    protected static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newFactory();

    @Override
    public IExpectation expectations(byte[] content) throws ValidatorException {
        return new XmlExpectation(content);
    }
}
