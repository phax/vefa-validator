package no.difi.vefa.validator.module;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import no.difi.vefa.validator.CheckerCacheLoader;
import no.difi.vefa.validator.RendererCacheLoader;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IRenderer;

/**
 * @author erlend
 */
public class CacheModule extends AbstractModule {

    @Provides
    @Singleton
    public LoadingCache<String, IChecker> getCheckerCache(IProperties properties, CheckerCacheLoader loader) {
        return CacheBuilder.newBuilder()
                .softValues()
                .maximumSize(properties.getInteger("pools.checker.size"))
                .expireAfterAccess(properties.getInteger("pools.checker.expire"), TimeUnit.MINUTES)
                .build(loader);
    }

    @Provides
    @Singleton
    public LoadingCache<String, IRenderer> getRendererCache(IProperties properties, RendererCacheLoader loader) {
        return CacheBuilder.newBuilder()
                .softValues()
                .maximumSize(properties.getInteger("pools.presenter.size"))
                .expireAfterAccess(properties.getInteger("pools.presenter.expire"), TimeUnit.MINUTES)
                .build(loader);
    }
}
