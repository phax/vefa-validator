package no.difi.vefa.validator.api;

import no.difi.vefa.validator.lang.VefaValidatorException;

import java.io.InputStream;

public interface IDeclarationWithChildren extends IDeclaration {

    Iterable<CachedFile> children(InputStream inputStream) throws VefaValidatorException;

}
