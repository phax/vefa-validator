package no.difi.vefa.validator.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import no.difi.vefa.validator.api.IArtifactHolder;

public class HolderLSResolveResource implements LSResourceResolver {

    private IArtifactHolder artifactHolder;

    private Path rootPath;

    public HolderLSResolveResource(IArtifactHolder artifactHolder, String rootPath) {
        this.artifactHolder = artifactHolder;
        this.rootPath = Paths.get(rootPath).getParent();
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        Path target;
        if (baseURI == null)
            target = rootPath.resolve(systemId);
        else
            target = Paths.get(baseURI.substring(7)).getParent().resolve(systemId);

        String newPath = ("/" + target.toString().replaceAll("\\\\", "/")).replaceAll("/([^/]+?)/\\.\\.", "").substring(1);

        return new HolderLSInput(artifactHolder.get(newPath), newPath);
    }
}
