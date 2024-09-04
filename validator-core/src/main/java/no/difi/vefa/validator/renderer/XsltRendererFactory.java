package no.difi.vefa.validator.renderer;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import com.google.inject.Inject;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltCompiler;
import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IArtifactHolder;
import no.difi.vefa.validator.api.IRenderer;
import no.difi.vefa.validator.api.IRendererFactory;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.HolderURIResolver;
import no.difi.vefa.validator.util.SaxonErrorListener;
import no.difi.xsd.vefa.validator._1.StylesheetType;

/**
 * @author erlend
 */
@Deprecated
@Type({".xsl", ".xslt"})
public class XsltRendererFactory implements IRendererFactory {

    @Inject
    private Processor processor;

    @Override
    public IRenderer prepare(StylesheetType stylesheetType, IArtifactHolder artifactHolder, String path) throws ValidatorException {
        try (InputStream inputStream = artifactHolder.getInputStream(path)) {
            XsltCompiler xsltCompiler = processor.newXsltCompiler();
            xsltCompiler.setErrorListener(SaxonErrorListener.INSTANCE);
            xsltCompiler.setURIResolver(new HolderURIResolver(artifactHolder, path));
            return new XsltRenderer(xsltCompiler.compile(new StreamSource(inputStream)),
                    stylesheetType, artifactHolder, path, processor);
        } catch (Exception e) {
            throw new ValidatorException(e.getMessage(), e);
        }
    }
}
