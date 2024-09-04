package no.difi.vefa.validator.api;

import no.difi.vefa.validator.lang.ValidatorException;

import java.io.InputStream;

public interface IDeclarationWithChildren extends IDeclaration {

    Iterable<CachedFile> children(InputStream inputStream) throws ValidatorException;

}
