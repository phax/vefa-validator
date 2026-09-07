package no.difi.vefa.validator.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IDeclaration;
import no.difi.vefa.validator.lang.VefaValidatorException;

public class DeclarationDetector
{
  public static final DeclarationIdentifier UNKNOWN = new DeclarationIdentifier (null,
                                                                                 null,
                                                                                 Collections.singletonList ("unknown"));
  private static final Logger LOGGER = LoggerFactory.getLogger (DeclarationDetector.class);

  private final List <DeclarationWrapper> rootDeclarationWrappers = new ArrayList <> ();

  public DeclarationDetector (final List <IDeclaration> declarations)
  {
    final Map <String, DeclarationWrapper> wrapperMap = new HashMap <> ();

    for (final IDeclaration declaration : declarations)
      if (declaration.getClass ().isAnnotationPresent (Type.class))
        for (final String type : declaration.getClass ().getAnnotation (Type.class).value ())
          wrapperMap.put (type, DeclarationWrapper.of (type, declaration));

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
    return _detect (rootDeclarationWrappers, null, contentStream, UNKNOWN);
  }

  private DeclarationIdentifier _detect (final List <DeclarationWrapper> wrappers,
                                         final byte @Nullable [] content,
                                         final InputStream contentStream,
                                         final DeclarationIdentifier parent) throws IOException
  {
    final byte [] realContent = content != null ? content : StreamUtils.read50KAndReset (contentStream);

    for (final DeclarationWrapper wrapper : wrappers)
    {
      try
      {
        if (wrapper.verify (realContent, parent == null ? null : parent.getIdentifier ()))
        {
          contentStream.mark (0);
          final List <String> identifier = wrapper.detect (contentStream,
                                                           parent == null ? null : parent.getIdentifier ());

          if (identifier == null)
          {
            break;
          }

          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Found: " + wrapper.getType () + " - " + identifier);

          return _detect (wrapper.getChildren (),
                          realContent,
                          contentStream,
                          new DeclarationIdentifier (parent, wrapper, identifier));
        }
      }
      catch (final VefaValidatorException e)
      {
        LOGGER.warn (e.getMessage (), e);
      }
      finally
      {
        try
        {
          contentStream.reset ();
        }
        catch (final IOException e)
        {
          LOGGER.warn ("Couldn't reset stream!", e);
        }
      }
    }

    return parent;
  }
}
