package no.difi.vefa.validator.api;

import java.util.Map;

/**
 * An instance representing a source.
 * <p/>
 * Implementations in need of close() method should implement java.io.Closeable.
 */
public interface ISourceInstance {

    Map<String, IArtifactHolder> getContent();

    IArtifactHolder getContent(String path);

}
