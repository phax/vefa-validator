package no.difi.vefa.validator;

import org.jspecify.annotations.NonNull;

import com.helger.cache.IMutableCache;

import no.difi.vefa.validator.api.IArtifactsSourceInstance;
import no.difi.vefa.validator.api.IArtifactsSourceProvider;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.properties.CombinedProperties;
import no.difi.vefa.validator.source.RepositorySource;
import no.difi.vefa.validator.trigger.TriggerFactory;
import no.difi.vefa.validator.util.DeclarationDetector;

/**
 * Builder supporting creation of validator.
 */
public class ValidatorBuilder
{
  private IArtifactsSourceProvider m_aSource;
  private IProperties m_aProperties;

  /**
   * Initiate creation of a new validator. Loads default plugins.
   *
   * @return Builder object
   */
  @NonNull
  public static ValidatorBuilder newValidator ()
  {
    return new ValidatorBuilder ();
  }

  /**
   * Internal constructor, no action needed.
   */
  private ValidatorBuilder ()
  {}

  /**
   * Defines configuration to use for validator.
   *
   * @param properties
   *        Configuration
   * @return Builder object
   */
  public ValidatorBuilder setProperties (final IProperties properties)
  {
    m_aProperties = properties;
    return this;
  }

  /**
   * Define source to use if other source then production repository to be used.
   *
   * @param source
   *        Source giving access to validation rules.
   * @return Builder object
   */
  public ValidatorBuilder setSource (final IArtifactsSourceProvider source)
  {
    m_aSource = source;
    return this;
  }

  /**
   * Initiate validator and return validator ready for use.
   *
   * @return Validator ready for use.
   */
  public Validator build ()
  {
    try
    {
      final IProperties properties = new CombinedProperties (m_aProperties, ValidatorDefaults.PROPERTIES);

      final IArtifactsSourceProvider sourceProvider = m_aSource != null ? m_aSource : RepositorySource.forProduction ();
      final IArtifactsSourceInstance sourceInstance = sourceProvider.createInstance (properties);

      final ValidatorEngine validatorEngine = new ValidatorEngine (sourceInstance);
      final CheckerCacheLoader checkerCacheLoader = new CheckerCacheLoader (ValidatorFactory.createCheckerFactories (),
                                                                            validatorEngine);
      final IMutableCache <String, IChecker> checkerCache = ValidatorFactory.createCheckerCache (properties,
                                                                                                 checkerCacheLoader);
      final DeclarationDetector declarationDetector = ValidatorFactory.createDeclarationDetector ();
      final TriggerFactory triggerFactory = new TriggerFactory (ValidatorFactory.createTriggers ());

      final ValidatorInstance validatorInstance = new ValidatorInstance (validatorEngine,
                                                                         properties,
                                                                         declarationDetector,
                                                                         checkerCache,
                                                                         triggerFactory);
      return new Validator (validatorInstance);
    }
    catch (final VefaValidatorException e)
    {
      throw new IllegalStateException ("Unable to build Validator.", e);
    }
  }
}
