package no.difi.vefa.validator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.ConvertedDocument;
import no.difi.vefa.validator.api.Document;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IValidation;
import no.difi.vefa.validator.api.IValidationSource;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.lang.UnknownDocumentTypeException;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.properties.CombinedProperties;
import no.difi.vefa.validator.util.CombinedFlagFilterer;
import no.difi.vefa.validator.util.DeclarationDetector;
import no.difi.vefa.validator.util.DeclarationIdentifier;
import no.difi.vefa.validator.util.DeclarationWrapper;
import no.difi.vefa.validator.util.StreamUtils;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FileType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.Report;
import no.difi.xsd.vefa.validator._1.TriggerType;

/**
 * Result of a validation.
 */
class ValidationInstance implements IValidation
{
  private static final Logger log = LoggerFactory.getLogger (ValidationInstance.class);

  private final ValidatorInstance validatorInstance;
  private final IProperties properties;
  private Configuration configuration;

  /**
   * Final report.
   */
  private final Report report;

  /**
   * Section used to gather problems during validation.
   */
  private final Section section = new Section (new CombinedFlagFilterer ());

  /**
   * Document subject to validation.
   */
  private Document document;

  private DeclarationWrapper declaration;

  private List <IValidation> children;

  public static ValidationInstance of (final ValidatorInstance validatorInstance,
                                       final IValidationSource validationSource)
  {
    return new ValidationInstance (validatorInstance, validationSource);
  }

  /**
   * Constructing new validator using validator instance and validation source
   * containing document to validate.
   *
   * @param validatorInstance
   *        Instance of validator.
   * @param validationSource
   *        Source to validate.
   */
  private ValidationInstance (final ValidatorInstance validatorInstance, final IValidationSource validationSource)
  {
    this.validatorInstance = validatorInstance;
    this.properties = new CombinedProperties (validationSource.getProperties (), validatorInstance.getProperties ());

    this.report = new Report ();
    this.report.setUuid (UUID.randomUUID ().toString ());
    this.report.setFlag (FlagType.OK);

    this.section.setTitle ("Validator");
    this.section.setFlag (FlagType.OK);

    try
    {
      loadDocument (validationSource.getInputStream ());
      loadConfiguration ();
      nestedValidation ();

      if (configuration != null)
        validate ();
    }
    catch (final IOException e)
    {
      log.warn (e.getMessage (), e);
    }
    catch (final UnknownDocumentTypeException e)
    {
      section.add ("SYSTEM-003", e.getMessage (), FlagType.UNKNOWN);
    }
    catch (final ValidatorException e)
    {
      section.add ("SYSTEM-001", e.getMessage (), FlagType.FATAL);
    }

    if (report.getTitle () == null)
      report.setTitle ("Unknown document type");

    if (section.getAssertion ().size () > 0)
    {
      for (final AssertionType assertionType : section.getAssertion ())
      {
        if (assertionType.getFlag ().compareTo (section.getFlag ()) > 0)
          section.setFlag (assertionType.getFlag ());
      }
      report.getSection ().add (0, section);

      if (section.getFlag ().compareTo (getReport ().getFlag ()) > 0)
        getReport ().setFlag (section.getFlag ());
    }
  }

  private void loadDocument (final InputStream inputStream) throws ValidatorException, IOException
  {
    ByteArrayInputStream byteArrayInputStream;
    if (inputStream instanceof ByteArrayInputStream)
    {
      // Use stream as-is.
      byteArrayInputStream = (ByteArrayInputStream) inputStream;
    }
    else
    {
      // Convert stream to ByteArrayOutputStream
      final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream ();
      ByteStreams.copy (inputStream, byteArrayOutputStream);
      byteArrayInputStream = new ByteArrayInputStream (byteArrayOutputStream.toByteArray ());
    }

    // To be able to reuse the stream later on.
    document = new Document (byteArrayInputStream);
    byteArrayInputStream.reset ();

    // Use declaration implementations to detect declaration to use.
    final DeclarationIdentifier declarationIdentifier = validatorInstance.detect (byteArrayInputStream);
    declaration = declarationIdentifier.getDeclaration ();

    if (declarationIdentifier.equals (DeclarationDetector.UNKNOWN))
      throw new UnknownDocumentTypeException ("Unable to detect type of content.");

    // Detect expectation
    IExpectation expectation = null;
    if (properties.getBoolean ("feature.expectation"))
    {
      // TODO this is buggy - reads only a limit amount of the file for test
      final byte [] bytes = StreamUtils.read50KAndReset (byteArrayInputStream);
      expectation = declaration.expectations (bytes);
      if (expectation != null)
        report.setDescription (expectation.getDescription ());
    }

    if (declaration.supportsConverter ())
    {
      final ByteArrayOutputStream convertedOutputStream = new ByteArrayOutputStream ();
      byteArrayInputStream.reset ();
      declaration.convert (byteArrayInputStream, convertedOutputStream);

      document = new ConvertedDocument (new ByteArrayInputStream (convertedOutputStream.toByteArray ()),
                                        byteArrayInputStream,
                                        declarationIdentifier.getFullIdentifier (),
                                        expectation);
    }
    else
    {
      document = new Document (byteArrayInputStream, declarationIdentifier.getFullIdentifier (), expectation);
    }
  }

  private void loadConfiguration () throws UnknownDocumentTypeException
  {
    // Default values for report
    report.setTitle ("Unknown document type");
    report.setFlag (FlagType.FATAL);

    // Get configuration using declaration
    this.configuration = validatorInstance.getConfiguration (document.getDeclarations ());

    if (!properties.getBoolean ("feature.suppress_notloaded"))
      for (final String notLoaded : configuration.getNotLoaded ())
        section.add ("SYSTEM-007", String.format ("Validation artifact '%s' not loaded.", notLoaded), FlagType.WARNING);

    // Update report using configuration for declaration
    report.setTitle (configuration.getTitle ());
    report.setConfiguration (configuration.getIdentifier ().getValue ());
    report.setBuild (configuration.getBuild ());
    report.setFlag (FlagType.OK);
  }

  private void validate ()
  {
    final long start = System.currentTimeMillis ();

    for (final FileType fileType : configuration.getFile ())
    {
      log.debug ("Validate: {}", fileType.getPath ());

      try
      {
        final Section section = validatorInstance.check (fileType, document, configuration);
        section.setConfiguration (fileType.getConfiguration ());
        section.setBuild (fileType.getBuild ());
        report.getSection ().add (section);

        if (section.getFlag ().compareTo (getReport ().getFlag ()) > 0)
          getReport ().setFlag (section.getFlag ());
      }
      catch (final ValidatorException e)
      {
        this.section.add ("SYSTEM-008", e.getMessage (), FlagType.ERROR);
      }

      if (getReport ().getFlag ().equals (FlagType.FATAL) || this.section.getFlag ().equals (FlagType.FATAL))
        break;
    }

    for (final TriggerType triggerType : configuration.getTrigger ())
    {
      try
      {
        final Section section = validatorInstance.trigger (triggerType, document, configuration);
        section.setConfiguration (triggerType.getConfiguration ());
        section.setBuild (triggerType.getBuild ());
        report.getSection ().add (section);

        if (section.getFlag ().compareTo (getReport ().getFlag ()) > 0)
          getReport ().setFlag (section.getFlag ());
      }
      catch (final ValidatorException e)
      {
        this.section.add ("SYSTEM-010", e.getMessage (), FlagType.ERROR);
      }
    }

    if (document.getExpectation () != null)
      document.getExpectation ().verify (section);

    report.setRuntime ((System.currentTimeMillis () - start) + "ms");
  }

  /**
   * Handling nested validation.
   */
  private void nestedValidation () throws ValidatorException
  {
    if (report.getFlag ().compareTo (FlagType.FATAL) < 0)
    {
      if (declaration.supportsChildren () && properties.getBoolean ("feature.nesting"))
      {
        final Iterable <CachedFile> iterable = declaration.children (document.getInputStream ());
        for (final CachedFile cachedFile : iterable)
        {
          addChildValidation (ValidationInstance.of (validatorInstance,
                                                     new ValidationSourceImpl (cachedFile.getContentStream ())),
                              cachedFile.getFilename ());
        }
      }
    }
  }

  private void addChildValidation (final IValidation validation, final String filename)
  {
    final Report childReport = validation.getReport ();
    childReport.setFilename (filename);
    report.getReport ().add (childReport);

    if (children == null)
      children = new ArrayList <> ();
    children.add (validation);
  }

  /**
   * Render document to a stream.
   *
   * @param outputStream
   *        Stream to use.
   */
  @Override
  public void render (final OutputStream outputStream) throws ValidatorException
  {
    render (outputStream, null);
  }

  /**
   * Render document to a stream, allows for extra configuration.
   *
   * @param outputStream
   *        Stream to use.
   * @param properties
   *        Extra configuration to use for this rendering.
   */
  @Override
  public void render (final OutputStream outputStream, final IProperties properties) throws ValidatorException
  {
    if (getReport ().getFlag ().equals (FlagType.FATAL))
      throw new ValidatorException (String.format ("Status '%s' is not supported for rendering.",
                                                   getReport ().getFlag ()));
    if (configuration == null)
      throw new ValidatorException ("Configuration was not detected, configuration is need for rendering.");
    if (configuration.getStylesheet () == null)
      throw new ValidatorException ("No stylesheet is defined for document type.");

    validatorInstance.render (configuration.getStylesheet (), document, properties, outputStream);
  }

  /**
   * Returns true if validated document is renderable based upon same criteria
   * as may be provide exception when using #render(...).
   *
   * @return 'true' if validated document is renderable.
   */
  @Override
  public boolean isRenderable ()
  {
    return configuration != null &&
           configuration.getStylesheet () != null &&
           !getReport ().getFlag ().equals (FlagType.FATAL);
  }

  /**
   * Document used for validation as represented in the validator.
   *
   * @return Document object.
   */
  @Override
  public Document getDocument ()
  {
    return document;
  }

  /**
   * Report is the result of validation.
   *
   * @return Report
   */
  @Override
  public Report getReport ()
  {
    return report;
  }

  /**
   * Nested validations of validation.
   *
   * @return List of validations or null if none available.
   */
  @Override
  public List <IValidation> getChildren ()
  {
    return children;
  }
}
