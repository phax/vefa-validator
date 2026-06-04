package no.difi.vefa.validator.declaration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import no.difi.vefa.validator.ValidatorFactory;
import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.util.DeclarationDetector;
import no.difi.vefa.validator.util.DeclarationIdentifier;

public class SbdhDeclarationTest
{

  private DeclarationDetector declarationDetector;

  @Before
  public void beforeClass ()
  {
    declarationDetector = ValidatorFactory.createDeclarationDetector ();
  }

  @Test
  public void simpleSbdh () throws Exception
  {

    try (
        InputStream inputStream = new BufferedInputStream (getClass ().getResourceAsStream ("/documents/peppol-bis-invoice-sbdh.xml")))
    {
      final DeclarationIdentifier declarationIdentifier = declarationDetector.detect (inputStream);
      assertEquals (declarationIdentifier.getIdentifier ().get (1), "SBDH:1.0");
      final Iterator <CachedFile> iterator = declarationIdentifier.getDeclaration ().children (inputStream).iterator ();
      assertTrue (iterator.hasNext ());
    }
  }

  @Test
  public void simpleSbdhOnly () throws Exception
  {

    try (
        InputStream inputStream = new BufferedInputStream (getClass ().getResourceAsStream ("/documents/sbdh-only.xml")))
    {
      final DeclarationIdentifier declarationIdentifier = declarationDetector.detect (inputStream);
      assertEquals (declarationIdentifier.getIdentifier ().get (1), "SBDH:1.0");
      final Iterator <CachedFile> iterator = declarationIdentifier.getDeclaration ().children (inputStream).iterator ();
      assertFalse (iterator.hasNext ());
    }
  }
}
