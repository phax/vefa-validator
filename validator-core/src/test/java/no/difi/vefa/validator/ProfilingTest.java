package no.difi.vefa.validator;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import no.difi.vefa.validator.api.IValidation;
import no.difi.xsd.vefa.validator._1.FlagType;

public class ProfilingTest
{

  private static Validator validator;

  @BeforeClass
  public static void beforeClass () throws Exception
  {
    validator = ValidatorBuilder.newValidator ().build ();
  }

  @Test
  @Ignore
  public void simple () throws Exception
  {
    for (int i = 0; i < 2000; i++)
    {
      try (InputStream inputStream = getClass ().getResourceAsStream ("/documents/huge-001.xml.gz"))
      {
        final GZIPInputStream gzipInputStream = new GZIPInputStream (inputStream);

        final IValidation validation = validator.validate (gzipInputStream);
        assertEquals (FlagType.ERROR, validation.getReport ().getFlag ());

        gzipInputStream.close ();
        inputStream.close ();

        System.out.println (i);
      }
    }
  }
}
