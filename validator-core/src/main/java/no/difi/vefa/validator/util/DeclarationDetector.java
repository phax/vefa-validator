package no.difi.vefa.validator.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IDeclaration;
import no.difi.vefa.validator.lang.ValidatorException;

@Singleton
public class DeclarationDetector
{
  public static final DeclarationIdentifier UNKNOWN = new DeclarationIdentifier (null,
                                                                                 null,
                                                                                 Collections.singletonList ("unknown"));
  private static final Logger log = LoggerFactory.getLogger (DeclarationDetector.class);
  private final List <DeclarationWrapper> rootDeclarationWrappers = new ArrayList <> ();

  @Inject
  public DeclarationDetector (final List <IDeclaration> declarations)
  {
    final Map <String, DeclarationWrapper> wrapperMap = new HashMap <> ();

    for (final IDeclaration declaration : declarations)
    {
      if (declaration.getClass ().isAnnotationPresent (Type.class))
      {
        for (final String type : declaration.getClass ().getAnnotation (Type.class).value ())
        {
          wrapperMap.put (type, DeclarationWrapper.of (type, declaration));
        }
      }
    }

    for (final String key : wrapperMap.keySet ())
    {
      if (key.contains ("."))
      {
        final String parent = key.substring (0, key.lastIndexOf ("."));
        wrapperMap.get (parent).getChildren ().add (wrapperMap.get (key));
      }
      else
      {
        rootDeclarationWrappers.add (wrapperMap.get (key));
      }
    }
  }

  public DeclarationIdentifier detect (final InputStream contentStream) throws IOException
  {
    return detect (rootDeclarationWrappers, null, contentStream, UNKNOWN);
  }

  private DeclarationIdentifier detect (final List <DeclarationWrapper> wrappers,
                                        byte [] content,
                                        final InputStream contentStream,
                                        final DeclarationIdentifier parent) throws IOException
  {

    if (content == null)
    {
      content = StreamUtils.read50KAndReset (contentStream);
    }

    for (final DeclarationWrapper wrapper : wrappers)
    {
      try
      {
        if (wrapper.verify (content, parent == null ? null : parent.getIdentifier ()))
        {
          contentStream.mark (0);
          final List <String> identifier = wrapper.detect (contentStream,
                                                           parent == null ? null : parent.getIdentifier ());

          if (identifier == null)
            break;
          log.debug ("Found: {} - {}", wrapper.getType (), identifier);

          return detect (wrapper.getChildren (),
                         content,
                         contentStream,
                         new DeclarationIdentifier (parent, wrapper, identifier));
        }
      }
      catch (final ValidatorException e)
      {
        log.warn (e.getMessage (), e);
      }
      finally
      {
        try
        {
          contentStream.reset ();
        }
        catch (final IOException e)
        {
          log.warn ("Couldn't reset stream!", e);
        }
      }
    }

    return parent;
  }
}
