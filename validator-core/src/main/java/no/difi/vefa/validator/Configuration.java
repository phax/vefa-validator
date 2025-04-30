package no.difi.vefa.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.difi.vefa.validator.api.IFlagFilter;
import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.ConfigurationType;
import no.difi.xsd.vefa.validator._1.FileType;
import no.difi.xsd.vefa.validator._1.FlagType;
import no.difi.xsd.vefa.validator._1.RuleActionType;
import no.difi.xsd.vefa.validator._1.RuleType;
import no.difi.xsd.vefa.validator._1.StylesheetType;
import no.difi.xsd.vefa.validator._1.TriggerType;

/**
 * Configurations found in validation artifacts are updated to this kind of object.
 */
class Configuration extends ConfigurationType implements IFlagFilter
{

  private final Map <String, RuleActionType> m_aRuleActions = new HashMap <> ();

  /**
   * List of resources not found during normalization of object.
   */
  private final List <String> notLoaded = new ArrayList <> ();

  /**
   * Create new configuration based on configuration from XML.
   *
   * @param configurationType
   *        Configuration from XML.
   */
  Configuration (final ConfigurationType configurationType)
  {
    // Copy rule
    this.setIdentifier (configurationType.getIdentifier ());
    this.setTitle (configurationType.getTitle ());
    this.setStandardId (configurationType.getStandardId ());
    this.setCustomizationId (configurationType.getCustomizationId ());
    this.setProfileId (configurationType.getProfileId ());
    this.setWeight (configurationType.getWeight ());
    this.setBuild (configurationType.getBuild ());
    this.inherit = configurationType.getInherit ();
    this.rule = configurationType.getRule ();
    this.file = configurationType.getFile ();
    this.trigger = configurationType.getTrigger ();
  }

  /**
   * Validation artifacts supports inheritance. This methods uses inheritance references to generate
   * a complete object containing all resources for a given document type.
   *
   * @param engine
   *        ValidatiorEngine for fetching of other configurations.
   */
  void normalize (final ValidatorEngine engine)
  {
    while (getInherit ().size () > 0)
    {
      final List <RuleType> rules = new ArrayList <> ();
      final List <FileType> files = new ArrayList <> ();
      final List <TriggerType> triggers = new ArrayList <> ();
      final List <String> inherits = new ArrayList <> ();
      StylesheetType stylesheet = null;

      for (final String inherit : getInherit ())
      {
        final ConfigurationType inherited = engine.getConfiguration (inherit);
        if (inherited != null)
        {
          rules.addAll (inherited.getRule ());
          files.addAll (inherited.getFile ());
          triggers.addAll (inherited.getTrigger ());
          inherits.addAll (inherited.getInherit ());
          if (inherited.getStylesheet () != null)
            stylesheet = inherited.getStylesheet ();
        }
        else
        {
          notLoaded.add (inherit);
        }
      }

      rules.addAll (this.getRule ());
      files.addAll (this.getFile ());
      triggers.addAll (this.getTrigger ());

      this.rule = rules;
      this.file = files;
      this.trigger = triggers;
      this.inherit = inherits;

      if (getStylesheet () == null)
        setStylesheet (stylesheet);
    }

    for (final RuleType ruleType : this.getRule ())
      m_aRuleActions.put (ruleType.getIdentifier (), ruleType.getAction ());
  }

  /**
   * Get list of resources not found when normalizing object.
   *
   * @return List of identifiers.
   */
  List <String> getNotLoaded ()
  {
    return notLoaded;
  }

  public void filterFlag (final AssertionType assertionType)
  {
    if (m_aRuleActions.containsKey (assertionType.getIdentifier ()))
    {
      switch (m_aRuleActions.get (assertionType.getIdentifier ()))
      {
        case SET_FUTURE_ERROR:
          assertionType.setFlag (FlagType.FUTURE_ERROR);
          break;
        case SET_ERROR:
          assertionType.setFlag (FlagType.ERROR);
          break;
        case SET_WARNING:
          assertionType.setFlag (FlagType.WARNING);
          break;
        case SET_FATAL:
          assertionType.setFlag (FlagType.FATAL);
          break;
        case SUPPRESS:
          assertionType.setFlag (null);
          break;
      }
    }
  }
}
