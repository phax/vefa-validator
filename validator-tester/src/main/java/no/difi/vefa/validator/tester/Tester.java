package no.difi.vefa.validator.tester;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import no.difi.vefa.validator.Validator;
import no.difi.vefa.validator.ValidatorBuilder;
import no.difi.vefa.validator.api.IValidation;
import no.difi.vefa.validator.properties.SimpleProperties;
import no.difi.vefa.validator.source.DirectorySource;
import no.difi.vefa.validator.source.RepositorySource;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.SectionType;

@Singleton
public class Tester implements Closeable
{
  private static final Logger log = LoggerFactory.getLogger (Tester.class);

  private Validator validator;
  private final List <IValidation> validations = new ArrayList <> ();
  private int tests;
  private int failed;

  public static List <IValidation> perform (final Path artifactsPath, final List <Path> testPaths)
  {
    try (Tester tester = new Tester (artifactsPath))
    {
      for (final Path path : testPaths)
        tester.perform (path);
      return tester.finish ();
    }
  }

  public static List <IValidation> perform (final URI artifactsUri, final List <Path> testPaths)
  {
    try (Tester tester = new Tester (artifactsUri))
    {
      for (final Path path : testPaths)
        tester.perform (path);
      return tester.finish ();
    }
  }

  private Tester (final Path artifactsPath)
  {
    validator = ValidatorBuilder.newValidator ()
                                .setProperties (new SimpleProperties ().set ("feature.nesting", true)
                                                                       .set ("feature.expectation", true)
                                                                       .set ("feature.suppress_notloaded", true))
                                .setSource (new DirectorySource (artifactsPath))
                                .build ();
  }

  private Tester (final URI artifactsUri)
  {
    validator = ValidatorBuilder.newValidator ()
                                .setProperties (new SimpleProperties ().set ("feature.nesting", true)
                                                                       .set ("feature.expectation", true)
                                                                       .set ("feature.suppress_notloaded", true))
                                .setSource (new RepositorySource (artifactsUri))
                                .build ();
  }

  private void perform (final Path path)
  {
    final List <File> files = new ArrayList <> (FileUtils.listFiles (path.toFile (),
                                                                     new WildcardFileFilter ("*.xml"),
                                                                     TrueFileFilter.INSTANCE));
    Collections.sort (files);

    for (final File file : files)
      if (!file.getName ().equals ("buildconfig.xml"))
        validate (file);
  }

  private List <IValidation> finish ()
  {
    log.info ("{} tests performed, {} tests failed", tests, failed);

    return validations;
  }

  private void validate (final File file)
  {
    try
    {
      final IValidation validation = validator.validate (file);
      validation.getReport ().setFilename (file.toString ());

      if (validation.getDocument ()
                    .getDeclarations ()
                    .contains ("xml.testset::http://difi.no/xsd/vefa/validator/1.0::testSet"))
      {
        log.info ("TestSet '{}'", file);

        for (int i = 0; i < validation.getChildren ().size (); i++)
        {
          final IValidation v = validation.getChildren ().get (i);
          v.getReport ().setFilename (String.format ("%s (%s)", file, i + 1));
          append (v.getDocument ().getExpectation ().getDescription (), v, i + 1);
        }
      }
      else
      {
        append (file.toString (), validation, null);
      }
    }
    catch (final NullPointerException e)
    {
      log.warn ("File '{}' ({})", file, "Unable to parse file - please make sure it contains valid xml.");
    }
    catch (final IOException e)
    {
      log.warn ("Test '{}' ({})", file, e.getMessage (), e);
    }
  }

  public void append (String description, final IValidation validation, final Integer numberInSet)
  {
    validations.add (validation);
    tests++;

    description = description.replaceAll ("[ \\t\\r\\n]+", " ");

    final String prefix = numberInSet == null ? "" : "  ";

    if (validation.getReport ().getFlag ().compareTo (FlagType.EXPECTED) > 0)
    {
      log.warn ("{}Test '{}' ({})", prefix, description, validation.getReport ().getFlag ());
      failed++;

      for (final SectionType sectionType : validation.getReport ().getSection ())
        for (final AssertionType assertionType : sectionType.getAssertion ())
          if (assertionType.getFlag ().compareTo (FlagType.EXPECTED) > 0)
            log.info ("{}  * {} {} ({})",
                      prefix,
                      assertionType.getIdentifier (),
                      assertionType.getText (),
                      assertionType.getFlag ());
    }
    else
      if (numberInSet == null)
      {
        log.info ("Test '{}'", description);
      }
  }

  @Override
  public void close ()
  {
    if (validator != null)
    {
      validator.close ();
      validator = null;
    }
  }
}
