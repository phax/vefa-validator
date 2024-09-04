package no.difi.vefa.validator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IRenderer;
import no.difi.vefa.validator.api.IRendererFactory;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.xsd.vefa.validator._1.StylesheetType;

/**
 * Pool of prepared renderers. Size if configured using properties.
 */
@Singleton
public class RendererCacheLoader extends CacheLoader <String, IRenderer>
{
  public static final int DEFAULT_SIZE = 250;
  private static final Logger log = LoggerFactory.getLogger (RendererCacheLoader.class);

  @Inject
  private ValidatorEngine validatorEngine;

  @Inject
  private List <IRendererFactory> factories;

  @Override
  public IRenderer load (final String key) throws Exception
  {
    try
    {
      final StylesheetType stylesheetType = validatorEngine.getStylesheet (key);

      for (final IRendererFactory factory : factories)
        for (final String extension : factory.getClass ().getAnnotation (Type.class).value ())
          if (stylesheetType.getPath ().toLowerCase ().endsWith (extension))
          {
            if (log.isDebugEnabled ())
              log.debug ("Renderer '{}'", key);
            return factory.prepare (stylesheetType,
                                    validatorEngine.getResource (stylesheetType.getPath ()),
                                    stylesheetType.getPath ().split ("#")[1]);
          }
    }
    catch (final Exception e)
    {
      throw new ValidatorException (String.format ("Unable to load presenter for '%s'.", key), e);
    }

    throw new ValidatorException (String.format ("No presenter found for '%s'", key));
  }
}
