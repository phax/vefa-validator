package no.difi.vefa.validator.util;

import jakarta.xml.bind.JAXBContext;

public class JAXBHelper
{

  public static JAXBContext context (final Class <?>... classes)
  {
    try
    {
      return JAXBContext.newInstance (classes);
    }
    catch (final Exception e)
    {
      throw new IllegalStateException ("Unable to load JAXBContext.", e);
    }
  }
}
