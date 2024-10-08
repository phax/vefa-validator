package no.difi.vefa.validator;

import java.util.concurrent.TimeUnit;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.properties.SimpleProperties;

/**
 * Class to hold defaults in validator.
 */
public class ValidatorDefaults {

    /**
     * Default configuration.
     */
    public static final IProperties PROPERTIES = new SimpleProperties()

            // feature
            .set("feature.expectation", false)
            .set("feature.nesting", false)
            .set("feature.suppress_notloaded", false)
            .set("feature.infourl", false)

            // pools.checker
            .set("pools.checker.size", CheckerCacheLoader.DEFAULT_SIZE)
            .set("pools.checker.expire", TimeUnit.DAYS.toMinutes(1))

            // pools.presenter
            .set("pools.presenter.size", RendererCacheLoader.DEFAULT_SIZE)
            .set("pools.presenter.expire", TimeUnit.DAYS.toMinutes(1))

            // finish
            ;

}
