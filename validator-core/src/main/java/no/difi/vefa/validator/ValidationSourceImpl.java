package no.difi.vefa.validator;

import java.io.InputStream;

import no.difi.vefa.validator.api.Properties;
import no.difi.vefa.validator.api.ValidationSource;

class ValidationSourceImpl implements ValidationSource {

    private InputStream inputStream;

    private Properties properties;

    public ValidationSourceImpl(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public ValidationSourceImpl(InputStream inputStream, Properties properties) {
        this(inputStream);
        this.properties = properties;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
