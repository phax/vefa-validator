package no.difi.vefa.validator;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.ISource;
import no.difi.vefa.validator.module.PropertiesModule;
import no.difi.vefa.validator.module.SourceModule;
import no.difi.vefa.validator.module.ValidatorModule;

/**
 * Builder supporting creation of validator.
 */
public class ValidatorBuilder {

    private ISource source;

    private IProperties properties;

    /**
     * Initiate creation of a new validator. Loads default plugins.
     *
     * @return Builder object
     */
    public static ValidatorBuilder newValidator() {
        return new ValidatorBuilder();
    }

    /**
     * Internal constructor, no action needed.
     */
    private ValidatorBuilder() {
        // No action
    }

    /**
     * Defines configuration to use for validator.
     *
     * @param properties Configuration
     * @return Builder object
     */
    public ValidatorBuilder setProperties(IProperties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Define source to use if other source then production repository to be used.
     *
     * @param source Source giving access to validation rules.
     * @return Builder object
     */
    public ValidatorBuilder setSource(ISource source) {
        this.source = source;
        return this;
    }

    /**
     * Initiate validator and return validator ready for use.
     *
     * @return Validator ready for use.
     */
    public Validator build() {
        List<Module> modules = new ArrayList<>();
        modules.add(new PropertiesModule(properties));
        modules.add(new SourceModule(source));

        return Guice.createInjector(Modules.override(new ValidatorModule()).with(modules)).getInstance(Validator.class);
    }
}
