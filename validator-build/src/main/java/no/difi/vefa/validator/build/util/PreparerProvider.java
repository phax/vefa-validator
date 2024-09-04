package no.difi.vefa.validator.build.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IPreparer;

/**
 * @author erlend
 */
@Singleton
public class PreparerProvider {

    public static final String DEFAULT = "#DEFAULT";

    private Map<String, IPreparer> preparerMap = new HashMap<>();

    @Inject
    public PreparerProvider(List<IPreparer> preparers) {
        for (IPreparer preparer : preparers)
            for (String extension : preparer.getClass().getAnnotation(Type.class).value())
                preparerMap.put(extension, preparer);
    }

    public IPreparer get(String extension) {
        return preparerMap.containsKey(extension) ?
                preparerMap.get(extension) : preparerMap.get(DEFAULT);
    }

    public void prepare(final Path source, final Path target, final IPreparer.EType type) throws IOException {
        if (IPreparer.EType.INCLUDE.equals(type) && Files.isDirectory(source)) {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    String filename = path.toString().substring(source.toString().length() + 1);
                    prepare(source.resolve(filename), target.resolve(filename), type);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            if (target.getParent() != null)
                Files.createDirectories(target.getParent());

            String extension = source.toString().substring(source.toString().lastIndexOf("."));
            get(extension).prepare(source, target, type);
        }
    }
}
