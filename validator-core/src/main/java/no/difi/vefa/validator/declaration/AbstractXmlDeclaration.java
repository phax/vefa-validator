package no.difi.vefa.validator.declaration;

import javax.xml.stream.XMLInputFactory;

import no.difi.vefa.validator.api.Declaration;
import no.difi.vefa.validator.api.Expectation;
import no.difi.vefa.validator.expectation.XmlExpectation;
import no.difi.vefa.validator.lang.ValidatorException;

abstract class AbstractXmlDeclaration implements Declaration {

    protected static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newFactory();

    @Override
    public Expectation expectations(byte[] content) throws ValidatorException {
        return new XmlExpectation(content);
    }
}
