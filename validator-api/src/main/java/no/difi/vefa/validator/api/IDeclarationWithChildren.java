package no.difi.vefa.validator.api;

import java.io.InputStream;

import no.difi.vefa.validator.lang.VefaValidatorException;

public interface IDeclarationWithChildren extends IDeclaration
{
  Iterable <CachedFile> children (InputStream inputStream) throws VefaValidatorException;
}
