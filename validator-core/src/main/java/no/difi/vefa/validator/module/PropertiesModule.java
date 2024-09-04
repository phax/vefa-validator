package no.difi.vefa.validator.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import no.difi.vefa.validator.ValidatorDefaults;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.properties.CombinedProperties;

/**
 * @author erlend
 */
public class PropertiesModule extends AbstractModule {

    private final IProperties properties;

    public PropertiesModule() {
        this(null);
    }

    public PropertiesModule(IProperties properties) {
        this.properties = properties;
    }

    @Provides
    @Singleton
    public IProperties getProperties() {
        // Create config combined with default values.
        return new CombinedProperties(properties, ValidatorDefaults.PROPERTIES);
    }
}
