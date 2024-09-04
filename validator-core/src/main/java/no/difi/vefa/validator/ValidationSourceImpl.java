package no.difi.vefa.validator;

import java.io.InputStream;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IValidationSource;

class ValidationSourceImpl implements IValidationSource {

    private InputStream inputStream;

    private IProperties properties;

    public ValidationSourceImpl(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public ValidationSourceImpl(InputStream inputStream, IProperties properties) {
        this(inputStream);
        this.properties = properties;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public IProperties getProperties() {
        return properties;
    }
}
