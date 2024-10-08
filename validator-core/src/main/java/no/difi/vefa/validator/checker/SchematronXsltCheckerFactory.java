package no.difi.vefa.validator.checker;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import com.google.inject.Inject;
import com.google.inject.Injector;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltCompiler;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IArtifactHolder;
import no.difi.vefa.validator.api.IChecker;
import no.difi.vefa.validator.api.ICheckerFactory;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.SaxonErrorListener;

/**
 * @author erlend
 */
@Type({".xsl", ".xslt", ".svrl.xsl", ".svrl.xslt", ".sch.xslt"})
public class SchematronXsltCheckerFactory implements ICheckerFactory {

    @Inject
    private Processor processor;

    @Inject
    private Injector injector;

    @Override
    public IChecker prepare(IArtifactHolder artifactHolder, String path) throws ValidatorException {
        try (InputStream inputStream = artifactHolder.getInputStream(path)) {
            XsltCompiler xsltCompiler = processor.newXsltCompiler();
            xsltCompiler.setErrorListener(SaxonErrorListener.INSTANCE);

            IChecker checker = new SchematronXsltChecker(processor, xsltCompiler.compile(new StreamSource(inputStream)));
            injector.injectMembers(checker);
            return checker;

        } catch (Exception e) {
            throw new ValidatorException(e.getMessage(), e);
        }
    }
}
