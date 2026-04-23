package no.difi.vefa.validator.api;

import org.jspecify.annotations.NonNull;

import no.difi.vefa.validator.lang.VefaValidatorException;

public interface ITrigger
{
  void check (@NonNull VefaDocument document, @NonNull Section section) throws VefaValidatorException;
}
