package no.difi.vefa.validator.util;

import no.difi.vefa.validator.api.IFlagFilterer;
import no.difi.xsd.vefa.validator._1.AssertionType;

public class CombinedFlagFilterer implements IFlagFilterer {

    private IFlagFilterer[] flagFilterers;

    public CombinedFlagFilterer(IFlagFilterer... flagFilterers) {
        this.flagFilterers = flagFilterers;
    }

    @Override
    public void filterFlag(AssertionType assertionType) {
        for (IFlagFilterer flagFilterer : flagFilterers)
            if (flagFilterer != null)
                flagFilterer.filterFlag(assertionType);
    }
}
