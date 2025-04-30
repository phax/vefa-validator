package no.difi.vefa.validator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeclarationIdentifier
{
  private final DeclarationIdentifier parent;
  private final DeclarationWrapper declaration;
  private final List <String> identifiers;

  public DeclarationIdentifier (final DeclarationIdentifier parent,
                                final DeclarationWrapper declaration,
                                final List <String> identifiers)
  {
    this.parent = parent;
    this.declaration = declaration;
    this.identifiers = identifiers;
  }

  public DeclarationIdentifier getParent ()
  {
    return parent;
  }

  public DeclarationWrapper getDeclaration ()
  {
    return declaration;
  }

  public List <String> getIdentifier ()
  {
    return identifiers;
  }

  public List <String> getFullIdentifier ()
  {
    if (declaration == null)
      return Collections.emptyList ();

    final List <String> result = new ArrayList <> ();
    for (final String identifier : identifiers)
    {
      if (identifier.startsWith ("configuration::"))
        result.add (identifier);
      else
        result.add (declaration.getType () + "::" + identifier);
    }
    return result;
  }

  @Override
  public String toString ()
  {
    final String identifier = identifiers.get (0);

    if (identifier.startsWith ("configuration::"))
      return identifier;
    return declaration == null ? "NA" : declaration.getType () + "::" + identifier;
  }
}
