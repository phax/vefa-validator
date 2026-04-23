package no.difi.vefa.validator.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.ITrigger;
import no.difi.vefa.validator.lang.VefaValidatorException;

@Singleton
public class TriggerFactory
{
  private final Map <String, ITrigger> m_aTriggers = new HashMap <> ();

  @Inject
  public TriggerFactory (final List <ITrigger> triggers)
  {
    for (final ITrigger trigger : triggers)
      for (final String type : trigger.getClass ().getAnnotation (Type.class).value ())
        m_aTriggers.put (type, trigger);
  }

  public ITrigger get (final String identifier) throws VefaValidatorException
  {
    if (m_aTriggers.containsKey (identifier))
      return m_aTriggers.get (identifier);

    throw new VefaValidatorException ("Trigger '" + identifier + "' not found.");
  }
}
