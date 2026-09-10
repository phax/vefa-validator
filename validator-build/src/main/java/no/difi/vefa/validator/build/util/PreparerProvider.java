package no.difi.vefa.validator.build.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IPreparer;
import no.difi.vefa.validator.build.preparer.DefaultPreparer;
import no.difi.vefa.validator.build.preparer.SchematronPreparer;

/**
 * @author erlend
 */
public class PreparerProvider
{
  public static final String DEFAULT = "#DEFAULT";

  private final Map <String, IPreparer> m_aPreparerMap = new HashMap <> ();

  public PreparerProvider ()
  {
    for (final IPreparer preparer : new IPreparer [] { new DefaultPreparer (), new SchematronPreparer () })
      for (final String extension : preparer.getClass ().getAnnotation (Type.class).value ())
        m_aPreparerMap.put (extension, preparer);
  }

  private IPreparer _get (final String extension)
  {
    return m_aPreparerMap.containsKey (extension) ? m_aPreparerMap.get (extension) : m_aPreparerMap.get (DEFAULT);
  }

  public void prepare (final Path source, final Path target, final IPreparer.EPreparerType type) throws IOException
  {
    if (IPreparer.EPreparerType.INCLUDE.equals (type) && Files.isDirectory (source))
    {
      Files.walkFileTree (source, new SimpleFileVisitor <Path> ()
      {
        @Override
        public FileVisitResult visitFile (final Path path,
                                          final BasicFileAttributes basicFileAttributes) throws IOException
        {
          final String filename = path.toString ().substring (source.toString ().length () + 1);
          prepare (source.resolve (filename), target.resolve (filename), type);
          return FileVisitResult.CONTINUE;
        }
      });
    }
    else
    {
      if (target.getParent () != null)
        Files.createDirectories (target.getParent ());

      final String extension = source.toString ().substring (source.toString ().lastIndexOf ("."));
      _get (extension).prepare (source, target, type);
    }
  }
}
