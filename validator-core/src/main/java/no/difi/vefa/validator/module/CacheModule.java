package no.difi.vefa.validator.module;

import java.time.Duration;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import no.difi.vefa.validator.CheckerCacheLoader;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.IProperties;

/**
 * @author erlend
 */
public class CacheModule extends AbstractModule
{
  @Provides
  @Singleton
  public LoadingCache <String, IChecker> getCheckerCache (final IProperties properties, final CheckerCacheLoader loader)
  {
    return CacheBuilder.newBuilder ()
                       .softValues ()
                       .maximumSize (properties.getInteger ("pools.checker.size"))
                       .expireAfterAccess (Duration.ofMinutes (properties.getInteger ("pools.checker.expire")))
                       .build (loader);
  }
}
