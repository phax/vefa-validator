package no.difi.vefa.validator.checker;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IArtifactHolder;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.HolderLSResolveResource;

/**
 * @author erlend
 */
@Type(".xsd")
public class XsdCheckerFactory implements ICheckerFactory {

    @Override
    public IChecker prepare(IArtifactHolder artifactHolder, String path) throws ValidatorException {
        try (InputStream inputStream = artifactHolder.getInputStream(path)) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setResourceResolver(new HolderLSResolveResource(artifactHolder, path));
            return new XsdChecker(schemaFactory.newSchema(new StreamSource(inputStream)));
        } catch (Exception e) {
            throw new ValidatorException(e.getMessage(), e);
        }
    }
}
