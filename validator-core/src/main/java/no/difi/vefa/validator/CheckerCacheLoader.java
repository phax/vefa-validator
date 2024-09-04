package no.difi.vefa.validator;

import java.util.List;

import com.google.common.cache.CacheLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.lang.ValidatorException;

/**
 * @author erlend
 */
@Slf4j
@Singleton
public class CheckerCacheLoader extends CacheLoader<String, IChecker> {

    public static final int DEFAULT_SIZE = 250;

    @Inject
    private List<ICheckerFactory> factories;

    @Inject
    private ValidatorEngine validatorEngine;

    @Override
    public IChecker load(String key) throws Exception {
        try {
            for (ICheckerFactory factory : factories) {
                for (String extension : factory.getClass().getAnnotation(Type.class).value()) {
                    if (key.toLowerCase().endsWith(extension)) {
                        return factory.prepare(validatorEngine.getResource(key), key.split("#")[1]);
                    }
                }
            }
        } catch (Exception e) {
            throw new ValidatorException(String.format("Unable to load checker for '%s'.", key), e);
        }

        throw new ValidatorException(String.format("No checker found for '%s'", key));
    }
}
