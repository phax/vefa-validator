package no.difi.vefa.validator.trigger;

import com.helger.asic.AsicVerifier;
import com.helger.asic.AsicVerifierFactory;
import com.helger.asic.jaxb.asic.Certificate;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.Document;
import no.difi.vefa.validator.api.Section;
import no.difi.vefa.validator.api.ITrigger;
import no.difi.xsd.vefa.validator._1.FlagType;

@Type ("asice")
public class AsiceTrigger implements ITrigger
{

  private final static AsicVerifierFactory factory = AsicVerifierFactory.newFactory ();

  @Override
  public void check (final Document document, final Section section)
  {
    try
    {
      section.setTitle ("ASiC-E Verifier");
      final AsicVerifier verifier = factory.verify (document.getInputStream ());

      for (final Certificate certificate : verifier.getAsicManifest ().getCertificate ())
        section.add ("ASICE-001", "Certificate: " + certificate.getSubject (), FlagType.INFO);
    }
    catch (final Exception e)
    {
      section.add ("ASICE-002", e.getMessage (), FlagType.FATAL);
    }
  }
}
