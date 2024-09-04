package no.difi.vefa.validator.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import no.difi.vefa.validator.api.IArtifactHolder;

public class HolderURIResolver implements URIResolver {

    private IArtifactHolder artifactHolder;

    private Path rootPath;

    public HolderURIResolver(IArtifactHolder artifactHolder, String rootPath) {
        this.artifactHolder = artifactHolder;
        this.rootPath = Paths.get(rootPath).getParent();
    }

    @Override
    public Source resolve(String href, String base) {
        Path target = (base == "" ? rootPath : Paths.get(base.substring(7)).getParent()).resolve(href);

        String newPath = ("/" + target.toString().replaceAll("\\\\", "/")).replaceAll("/(.+?)/\\.\\.", "").substring(1);

        StreamSource streamSource = new StreamSource(artifactHolder.getInputStream(newPath));
        streamSource.setPublicId(String.format("holder:%s", newPath));
        streamSource.setSystemId(String.format("holder:%s", newPath));

        return streamSource;
    }
}
