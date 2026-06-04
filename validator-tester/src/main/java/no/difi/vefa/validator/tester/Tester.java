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
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.difi.vefa.validator.Validator;
import no.difi.vefa.validator.ValidatorBuilder;
import no.difi.vefa.validator.api.IValidation;
import no.difi.vefa.validator.properties.SimpleProperties;
import no.difi.vefa.validator.source.DirectorySource;
import no.difi.vefa.validator.source.RepositorySource;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.SectionType;

public class Tester implements Closeable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (Tester.class);

  private Validator validator;
  private final List <IValidation> validations = new ArrayList <> ();
  private int tests;
  private int failed;

  public static List <IValidation> perform (final Path artifactsPath, final List <Path> testPaths)
  {
    try (Tester tester = new Tester (artifactsPath))
    {
      for (final Path path : testPaths)
        tester._perform (path);
      return tester._finish ();
    }
  }

  public static List <IValidation> perform (final URI artifactsUri, final List <Path> testPaths)
  {
    try (Tester tester = new Tester (artifactsUri))
    {
      for (final Path path : testPaths)
        tester._perform (path);
      return tester._finish ();
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

  private void _perform (final Path path)
  {
    final List <File> files = new ArrayList <> (FileUtils.listFiles (path.toFile (),
                                                                     WildcardFileFilter.builder ()
                                                                                       .setWildcards ("*.xml")
                                                                                       .get (),
                                                                     TrueFileFilter.INSTANCE));
    Collections.sort (files);

    for (final File file : files)
      if (!file.getName ().equals ("buildconfig.xml"))
        _validate (file);
  }

  @NonNull
  private List <IValidation> _finish ()
  {
    LOGGER.info (tests + " tests performed, " + failed + " tests failed");

    return validations;
  }

  private void _validate (final File file)
  {
    try
    {
      final IValidation validation = validator.validate (file);
      validation.getReport ().setFilename (file.toString ());

      if (validation.getDocument ()
                    .getDeclarations ()
                    .contains ("xml.testset::http://difi.no/xsd/vefa/validator/1.0::testSet"))
      {
        LOGGER.info ("TestSet '" + file + "'");

        int i = 0;
        for (final IValidation v : validation.getChildren ())
        {
          v.getReport ().setFilename (file.toString () + " (" + (i + 1) + ")");
          append (v.getDocument ().getExpectation ().getDescription (), v, i + 1);
          i++;
        }
      }
      else
      {
        append (file.toString (), validation, -1);
      }
    }
    catch (final NullPointerException e)
    {
      LOGGER.warn ("File '" + file + "' (Unable to parse file - please make sure it contains valid XML)");
    }
    catch (final IOException e)
    {
      LOGGER.warn ("Test '" + file + "' (" + e.getMessage () + ")", e);
    }
  }

  public void append (String description, final IValidation validation, final int numberInSet)
  {
    validations.add (validation);
    tests++;

    description = description.replaceAll ("[ \\t\\r\\n]+", " ");

    final String prefix = numberInSet < 0 ? "" : "  ";

    if (validation.getReport ().getFlag ().compareTo (FlagType.EXPECTED) > 0)
    {
      LOGGER.warn (prefix + "Test '" + description + "' (" + validation.getReport ().getFlag () + ")");
      failed++;

      for (final SectionType sectionType : validation.getReport ().getSection ())
        for (final AssertionType assertionType : sectionType.getAssertion ())
          if (assertionType.getFlag ().compareTo (FlagType.EXPECTED) > 0)
            LOGGER.info (prefix +
                         " * " +
                         assertionType.getIdentifier () +
                         " " +
                         assertionType.getText () +
                         " (" +
                         assertionType.getFlag () +
                         ")");
    }
    else
      if (numberInSet <= 0)
      {
        LOGGER.info ("Test '" + description + "'");
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
