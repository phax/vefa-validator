package no.difi.vefa.validator.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.ITrigger;
import no.difi.vefa.validator.lang.ValidatorException;

@Singleton
public class TriggerFactory
{
  private final Map <String, ITrigger> triggers = new HashMap <> ();

  @Inject
  public TriggerFactory (final List <ITrigger> triggers)
  {
    for (final ITrigger trigger : triggers)
    {
      for (final String type : trigger.getClass ().getAnnotation (Type.class).value ())
        this.triggers.put (type, trigger);
    }
  }

  public ITrigger get (final String identifier) throws ValidatorException
  {
    if (triggers.containsKey (identifier))
      return triggers.get (identifier);

    throw new ValidatorException (String.format ("Trigger '%s' not found.", identifier));
  }
}
