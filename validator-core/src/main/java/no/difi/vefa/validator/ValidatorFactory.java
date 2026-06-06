package no.difi.vefa.validator;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.stream.StreamSource;

import com.helger.cache.IMutableCache;
import com.helger.cache.impl.ProviderCache;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.api.IDeclaration;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.ITrigger;
import no.difi.vefa.validator.checker.SchematronCheckerFactory;
import no.difi.vefa.validator.checker.SchematronXsltCheckerFactory;
import no.difi.vefa.validator.checker.XsdCheckerFactory;
import no.difi.vefa.validator.configuration.AsiceConfigurationProvider;
import no.difi.vefa.validator.configuration.ValidatorTestConfigurationProvider;
import no.difi.vefa.validator.declaration.AsiceDeclaration;
import no.difi.vefa.validator.declaration.AsiceXmlDeclaration;
import no.difi.vefa.validator.declaration.EspdDeclaration;
import no.difi.vefa.validator.declaration.NoblDeclaration;
import no.difi.vefa.validator.declaration.SbdhDeclaration;
import no.difi.vefa.validator.declaration.UblDeclaration;
import no.difi.vefa.validator.declaration.UnCefactDeclaration;
import no.difi.vefa.validator.declaration.ValidatorTestDeclaration;
import no.difi.vefa.validator.declaration.ValidatorTestSetDeclaration;
import no.difi.vefa.validator.declaration.XmlDeclaration;
import no.difi.vefa.validator.declaration.ZipDeclaration;
import no.difi.vefa.validator.trigger.AsiceTrigger;
import no.difi.vefa.validator.util.DeclarationDetector;
import no.difi.xsd.vefa.validator._1.Configurations;

public final class ValidatorFactory
{
  public static final XMLInputFactory XML_INPUT_FACTORY;

  static
  {
    XML_INPUT_FACTORY = XMLInputFactory.newFactory ();
    XML_INPUT_FACTORY.setProperty (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    XML_INPUT_FACTORY.setProperty (XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
  }

  public static final Processor SAXON_PROCESSOR;

  static
  {
    final Configuration configuration = new Configuration ();
    configuration.setConfigurationProperty (Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
    SAXON_PROCESSOR = new Processor (configuration);
  }

  public static final XsltExecutable SBDH_EXTRACTOR;

  static
  {
    try (InputStream inputStream = ValidatorFactory.class.getResourceAsStream ("/vefa-validator/xslt/sbdh-extractor.xslt"))
    {
      final XsltCompiler xsltCompiler = SAXON_PROCESSOR.newXsltCompiler ();
      SBDH_EXTRACTOR = xsltCompiler.compile (new StreamSource (inputStream));
    }
    catch (IOException | SaxonApiException e)
    {
      throw new IllegalStateException ("Unable to load extraction of SBDH content.", e);
    }
  }

  public static final List <Configurations> CONFIGURATIONS = new ArrayList <> ();
  static
  {
    CONFIGURATIONS.add (new AsiceConfigurationProvider ().getConfigurations ());
    CONFIGURATIONS.add (new ValidatorTestConfigurationProvider ().getConfigurations ());
  }

  private ValidatorFactory ()
  {}

  public static List <IDeclaration> createDeclarations ()
  {
    final List <IDeclaration> list = new ArrayList <> ();
    list.add (new AsiceDeclaration ());
    list.add (new AsiceXmlDeclaration ());
    list.add (new EspdDeclaration ());
    list.add (new NoblDeclaration ());
    list.add (new SbdhDeclaration ());
    list.add (new UblDeclaration ());
    list.add (new UnCefactDeclaration ());
    list.add (new ValidatorTestDeclaration ());
    list.add (new ValidatorTestSetDeclaration ());
    list.add (new XmlDeclaration ());
    list.add (new ZipDeclaration ());
    return Collections.unmodifiableList (list);
  }

  public static List <ICheckerFactory> createCheckerFactories ()
  {
    final List <ICheckerFactory> list = new ArrayList <> ();
    list.add (new SchematronCheckerFactory ());
    list.add (new SchematronXsltCheckerFactory ());
    list.add (new XsdCheckerFactory ());
    return Collections.unmodifiableList (list);
  }

  public static List <ITrigger> createTriggers ()
  {
    final List <ITrigger> list = new ArrayList <> ();
    list.add (new AsiceTrigger ());
    return Collections.unmodifiableList (list);
  }

  public static DeclarationDetector createDeclarationDetector ()
  {
    return new DeclarationDetector (createDeclarations ());
  }

  // Note: ph-cache offers expireAfterWrite (fixed lifetime from put), not Guava's
  // expireAfterAccess (sliding window). The pools.checker.expire value is reused as-is.
  public static IMutableCache <String, IChecker> createCheckerCache (final IProperties properties,
                                                                     final CheckerCacheLoader loader)
  {
    return ProviderCache.<String, IChecker> builder ()
                        .name ("vefa-checker-cache")
                        .maxSize (properties.getInteger ("pools.checker.size"))
                        .expireAfterWrite (Duration.ofMinutes (properties.getInteger ("pools.checker.expire")))
                        .valueProvider (loader)
                        .build ();
  }
}
