package no.difi.vefa.validator.api;

import java.util.List;

import jakarta.xml.bind.annotation.XmlTransient;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.SectionType;

public class Section extends SectionType {

  @XmlTransient
  private final IFlagFilterer flagFilterer;

  /**
   * Initiate section.
   *
   * @param flagFilterer flag filterer
   */
  public Section(final IFlagFilterer flagFilterer) {
    this.flagFilterer = flagFilterer;

    this.setFlag(FlagType.OK);
  }

  /**
   * Add assertion to section using identifier, description and flag.
   *
   * @param identifier Identifier used for matching.
   * @param text Description of identifier.
   * @param flagType Flag associated with identifier.
   */
  public void add(final String identifier, final String text, final String textFriendly, final FlagType flagType) {
    final AssertionType assertionType = new AssertionType();
    assertionType.setIdentifier(identifier);
    assertionType.setText(text);
    assertionType.setTextFriendly(textFriendly);
    assertionType.setFlag(flagType);

    add(assertionType);
  }

  /**
   * Add assertion to section using identifier, description and flag.
   *
   * @param identifier Identifier used for matching.
   * @param text Description of identifier.
   * @param flagType Flag associated with identifier.
   */
  public void add(final String identifier, final String text, final FlagType flagType) {
    final AssertionType assertionType = new AssertionType();
    assertionType.setIdentifier(identifier);
    assertionType.setText(text);
    assertionType.setFlag(flagType);

    add(assertionType);
  }

  public void add(final List<AssertionType> assertions) {
    for (final AssertionType assertion : assertions)
      add(assertion);
  }

  public void add(final AssertionType assertion) {
    flagFilterer.filterFlag(assertion);

    if (assertion.getTextFriendly() == null)
      assertion.setTextFriendly(assertion.getText());
    if (assertion.getLocationFriendly() == null)
      assertion.setLocationFriendly(assertion.getLocation());


    if (getInfoUrl() != null)
      assertion.setInfoUrl(getInfoUrl().replace("{}", assertion.getIdentifier()));

    if (assertion.getFlag() != null) {

      if (assertion.getFlag().compareTo(getFlag()) > 0)
        setFlag(assertion.getFlag());

      this.getAssertion().add(assertion);
    }
  }
}
