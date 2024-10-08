package no.difi.vefa.validator.checker;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IArtifactHolder;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.SaxonErrorListener;
import no.difi.vefa.validator.util.SaxonMessageListener;

/**
 * Implementation performing step 3 (compilation) of Schematron.
 *
 * @author erlend
 */
@Type(".sch")
public class SchematronCheckerFactory implements ICheckerFactory {

    @Inject
    @Named("schematron-step3")
    private Provider<XsltExecutable> schematronCompiler;

    @Inject
    private Processor processor;

    @Inject
    private Injector injector;

    @Override
    public IChecker prepare(IArtifactHolder artifactHolder, String path) throws ValidatorException {
        try (InputStream inputStream = artifactHolder.getInputStream(path)) {
            XdmDestination destination = new XdmDestination();

            XsltTransformer xsltTransformer = schematronCompiler.get().load();
            xsltTransformer.setErrorListener(SaxonErrorListener.INSTANCE);
            xsltTransformer.setMessageListener(SaxonMessageListener.INSTANCE);
            xsltTransformer.setSource(new StreamSource(inputStream));
            xsltTransformer.setDestination(destination);
            xsltTransformer.transform();

            XsltCompiler xsltCompiler = processor.newXsltCompiler();
            xsltCompiler.setErrorListener(SaxonErrorListener.INSTANCE);

            IChecker checker = new SchematronXsltChecker(processor, xsltCompiler.compile(destination.getXdmNode().asSource()));
            injector.injectMembers(checker);
            return checker;
        } catch (Exception e) {
            throw new ValidatorException(e.getMessage(), e);
        }
    }
}
