package no.difi.vefa.validator.build.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import no.difi.vefa.validator.api.IPreparer;
import no.difi.vefa.validator.build.preparer.DefaultPreparer;
import no.difi.vefa.validator.build.preparer.SchematronPreparer;

/**
 * @author erlend
 */
public class BuildModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<IPreparer> preparers = Multibinder.newSetBinder(binder(), IPreparer.class);
        preparers.addBinding().to(DefaultPreparer.class);
        preparers.addBinding().to(SchematronPreparer.class);
    }

    @Provides
    @Singleton
    public List<IPreparer> getPreparers(Set<IPreparer> preparers) {
        return Collections.unmodifiableList(new ArrayList<>(preparers));
    }
}
