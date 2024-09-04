package no.difi.vefa.validator.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.ITrigger;
import no.difi.vefa.validator.lang.ValidatorException;

@Slf4j
@Singleton
public class TriggerFactory {

    private final Map<String, ITrigger> triggers = new HashMap<>();

    @Inject
    public TriggerFactory(List<ITrigger> triggers) {
        for (ITrigger trigger : triggers) {
            for (String type : trigger.getClass().getAnnotation(Type.class).value())
                this.triggers.put(type, trigger);
        }
    }

    public ITrigger get(String identifier) throws ValidatorException {
        if (triggers.containsKey(identifier))
            return triggers.get(identifier);

        throw new ValidatorException(String.format("Trigger '%s' not found.", identifier));
    }
}
