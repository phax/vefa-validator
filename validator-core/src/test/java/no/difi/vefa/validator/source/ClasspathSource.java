package no.difi.vefa.validator.source;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.ISourceInstance;
import no.difi.vefa.validator.lang.ValidatorException;

public class ClasspathSource extends AbstractSource {

    private String location;

    public ClasspathSource(String location) {
        this.location = location;
    }

    @Override
    public ISourceInstance createInstance(IProperties properties) throws ValidatorException {
        return new ClasspathSourceInstance(properties, location);
    }
}
