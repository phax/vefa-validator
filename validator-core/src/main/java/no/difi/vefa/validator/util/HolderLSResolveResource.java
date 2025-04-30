package no.difi.vefa.validator.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import no.difi.vefa.validator.api.IArtifactHolder;

public final class HolderLSResolveResource implements LSResourceResolver
{
  private final IArtifactHolder m_aArtifactHolder;
  private final Path m_aRootPath;

  public HolderLSResolveResource (final IArtifactHolder artifactHolder, final String rootPath)
  {
    this.m_aArtifactHolder = artifactHolder;
    this.m_aRootPath = Paths.get (rootPath).getParent ();
  }

  @Override
  public LSInput resolveResource (final String type,
                                  final String namespaceURI,
                                  final String publicId,
                                  final String systemId,
                                  final String baseURI)
  {
    final Path target;
    if (baseURI == null)
      target = m_aRootPath.resolve (systemId);
    else
      target = Paths.get (baseURI.substring (7)).getParent ().resolve (systemId);

    final String newPath = ("/" + target.toString ().replace ('\\', '/')).replaceAll ("/([^/]+?)/\\.\\.", "")
                                                                         .substring (1);
    return new HolderLSInput (m_aArtifactHolder.get (newPath), newPath);
  }
}
