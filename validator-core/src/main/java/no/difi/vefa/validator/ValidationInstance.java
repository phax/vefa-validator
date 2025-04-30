package no.difi.vefa.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.timing.StopWatch;

import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.ConvertedVefaDocument;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IValidation;
import no.difi.vefa.validator.api.IValidationSource;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.api.VefaDocument;
import no.difi.vefa.validator.lang.UnknownDocumentTypeException;
import no.difi.vefa.validator.lang.VefaValidatorException;
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

  private final ValidatorInstance m_aValidatorInstance;
  private final IProperties m_aProperties;
  private Configuration m_aConfiguration;

  /**
   * Final report.
   */
  private final Report m_aReport;

  /**
   * Section used to gather problems during validation.
   */
  private final Section m_aSection = new Section (new CombinedFlagFilterer ());

  /**
   * Document subject to validation.
   */
  private VefaDocument m_aDocument;

  private DeclarationWrapper m_aDeclaration;

  private List <IValidation> m_aChildren;

  /**
   * Constructing new validator using validator instance and validation source containing document
   * to validate.
   *
   * @param validatorInstance
   *        Instance of validator.
   * @param validationSource
   *        Source to validate.
   */
  private ValidationInstance (final ValidatorInstance validatorInstance, final IValidationSource validationSource)
  {
    this.m_aValidatorInstance = validatorInstance;
    this.m_aProperties = new CombinedProperties (validationSource.getProperties (), validatorInstance.getProperties ());

    this.m_aReport = new Report ();
    this.m_aReport.setUuid (UUID.randomUUID ().toString ());
    this.m_aReport.setFlag (FlagType.OK);

    this.m_aSection.setTitle ("Validator");
    this.m_aSection.setFlag (FlagType.OK);

    try
    {
      _loadDocument (validationSource.getInputStream ());
      _loadConfiguration ();
      nestedValidation ();

      if (m_aConfiguration != null)
        _validate ();
    }
    catch (final IOException e)
    {
      log.warn (e.getMessage (), e);
    }
    catch (final UnknownDocumentTypeException e)
    {
      m_aSection.add ("SYSTEM-003", e.getMessage (), FlagType.UNKNOWN);
    }
    catch (final VefaValidatorException e)
    {
      m_aSection.add ("SYSTEM-001", e.getMessage (), FlagType.FATAL);
    }

    if (m_aReport.getTitle () == null)
      m_aReport.setTitle ("Unknown document type");

    if (m_aSection.getAssertion ().size () > 0)
    {
      for (final AssertionType assertionType : m_aSection.getAssertion ())
      {
        if (assertionType.getFlag ().compareTo (m_aSection.getFlag ()) > 0)
          m_aSection.setFlag (assertionType.getFlag ());
      }
      m_aReport.getSection ().add (0, m_aSection);

      if (m_aSection.getFlag ().compareTo (getReport ().getFlag ()) > 0)
        getReport ().setFlag (m_aSection.getFlag ());
    }
  }

  private void _loadDocument (final InputStream inputStream) throws VefaValidatorException, IOException
  {
    final NonBlockingByteArrayInputStream aBAIS;
    if (inputStream instanceof NonBlockingByteArrayInputStream)
    {
      // Use stream as-is.
      aBAIS = (NonBlockingByteArrayInputStream) inputStream;
    }
    else
    {
      // Convert stream to ByteArrayOutputStream
      final NonBlockingByteArrayOutputStream byteArrayOutputStream = new NonBlockingByteArrayOutputStream ();
      StreamHelper.copyInputStreamToOutputStream (inputStream, byteArrayOutputStream);
      aBAIS = byteArrayOutputStream.getAsInputStream ();
    }
    log.info ("Loading document from stream");

    if (false)
    {
      // To be able to reuse the stream later on.
      m_aDocument = new VefaDocument (aBAIS);
      aBAIS.reset ();
    }

    // Use declaration implementations to detect declaration to use.
    final DeclarationIdentifier declarationIdentifier = m_aValidatorInstance.detect (aBAIS);
    m_aDeclaration = declarationIdentifier.getDeclaration ();
    if (declarationIdentifier.equals (DeclarationDetector.UNKNOWN))
      throw new UnknownDocumentTypeException ("Unable to detect type of content.");
    log.info ("  Detected declaration: " +
              m_aDeclaration.getType () +
              (m_aDeclaration.supportsConverter () ? " (with converter)" : "") +
              (m_aDeclaration.supportsChildren () ? " (with children)" : ""));

    // Detect expectation
    IExpectation expectation = null;
    if (m_aProperties.getBoolean ("feature.expectation"))
    {
      // TODO this is buggy - reads only a limit amount of the file for test
      final byte [] bytes = StreamUtils.read50KAndReset (aBAIS);
      expectation = m_aDeclaration.expectations (bytes);
      if (expectation != null)
      {
        m_aReport.setDescription (expectation.getDescription ());
        log.info ("  Detected expectation: " + expectation.getDescription ());
      }
    }

    if (m_aDeclaration.supportsConverter ())
    {
      final NonBlockingByteArrayOutputStream convertedOutputStream = new NonBlockingByteArrayOutputStream ();
      aBAIS.reset ();
      m_aDeclaration.convert (aBAIS, convertedOutputStream);

      m_aDocument = new ConvertedVefaDocument (convertedOutputStream.getAsInputStream (),
                                               aBAIS,
                                               declarationIdentifier.getFullIdentifier (),
                                               expectation);
    }
    else
    {
      m_aDocument = new VefaDocument (aBAIS, declarationIdentifier.getFullIdentifier (), expectation);
    }
  }

  private void _loadConfiguration () throws UnknownDocumentTypeException
  {
    // Default values for report
    m_aReport.setTitle ("Unknown document type");
    m_aReport.setFlag (FlagType.FATAL);

    // Get configuration using declaration
    m_aConfiguration = m_aValidatorInstance.getConfiguration (m_aDocument.getDeclarations ());

    if (!m_aProperties.getBoolean ("feature.suppress_notloaded"))
      for (final String notLoaded : m_aConfiguration.getNotLoaded ())
        m_aSection.add ("SYSTEM-007", "Validation artifact '" + notLoaded + "' not loaded.", FlagType.WARNING);

    // Update report using configuration for declaration
    m_aReport.setTitle (m_aConfiguration.getTitle ());
    m_aReport.setConfiguration (m_aConfiguration.getIdentifier ().getValue ());
    m_aReport.setBuild (m_aConfiguration.getBuild ());
    m_aReport.setFlag (FlagType.OK);
  }

  private void _validate ()
  {
    final StopWatch aSW = StopWatch.createdStarted ();

    for (final FileType fileType : m_aConfiguration.getFile ())
    {
      log.info ("Validating '" + fileType.getPath () + "'");

      try
      {
        final Section section = m_aValidatorInstance.check (fileType, m_aDocument, m_aConfiguration);
        section.setConfiguration (fileType.getConfiguration ());
        section.setBuild (fileType.getBuild ());
        m_aReport.getSection ().add (section);

        if (section.getFlag ().compareTo (getReport ().getFlag ()) > 0)
          getReport ().setFlag (section.getFlag ());
      }
      catch (final VefaValidatorException e)
      {
        this.m_aSection.add ("SYSTEM-008", e.getMessage (), FlagType.ERROR);
      }

      if (getReport ().getFlag ().equals (FlagType.FATAL) || m_aSection.getFlag ().equals (FlagType.FATAL))
        break;
    }

    for (final TriggerType triggerType : m_aConfiguration.getTrigger ())
    {
      try
      {
        final Section section = m_aValidatorInstance.trigger (triggerType, m_aDocument, m_aConfiguration);
        section.setConfiguration (triggerType.getConfiguration ());
        section.setBuild (triggerType.getBuild ());
        m_aReport.getSection ().add (section);

        if (section.getFlag ().compareTo (getReport ().getFlag ()) > 0)
          getReport ().setFlag (section.getFlag ());
      }
      catch (final VefaValidatorException e)
      {
        this.m_aSection.add ("SYSTEM-010", e.getMessage (), FlagType.ERROR);
      }
    }

    if (m_aDocument.getExpectation () != null)
      m_aDocument.getExpectation ().verify (m_aSection);

    aSW.stop ();
    m_aReport.setRuntime (aSW.getMillis () + "ms");
  }

  /**
   * Handling nested validation.
   */
  private void nestedValidation () throws VefaValidatorException
  {
    if (m_aReport.getFlag ().compareTo (FlagType.FATAL) < 0)
    {
      if (m_aDeclaration.supportsChildren () && m_aProperties.getBoolean ("feature.nesting"))
      {
        final Iterable <CachedFile> iterable = m_aDeclaration.children (m_aDocument.getInputStream ());
        for (final CachedFile cachedFile : iterable)
        {
          addChildValidation (ValidationInstance.of (m_aValidatorInstance,
                                                     new ValidationSourceImpl (cachedFile.getContentStream (), null)),
                              cachedFile.getFilename ());
        }
      }
    }
  }

  private void addChildValidation (final IValidation validation, final String filename)
  {
    final Report childReport = validation.getReport ();
    childReport.setFilename (filename);
    m_aReport.getReport ().add (childReport);

    if (m_aChildren == null)
      m_aChildren = new ArrayList <> ();
    m_aChildren.add (validation);
  }

  /**
   * Document used for validation as represented in the validator.
   *
   * @return Document object.
   */
  @Override
  public VefaDocument getDocument ()
  {
    return m_aDocument;
  }

  /**
   * Report is the result of validation.
   *
   * @return Report
   */
  @Override
  public Report getReport ()
  {
    return m_aReport;
  }

  /**
   * Nested validations of validation.
   *
   * @return List of validations or null if none available.
   */
  @Override
  public List <IValidation> getChildren ()
  {
    return m_aChildren;
  }

  public static ValidationInstance of (final ValidatorInstance validatorInstance,
                                       final IValidationSource validationSource)
  {
    return new ValidationInstance (validatorInstance, validationSource);
  }
}
