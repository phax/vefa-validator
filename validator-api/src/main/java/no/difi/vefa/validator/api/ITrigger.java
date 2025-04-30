package no.difi.vefa.validator.api;

import no.difi.vefa.validator.lang.VefaValidatorException;

public interface ITrigger {

    void check(VefaDocument document, Section section) throws VefaValidatorException;
    
}
