package no.difi.vefa.validator.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.ISource;
import no.difi.vefa.validator.api.ISourceInstance;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.source.RepositorySource;

/**
 * @author erlend
 */
public class SourceModule extends AbstractModule {

    private final ISource source;

    public SourceModule() {
        this(null);
    }

    public SourceModule(ISource source) {
        this.source = source;
    }

    @Provides
    @Singleton
    public ISourceInstance getSource(IProperties properties) throws ValidatorException {
        // Make sure to default to repository source if no source is set.
        return (source != null ? source : RepositorySource.forProduction())
                .createInstance(properties);
    }
}
