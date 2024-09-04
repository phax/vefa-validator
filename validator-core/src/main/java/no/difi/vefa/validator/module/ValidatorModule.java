package no.difi.vefa.validator.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.api.IConfigurationProvider;
import no.difi.vefa.validator.api.IDeclaration;
import no.difi.vefa.validator.api.IRendererFactory;
import no.difi.vefa.validator.api.ITrigger;
import no.difi.vefa.validator.checker.SchematronCheckerFactory;
import no.difi.vefa.validator.checker.SchematronXsltCheckerFactory;
import no.difi.vefa.validator.checker.XsdCheckerFactory;
import no.difi.vefa.validator.configuration.AsiceConfigurationProvider;
import no.difi.vefa.validator.configuration.ValidatorTestConfigurationProvider;
import no.difi.vefa.validator.declaration.AsiceDeclaration;
import no.difi.vefa.validator.declaration.AsiceXmlDeclaration;
import no.difi.vefa.validator.declaration.EspdDeclaration;
import no.difi.vefa.validator.declaration.NoblDeclaration;
import no.difi.vefa.validator.declaration.SbdhDeclaration;
import no.difi.vefa.validator.declaration.UblDeclaration;
import no.difi.vefa.validator.declaration.UnCefactDeclaration;
import no.difi.vefa.validator.declaration.ValidatorTestDeclaration;
import no.difi.vefa.validator.declaration.ValidatorTestSetDeclaration;
import no.difi.vefa.validator.declaration.XmlDeclaration;
import no.difi.vefa.validator.declaration.ZipDeclaration;
import no.difi.vefa.validator.renderer.XsltRendererFactory;
import no.difi.vefa.validator.trigger.AsiceTrigger;
import no.difi.xsd.vefa.validator._1.Configurations;

/**
 * @author erlend
 */
public class ValidatorModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new CacheModule());
        install(new PropertiesModule());
        install(new SaxonModule());
        install(new SbdhModule());
        install(new SourceModule());
        install(new SchematronModule());

        Multibinder<ICheckerFactory> checkers = Multibinder.newSetBinder(binder(), ICheckerFactory.class);
        checkers.addBinding().to(SchematronCheckerFactory.class);
        checkers.addBinding().to(SchematronXsltCheckerFactory.class);
        checkers.addBinding().to(XsdCheckerFactory.class);

        Multibinder<IRendererFactory> renderers = Multibinder.newSetBinder(binder(), IRendererFactory.class);
        renderers.addBinding().to(XsltRendererFactory.class);

        Multibinder<ITrigger> triggers = Multibinder.newSetBinder(binder(), ITrigger.class);
        triggers.addBinding().to(AsiceTrigger.class);

        Multibinder<IDeclaration> declarations = Multibinder.newSetBinder(binder(), IDeclaration.class);
        declarations.addBinding().to(AsiceDeclaration.class);
        declarations.addBinding().to(AsiceXmlDeclaration.class);
        declarations.addBinding().to(EspdDeclaration.class);
        declarations.addBinding().to(NoblDeclaration.class);
        declarations.addBinding().to(SbdhDeclaration.class);
        declarations.addBinding().to(UblDeclaration.class);
        declarations.addBinding().to(UnCefactDeclaration.class);
        declarations.addBinding().to(ValidatorTestDeclaration.class);
        declarations.addBinding().to(ValidatorTestSetDeclaration.class);
        declarations.addBinding().to(XmlDeclaration.class);
        declarations.addBinding().to(ZipDeclaration.class);

        Multibinder<IConfigurationProvider> configurations = Multibinder.newSetBinder(binder(), IConfigurationProvider.class);
        configurations.addBinding().to(AsiceConfigurationProvider.class);
        configurations.addBinding().to(ValidatorTestConfigurationProvider.class);
    }

    @Provides
    @Singleton
    public List<ICheckerFactory> getCheckerFactories(Set<ICheckerFactory> factories) {
        return Collections.unmodifiableList(new ArrayList<>(factories));
    }

    @Provides
    @Singleton
    public List<IRendererFactory> getRendererFactories(Set<IRendererFactory> factories) {
        return Collections.unmodifiableList(new ArrayList<>(factories));
    }

    @Provides
    @Singleton
    public List<ITrigger> getTriggers(Set<ITrigger> triggers) {
        return Collections.unmodifiableList(new ArrayList<>(triggers));
    }

    @Provides
    @Singleton
    public List<IDeclaration> getDeclarations(Set<IDeclaration> declarations) {
        return Collections.unmodifiableList(new ArrayList<>(declarations));
    }

    @Provides
    @Singleton
    public List<Configurations> getConfigurations(Set<IConfigurationProvider> providers) {
        return providers.stream()
                .map(IConfigurationProvider::getConfigurations)
                .collect(Collectors.toList());
    }
}
