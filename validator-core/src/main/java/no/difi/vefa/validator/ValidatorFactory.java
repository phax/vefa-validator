package no.difi.vefa.validator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;

import com.google.errorprone.annotations.Immutable;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.Processor;
import no.difi.vefa.validator.configuration.AsiceConfigurationProvider;
import no.difi.vefa.validator.configuration.ValidatorTestConfigurationProvider;
import no.difi.xsd.vefa.validator._1.Configurations;

@Immutable
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

  public static final List <Configurations> CONFIGURATIONS = new ArrayList <> ();
  static
  {
    CONFIGURATIONS.add (new AsiceConfigurationProvider ().getConfigurations ());
    CONFIGURATIONS.add (new ValidatorTestConfigurationProvider ().getConfigurations ());
  }

  private ValidatorFactory ()
  {}

}
