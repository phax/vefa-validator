package no.difi.vefa.validator;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import no.difi.vefa.validator.api.Document;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IRenderer;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.lang.UnknownDocumentTypeException;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.properties.CombinedProperties;
import no.difi.vefa.validator.trigger.TriggerFactory;
import no.difi.vefa.validator.util.CombinedFlagFilterer;
import no.difi.vefa.validator.util.DeclarationDetector;
import no.difi.vefa.validator.util.DeclarationIdentifier;
import no.difi.xsd.vefa.validator._1.ConfigurationType;
import no.difi.xsd.vefa.validator._1.FileType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.PackageType;
import no.difi.xsd.vefa.validator._1.StylesheetType;
import no.difi.xsd.vefa.validator._1.TriggerType;

/**
 * Contains CheckerPools and Configuration, and is entry point for validation.
 */
@Singleton
class ValidatorInstance implements Closeable
{
  private static final Logger log = LoggerFactory.getLogger (ValidatorInstance.class);

  /**
   * Instance of ValidatorEngine containing all raw content needed for
   * validation.
   */
  @Inject
  private ValidatorEngine validatorEngine;

  /**
   * Current validator configuration.
   */
  @Inject
  private IProperties properties;

  /**
   * Declarations to use.
   */
  @Inject
  private DeclarationDetector declarationDetector;

  /**
   * Cache of checkers.
   */
  @Inject
  private LoadingCache <String, IChecker> checkerCache;

  /**
   * Pool of presenters.
   */
  @Deprecated
  @Inject
  private LoadingCache <String, IRenderer> rendererCache;

  /**
   * Trigger factory.
   */
  @Inject
  private TriggerFactory triggerFactory;

  /**
   * Normalized configurations indexed by document declarations.
   */
  private final Map <String, Configuration> configurationMap = new HashMap <> ();

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

    throw new UnknownDocumentTypeException (String.format ("Configuration for '%s' not found.", declarations.get (0)));
  }

  protected DeclarationIdentifier detect (final InputStream contentStream) throws IOException
  {
    return declarationDetector.detect (contentStream);
  }

  /**
   * Render document using stylesheet
   *
   * @param stylesheet
   *        Stylesheet identifier from configuration.
   * @param document
   *        Document used for styling.
   * @param outputStream
   *        Stream for dumping of result.
   */
  @Deprecated
  protected void render (final StylesheetType stylesheet,
                         final Document document,
                         final IProperties properties,
                         final OutputStream outputStream) throws ValidatorException
  {
    IRenderer renderer;
    try
    {
      renderer = rendererCache.get (stylesheet.getIdentifier ());
    }
    catch (final Exception e)
    {
      log.warn (e.getMessage (), e);
      throw new ValidatorException (String.format ("Unable to borrow presenter object from pool for '%s'.",
                                                   stylesheet.getIdentifier ()),
                                    e);
    }

    renderer.render (document, new CombinedProperties (properties, this.properties), outputStream);
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
  protected Section check (final FileType fileType,
                           final Document document,
                           final Configuration configuration) throws ValidatorException
  {
    IChecker checker;
    try
    {
      checker = checkerCache.get (fileType.getPath ());
    }
    catch (final Exception e)
    {
      log.warn (e.getMessage (), e);
      throw new ValidatorException (String.format ("Unable to get checker object from pool for '%s'.",
                                                   configuration.getIdentifier ()),
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
                             final Document document,
                             final Configuration configuration) throws ValidatorException
  {
    final Section section = new Section (new CombinedFlagFilterer (configuration, document.getExpectation ()));
    section.setFlag (FlagType.OK);
    triggerFactory.get (triggerType.getIdentifier ()).check (document, section);
    return section;
  }

  @Override
  public void close () throws IOException
  {
    checkerCache.invalidateAll ();
    checkerCache.cleanUp ();

    rendererCache.invalidateAll ();
    rendererCache.cleanUp ();

    // This is last statement, allow to propagate.
    validatorEngine.close ();
  }
}
