package no.difi.vefa.validator.expectation;

import java.io.ByteArrayInputStream;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import no.difi.vefa.validator.util.JAXBHelper;
import no.difi.xsd.vefa.validator._1.AssertElementType;
import no.difi.xsd.vefa.validator._1.AssertType;
import no.difi.xsd.vefa.validator._1.Test;

public class ValidatorTestExpectation extends AbstractExpectation
{
  private static final Logger log = LoggerFactory.getLogger (ValidatorTestExpectation.class);
  private static JAXBContext jaxbContext = JAXBHelper.context (Test.class);

  public ValidatorTestExpectation (final byte [] bytes)
  {
    try
    {
      final Test test = jaxbContext.createUnmarshaller ()
                                   .unmarshal (new StreamSource (new ByteArrayInputStream (bytes)), Test.class)
                                   .getValue ();
      final AssertType assertType = test.getAssert ();

      if (assertType != null)
      {
        description = test.getId () == null ? assertType.getDescription ()
                                            : String.format ("%s) %s", test.getId (), assertType.getDescription ());
        scopes.addAll (assertType.getScope ());

        for (final AssertElementType a : assertType.getFatal ())
          fatals.put (a.getValue (), a.getNumber () == null ? Integer.valueOf (1) : a.getNumber ());
        for (final AssertElementType a : assertType.getError ())
          errors.put (a.getValue (), a.getNumber () == null ? Integer.valueOf (1) : a.getNumber ());
        for (final AssertElementType a : assertType.getWarning ())
          warnings.put (a.getValue (), a.getNumber () == null ? Integer.valueOf (1) : a.getNumber ());
        for (final String s : assertType.getSuccess ())
          successes.put (s, 1);
      }

    }
    catch (final JAXBException e)
    {
      log.warn (e.getMessage (), e);
    }
  }
}
