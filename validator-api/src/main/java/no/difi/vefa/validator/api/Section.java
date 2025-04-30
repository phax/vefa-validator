package no.difi.vefa.validator.api;

import java.util.List;

import jakarta.xml.bind.annotation.XmlTransient;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.SectionType;

public class Section extends SectionType
{

  @XmlTransient
  private final IFlagFilter m_aFlagFilter;

  /**
   * Initiate section.
   *
   * @param flagFilter
   *        flag filterer
   */
  public Section (final IFlagFilter flagFilter)
  {
    m_aFlagFilter = flagFilter;

    this.setFlag (FlagType.OK);
  }

  /**
   * Add assertion to section using identifier, description and flag.
   *
   * @param identifier
   *        Identifier used for matching.
   * @param text
   *        Description of identifier.
   * @param flagType
   *        Flag associated with identifier.
   */
  public void add (final String identifier, final String text, final String textFriendly, final FlagType flagType)
  {
    final AssertionType assertionType = new AssertionType ();
    assertionType.setIdentifier (identifier);
    assertionType.setText (text);
    assertionType.setTextFriendly (textFriendly);
    assertionType.setFlag (flagType);

    add (assertionType);
  }

  /**
   * Add assertion to section using identifier, description and flag.
   *
   * @param identifier
   *        Identifier used for matching.
   * @param text
   *        Description of identifier.
   * @param flagType
   *        Flag associated with identifier.
   */
  public void add (final String identifier, final String text, final FlagType flagType)
  {
    final AssertionType assertionType = new AssertionType ();
    assertionType.setIdentifier (identifier);
    assertionType.setText (text);
    assertionType.setFlag (flagType);

    add (assertionType);
  }

  public void add (final List <AssertionType> assertions)
  {
    for (final AssertionType a : assertions)
      add (a);
  }

  public void add (final AssertionType aAssertion)
  {
    m_aFlagFilter.filterFlag (aAssertion);

    if (aAssertion.getTextFriendly () == null)
      aAssertion.setTextFriendly (aAssertion.getText ());
    if (aAssertion.getLocationFriendly () == null)
      aAssertion.setLocationFriendly (aAssertion.getLocation ());

    if (getInfoUrl () != null)
      aAssertion.setInfoUrl (getInfoUrl ().replace ("{}", aAssertion.getIdentifier ()));

    if (aAssertion.getFlag () != null)
    {

      if (aAssertion.getFlag ().compareTo (getFlag ()) > 0)
        setFlag (aAssertion.getFlag ());

      this.getAssertion ().add (aAssertion);
    }
  }
}
