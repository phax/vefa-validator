package no.difi.vefa.validator;

import java.util.concurrent.TimeUnit;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.properties.SimpleProperties;

/**
 * Class to hold defaults in validator.
 */
public final class ValidatorDefaults
{
  /**
   * Default configuration.
   */
  public static final IProperties PROPERTIES;
  static
  {
    PROPERTIES = new SimpleProperties ()
                                        // feature
                                        .set ("feature.expectation", Boolean.FALSE)
                                        .set ("feature.nesting", Boolean.FALSE)
                                        .set ("feature.suppress_notloaded", Boolean.FALSE)
                                        .set ("feature.infourl", Boolean.FALSE)

                                        // pools.checker
                                        .set ("pools.checker.size", Integer.valueOf (CheckerCacheLoader.DEFAULT_SIZE))
                                        .set ("pools.checker.expire", Long.valueOf (TimeUnit.DAYS.toMinutes (1)))
    // finish
    ;
  }
}
