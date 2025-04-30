package no.difi.vefa.validator.api;

import java.io.InputStream;
import java.io.OutputStream;

import no.difi.vefa.validator.lang.VefaValidatorException;

public interface IDeclarationWithConverter extends IDeclaration {

  void convert(InputStream inputStream, OutputStream outputStream) throws VefaValidatorException;

}
