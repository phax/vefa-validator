package no.difi.vefa.validator;

import javax.xml.stream.XMLInputFactory;

import com.google.errorprone.annotations.Immutable;

@Immutable
public final class ValidatorXml
{
  public static final XMLInputFactory XML_INPUT_FACTORY;

  static
  {
    XML_INPUT_FACTORY = XMLInputFactory.newFactory ();
    XML_INPUT_FACTORY.setProperty (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    XML_INPUT_FACTORY.setProperty (XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
  }

  private ValidatorXml ()
  {}
}
