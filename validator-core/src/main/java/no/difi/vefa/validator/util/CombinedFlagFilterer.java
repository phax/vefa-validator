package no.difi.vefa.validator.util;

import no.difi.vefa.validator.api.IFlagFilter;
import no.difi.xsd.vefa.validator._1.AssertionType;

public class CombinedFlagFilterer implements IFlagFilter
{
  private final IFlagFilter [] flagFilterers;

  public CombinedFlagFilterer (final IFlagFilter... flagFilterers)
  {
    this.flagFilterers = flagFilterers;
  }

  @Override
  public void filterFlag (final AssertionType assertionType)
  {
    for (final IFlagFilter flagFilterer : flagFilterers)
      if (flagFilterer != null)
        flagFilterer.filterFlag (assertionType);
  }
}
