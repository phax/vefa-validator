package no.difi.vefa.validator.declaration;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.IDeclarationWithChildren;
import no.difi.vefa.validator.lang.ValidatorException;

@Type ("xml.sbdh")
public class SbdhDeclaration extends AbstractXmlDeclaration implements IDeclarationWithChildren
{

  private static final String NAMESPACE = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader";

  @Inject
  @Named ("sbdh-extractor")
  private Provider <XsltExecutable> extractor;

  @Inject
  private Processor processor;

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    return parent.get (0).startsWith (NAMESPACE);
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent)
  {
    return Arrays.asList (parent.get (0), "SBDH:1.0");
  }

  @Override
  public Iterable <CachedFile> children (final InputStream inputStream) throws ValidatorException
  {
    try
    {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream ();

      final XsltTransformer xsltTransformer = extractor.get ().load ();
      xsltTransformer.setSource (new StreamSource (inputStream));
      xsltTransformer.setDestination (processor.newSerializer (baos));
      xsltTransformer.transform ();
      xsltTransformer.close ();

      if (baos.size () <= 38)
        return Collections.emptyList ();

      return Collections.singletonList (CachedFile.of (baos.toByteArray ()));
    }
    catch (final SaxonApiException e)
    {
      throw new ValidatorException ("Unable to extract SBDH content.", e);
    }
  }
}
