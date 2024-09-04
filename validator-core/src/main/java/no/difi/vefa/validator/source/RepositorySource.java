package no.difi.vefa.validator.source;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.ISourceInstance;
import no.difi.vefa.validator.lang.ValidatorException;

/**
 * Defines a repository as source for validation artifacts.
 */
public class RepositorySource extends AbstractSource {

    private List<URI> rootUri;

    public static RepositorySource forTest() {
        return create("https://anskaffelser.dev/repo/validator/draft/");
    }

    public static RepositorySource forProduction() {
        return create("https://anskaffelser.dev/repo/validator/current/");
    }

    public static RepositorySource of(String... uris) {
        return new RepositorySource(uris);
    }

    static RepositorySource create(String uri) {
        return new RepositorySource(uri);
    }

    /**
     * Helper method to allow using string when initiating the new source.
     *
     * @param uris Uri used to fetch validation artifacts.
     */
    public RepositorySource(String... uris) {
        rootUri = new ArrayList<>();
        for (String uri : uris)
            rootUri.add(URI.create(uri));
    }

    /**
     * Initiate the new source.
     *
     * @param uri Uri used to fetch validation artifacts.
     */
    public RepositorySource(URI... uri) {
        this.rootUri = Arrays.asList(uri);
    }

    public RepositorySource(List<URI> uris) {
        this.rootUri = uris;
    }

    @Override
    public ISourceInstance createInstance(IProperties properties) throws ValidatorException {
        return new RepositorySourceInstance(properties, rootUri);
    }
}
