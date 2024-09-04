package no.difi.vefa.validator.build.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import no.difi.vefa.validator.api.IPreparer;
import no.difi.vefa.validator.build.model.Build;
import no.difi.vefa.validator.build.util.AsicArchiver;
import no.difi.vefa.validator.build.util.DirectoryCleaner;
import no.difi.vefa.validator.build.util.PreparerProvider;
import no.difi.vefa.validator.util.JAXBHelper;
import no.difi.xsd.vefa.validator._1.BuildConfigurations;
import no.difi.xsd.vefa.validator._1.ConfigurationType;
import no.difi.xsd.vefa.validator._1.Configurations;
import no.difi.xsd.vefa.validator._1.FileType;
import no.difi.xsd.vefa.validator._1.StylesheetType;

@Slf4j
@Singleton
public class BuildTask
{

  private static final JAXBContext JAXB_CONTEXT = JAXBHelper.context (Configurations.class, BuildConfigurations.class);

  @Inject
  private PreparerProvider preparerProvider;

  public void build (final Build build) throws IOException, JAXBException
  {
    final Path contentsPath = build.getTargetFolder ().resolve ("contents");

    if (Files.exists (build.getTargetFolder ()))
      DirectoryCleaner.clean (build.getTargetFolder (), false);
    Files.createDirectories (contentsPath);

    final Configurations configurations = build.getConfigurations ();

    final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller ();

    for (final Path sourcePath : build.getSourcePath ())
    {

      final File workFolder = sourcePath.toFile ();

      log.info ("Source '{}'", workFolder.getAbsolutePath ());

      // Find configurations
      for (final File file : FileUtils.listFiles (workFolder,
                                                  new NameFileFilter (build.getSetting ("config")),
                                                  TrueFileFilter.INSTANCE))
      {
        try
        {
          final BuildConfigurations config = (BuildConfigurations) unmarshaller.unmarshal (new FileInputStream (file));

          final File configFolder = new File (file.getParent ());

          for (final ConfigurationType configuration : config.getConfiguration ())
          {
            for (final FileType fileType : configuration.getFile ())
            {
              fileType.setSource (fileType.getSource () != null ? fileType.getSource () : fileType.getPath ());

              preparerProvider.prepare (configFolder.toPath ().resolve (fileType.getSource ()),
                                        contentsPath.resolve (fileType.getPath ()),
                                        IPreparer.EType.FILE);

              fileType.setSource (null);
            }

            if (configuration.getStylesheet () != null)
            {
              final StylesheetType stylesheet = configuration.getStylesheet ();
              stylesheet.setSource (stylesheet.getSource () != null ? stylesheet.getSource () : stylesheet.getPath ());

              preparerProvider.prepare (configFolder.toPath ().resolve (stylesheet.getSource ()),
                                        contentsPath.resolve (stylesheet.getPath ()),
                                        IPreparer.EType.STYLESHEET);

              stylesheet.setSource (null);
            }

            configuration.setBuild (configuration.getBuild () != null ? configuration.getBuild ()
                                                                      : build.getSetting ("build"));
            configuration.setWeight (configuration.getWeight () != 0 ? configuration.getWeight ()
                                                                     : Long.parseLong (build.getSetting ("weight")));

            configurations.getConfiguration ().add (configuration);
          }

          for (final FileType fileType : config.getInclude ())
          {
            if (fileType.getSource () == null)
              fileType.setSource (fileType.getPath ());

            preparerProvider.prepare (configFolder.toPath ().resolve (fileType.getSource ()),
                                      contentsPath.resolve (fileType.getPath ()),
                                      IPreparer.EType.INCLUDE);
          }

          configurations.getPackage ().addAll (config.getPackage ());

          for (final String testFolder : config.getTestfolder ())
            build.addTestFolder (".".equals (testFolder) ? configFolder : new File (configFolder, testFolder));

          log.info ("Loading '{}'", file.toString ());
        }
        catch (final JAXBException e)
        {
          log.warn ("Loading failed for '{}'", file.toString (), e);
        }
      }
    }

    final String configFilename = String.format ("config-%s-%s.xml",
                                                 build.getSetting ("name"),
                                                 build.getSetting ("build"));
    try (OutputStream outputStream = Files.newOutputStream (contentsPath.resolve (configFilename)))
    {
      final Marshaller marshaller = JAXB_CONTEXT.createMarshaller ();
      marshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal (configurations, outputStream);
    }

    AsicArchiver.archive (build.getTargetFolder ()
                               .resolve (String.format ("%s-%s.asice",
                                                        build.getSetting ("name"),
                                                        build.getSetting ("build"))),
                          contentsPath);

    DirectoryCleaner.clean (contentsPath, true);
  }
}
