package no.difi.vefa.validator.checker;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.xml.transform.stream.StreamSource;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.base.timing.StopWatch;
import com.helger.io.resource.ClassPathResource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.TeeDestination;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import no.difi.vefa.validator.ValidatorFactory;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.api.VefaDocument;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.JAXBHelper;
import no.difi.vefa.validator.util.VefaSaxonErrorListener;
import no.difi.vefa.validator.util.VefaSaxonMessageListener;
import no.difi.xsd.vefa.validator._1.SectionType;

public class SchematronXsltChecker implements IChecker
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SchematronXsltChecker.class);
  private static final JAXBContext JAXB_CONTEXT = JAXBHelper.context (SectionType.class);

  /**
   * System property that stores each created SVRL as <code>svrl-&lt;UUID&gt;.xml</code>. The value
   * <code>true</code> uses the current working directory, any other non-empty value (except
   * <code>false</code>) is used as the target directory, which is created if needed. Only intended for
   * debugging.
   */
  public static final String SYSTEM_PROPERTY_SAVE_SVRL = "vefa.validator.debug.saveSvrl";

  private static final XsltExecutable SVRL_PARSER;
  static
  {
    LOGGER.info ("Compiling SVRL Parser");
    try (final InputStream inputStream = ClassPathResource.getInputStream ("/vefa-validator/xslt/svrl-parser.xslt",
                                                                           SchematronXsltChecker.class.getClassLoader ()))
    {
      final XsltCompiler xsltCompiler = ValidatorFactory.SAXON_PROCESSOR.newXsltCompiler ();
      SVRL_PARSER = xsltCompiler.compile (new StreamSource (inputStream));
    }
    catch (final Exception e)
    {
      throw new IllegalStateException ("Unable to load parsing of Schematron reports.", e);
    }
  }

  private final XsltExecutable m_aXsltExecutable;

  public SchematronXsltChecker (final XsltExecutable xsltExecutable)
  {
    m_aXsltExecutable = xsltExecutable;
  }

  @NonNull
  private static Destination _wrapWithSvrlFile (@NonNull final Destination aDestination,
                                                @NonNull final String sTargetDir)
  {
    final File aDir = "true".equalsIgnoreCase (sTargetDir) ? new File (".") : new File (sTargetDir);
    if (!aDir.isDirectory () && !aDir.mkdirs ())
      throw new IllegalStateException ("Failed to create SVRL target directory '" + aDir.getAbsolutePath () + "'");
    final File aSvrlFile = new File (aDir, "svrl-" + UUID.randomUUID ().toString () + ".xml");
    LOGGER.info ("Saving SVRL to '" + aSvrlFile.getAbsolutePath () + "'");
    return new TeeDestination (aDestination, ValidatorFactory.SAXON_PROCESSOR.newSerializer (aSvrlFile));
  }

  @Override
  public void check (@NonNull final VefaDocument document, @NonNull final Section section) throws VefaValidatorException
  {
    LOGGER.info ("Running Schematron XSTL-based validation");
    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      final NonBlockingByteArrayOutputStream baos = new NonBlockingByteArrayOutputStream ();
      {
        final XsltTransformer parser = SVRL_PARSER.load ();
        parser.setErrorListener (VefaSaxonErrorListener.INSTANCE);
        parser.setMessageHandler (VefaSaxonMessageListener.INSTANCE);
        parser.setDestination (ValidatorFactory.SAXON_PROCESSOR.newSerializer (baos));

        final XsltTransformer schematron = m_aXsltExecutable.load ();
        schematron.setErrorListener (VefaSaxonErrorListener.INSTANCE);
        schematron.setMessageHandler (VefaSaxonMessageListener.INSTANCE);
        schematron.setSource (new StreamSource (document.getInputStream ()));
        final String sSaveSvrl = System.getProperty (SYSTEM_PROPERTY_SAVE_SVRL);
        final boolean bSaveSvrl = sSaveSvrl != null && !sSaveSvrl.isEmpty () && !"false".equalsIgnoreCase (sSaveSvrl);
        schematron.setDestination (bSaveSvrl ? _wrapWithSvrlFile (parser, sSaveSvrl) : parser);

        schematron.transform ();

        schematron.close ();
        parser.close ();
      }

      aSW.stop ();

      if (false)
        LOGGER.info ("Inbetween: " + baos.getAsString (StandardCharsets.UTF_8));

      final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();
      final SectionType sectionType = unmarshaller.unmarshal (new StreamSource (baos.getAsInputStream ()),
                                                              SectionType.class).getValue ();

      section.setTitle (sectionType.getTitle ());
      section.add (sectionType.getAssertion ());
      section.setRuntime (aSW.getMillis () + "ms");
    }
    catch (final Exception e)
    {
      throw new VefaValidatorException ("Unable to perform check: " + e.getMessage (), e);
    }
  }
}
