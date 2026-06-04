package no.difi.vefa.validator;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.string.StringImplode;
import com.helger.cache.IMutableCache;

import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.api.VefaDocument;
import no.difi.vefa.validator.lang.UnknownDocumentTypeException;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.trigger.TriggerFactory;
import no.difi.vefa.validator.util.CombinedFlagFilterer;
import no.difi.vefa.validator.util.DeclarationDetector;
import no.difi.vefa.validator.util.DeclarationIdentifier;
import no.difi.xsd.vefa.validator._1.ConfigurationType;
import no.difi.xsd.vefa.validator._1.FileType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.PackageType;
import no.difi.xsd.vefa.validator._1.TriggerType;

/**
 * Contains CheckerPools and Configuration, and is entry point for validation.
 */
class ValidatorInstance implements Closeable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ValidatorInstance.class);

  /**
   * Instance of ValidatorEngine containing all raw content needed for validation.
   */
  private final ValidatorEngine validatorEngine;

  /**
   * Current validator configuration.
   */
  private final IProperties properties;

  /**
   * Declarations to use.
   */
  private final DeclarationDetector declarationDetector;

  /**
   * Cache of checkers.
   */
  private final IMutableCache <String, IChecker> checkerCache;

  /**
   * Trigger factory.
   */
  private final TriggerFactory triggerFactory;

  /**
   * Normalized configurations indexed by document declarations.
   */
  private final Map <String, Configuration> configurationMap = new HashMap <> ();

  public ValidatorInstance (final ValidatorEngine validatorEngine,
                            final IProperties properties,
                            final DeclarationDetector declarationDetector,
                            final IMutableCache <String, IChecker> checkerCache,
                            final TriggerFactory triggerFactory)
  {
    this.validatorEngine = validatorEngine;
    this.properties = properties;
    this.declarationDetector = declarationDetector;
    this.checkerCache = checkerCache;
    this.triggerFactory = triggerFactory;
  }

  /**
   * List of packages supported by validator.
   *
   * @return List of packages.
   */
  protected List <PackageType> getPackages ()
  {
    return validatorEngine.getPackages ();
  }

  /**
   * Fetch properties for internal use.
   *
   * @return Current properties.
   */
  protected IProperties getProperties ()
  {
    return properties;
  }

  public String getEngineName ()
  {
    return "Engine [" +
           StringImplode.imploder ()
                        .source (validatorEngine.getPackages (), PackageType::getValue)
                        .separator (", ")
                        .build () +
           "]";
  }

  /**
   * Return validation configuration.
   *
   * @param declarations
   *        Fetch configuration using declarations.
   */
  protected Configuration getConfiguration (final List <String> declarations) throws UnknownDocumentTypeException
  {
    for (final String declaration : declarations)
    {
      // Check cache of configurations is ready to use.
      if (configurationMap.containsKey (declaration))
        return configurationMap.get (declaration);

      final ConfigurationType configurationType = validatorEngine.getConfigurationByDeclaration (declaration);

      if (configurationType != null)
      {
        // Create a new instance of configuration using the raw configuration.
        final Configuration configuration = new Configuration (configurationType);

        // Normalize configuration using inheritance declarations.
        configuration.normalize (validatorEngine);
        // Add configuration to map containing configurations ready to use.
        configurationMap.put (declaration, configuration);

        // Return configuration.
        return configuration;
      }
    }

    throw new UnknownDocumentTypeException ("Configuration for '" + declarations.get (0) + "' not found.");
  }

  protected DeclarationIdentifier detect (final InputStream contentStream) throws IOException
  {
    return declarationDetector.detect (contentStream);
  }

  /**
   * Validate document using a file definition.
   *
   * @param fileType
   *        File definition from configuration.
   * @param document
   *        Document to validate.
   * @param configuration
   *        Complete configuration
   * @return Result of validation.
   */
  protected Section check (final FileType fileType, final VefaDocument document, final Configuration configuration)
                                                                                                                    throws VefaValidatorException
  {
    final IChecker checker;
    try
    {
      checker = checkerCache.getFromCache (fileType.getPath ());
    }
    catch (final Exception e)
    {
      LOGGER.error (e.getMessage (), e);
      throw new VefaValidatorException ("Unable to get checker object from pool for '" +
                                        configuration.getIdentifier () +
                                        "'.",
                                        e);
    }

    final Section section = new Section (new CombinedFlagFilterer (configuration, document.getExpectation ()));
    section.setFlag (FlagType.OK);

    if (properties.getBoolean ("feature.infourl"))
      section.setInfoUrl (fileType.getInfoUrl ());

    checker.check (document, section);

    section.setInfoUrl (null);
    return section;
  }

  /**
   * Validate document using a trigger definition.
   *
   * @param triggerType
   *        Trigger definition from configuration.
   * @param document
   *        Document to validate.
   * @param configuration
   *        Complete configuration
   * @return Result of validation.
   */
  protected Section trigger (final TriggerType triggerType,
                             final VefaDocument document,
                             final Configuration configuration) throws VefaValidatorException
  {
    final Section section = new Section (new CombinedFlagFilterer (configuration, document.getExpectation ()));
    section.setFlag (FlagType.OK);
    triggerFactory.get (triggerType.getIdentifier ()).check (document, section);
    return section;
  }

  @Override
  public void close () throws IOException
  {
    checkerCache.clearCache ();

    // This is last statement, allow to propagate.
    validatorEngine.close ();
  }
}
