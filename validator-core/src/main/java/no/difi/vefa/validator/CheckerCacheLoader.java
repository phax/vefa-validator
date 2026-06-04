package no.difi.vefa.validator;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.lang.VefaValidatorException;

/**
 * @author erlend
 */
public class CheckerCacheLoader implements Function <String, IChecker>
{
  public static final int DEFAULT_SIZE = 250;

  private final List <ICheckerFactory> factories;

  private final ValidatorEngine validatorEngine;

  public CheckerCacheLoader (final List <ICheckerFactory> factories, final ValidatorEngine validatorEngine)
  {
    this.factories = factories;
    this.validatorEngine = validatorEngine;
  }

  @Override
  public IChecker apply (final String key)
  {
    try
    {
      for (final ICheckerFactory aFactory : factories)
        for (final String extension : aFactory.getClass ().getAnnotation (Type.class).value ())
          if (key.toLowerCase (Locale.ROOT).endsWith (extension))
            return aFactory.prepare (validatorEngine.getResource (key), key.split ("#")[1]);
    }
    catch (final Exception e)
    {
      throw new IllegalStateException (new VefaValidatorException ("Unable to load checker for '" + key + "'.", e));
    }

    throw new IllegalStateException (new VefaValidatorException ("No checker found for '" + key + "'"));
  }
}
