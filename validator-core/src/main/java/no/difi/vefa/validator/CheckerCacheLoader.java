package no.difi.vefa.validator;

import java.util.List;
import java.util.Locale;

import com.google.common.cache.CacheLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.lang.ValidatorException;

/**
 * @author erlend
 */
@Singleton
public class CheckerCacheLoader extends CacheLoader <String, IChecker>
{

  public static final int DEFAULT_SIZE = 250;

  @Inject
  private List <ICheckerFactory> factories;

  @Inject
  private ValidatorEngine validatorEngine;

  @Override
  public IChecker load (final String key) throws Exception
  {
    try
    {
      for (final ICheckerFactory factory : factories)
        for (final String extension : factory.getClass ().getAnnotation (Type.class).value ())
          if (key.toLowerCase (Locale.ROOT).endsWith (extension))
            return factory.prepare (validatorEngine.getResource (key), key.split ("#")[1]);
    }
    catch (final Exception e)
    {
      throw new ValidatorException ("Unable to load checker for '" + key + "'.", e);
    }

    throw new ValidatorException ("No checker found for '" + key + "'");
  }
}
