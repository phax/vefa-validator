package no.difi.vefa.validator.api;

import org.jspecify.annotations.NonNull;

import no.difi.xsd.vefa.validator._1.Configurations;

/**
 * @author erlend
 */
public interface IConfigurationProvider
{
  @NonNull
  Configurations getConfigurations ();
}
