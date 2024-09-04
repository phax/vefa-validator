package no.difi.vefa.validator.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.IDeclaration;
import no.difi.vefa.validator.api.IDeclarationWithChildren;
import no.difi.vefa.validator.api.IDeclarationWithConverter;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.lang.ValidatorException;

public class DeclarationWrapper implements IDeclaration, IDeclarationWithChildren, IDeclarationWithConverter
{

  private final String type;

  private final IDeclaration declaration;

  private final List <DeclarationWrapper> children = new ArrayList <> ();

  public static DeclarationWrapper of (final String type, final IDeclaration declaration)
  {
    return new DeclarationWrapper (type, declaration);
  }

  private DeclarationWrapper (final String type, final IDeclaration declaration)
  {
    this.type = type;
    this.declaration = declaration;
  }

  public String getType ()
  {
    return type;
  }

  public IDeclaration getDeclaration ()
  {
    return declaration;
  }

  public List <DeclarationWrapper> getChildren ()
  {
    return children;
  }

  @Override
  public boolean verify (final byte [] content, final List <String> parent) throws ValidatorException
  {
    return declaration.verify (content, parent);
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent) throws ValidatorException
  {
    return declaration.detect (contentStream, parent);
  }

  @Override
  public IExpectation expectations (final byte [] content) throws ValidatorException
  {
    return declaration.expectations (content);
  }

  public boolean supportsChildren ()
  {
    return declaration instanceof IDeclarationWithChildren;
  }

  @Override
  public Iterable <CachedFile> children (final InputStream inputStream) throws ValidatorException
  {
    return ((IDeclarationWithChildren) declaration).children (inputStream);
  }

  public boolean supportsConverter ()
  {
    return declaration instanceof IDeclarationWithConverter;
  }

  @Override
  public void convert (final InputStream inputStream, final OutputStream outputStream) throws ValidatorException
  {
    ((IDeclarationWithConverter) declaration).convert (inputStream, outputStream);
  }

  @Override
  public String toString ()
  {
    return type + " // " + declaration.getClass ().getName ();
  }
}
