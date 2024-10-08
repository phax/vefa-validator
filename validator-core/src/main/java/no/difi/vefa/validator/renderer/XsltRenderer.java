package no.difi.vefa.validator.renderer;

import java.io.OutputStream;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import no.difi.vefa.validator.api.IArtifactHolder;
import no.difi.vefa.validator.api.Document;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IRenderer;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.HolderURIResolver;
import no.difi.xsd.vefa.validator._1.SettingType;
import no.difi.xsd.vefa.validator._1.StylesheetType;

/**
 * Defines presenter for templates defined by XSLT.
 */
@Deprecated
public class XsltRenderer implements IRenderer {

    private XsltExecutable xsltExecutable;

    /**
     * Holds the stylesheet definition.
     */
    private StylesheetType stylesheetType;

    private IArtifactHolder artifactHolder;

    private String path;

    private Processor processor;

    public XsltRenderer(XsltExecutable xsltExecutable, StylesheetType stylesheetType, IArtifactHolder artifactHolder, String path, Processor processor) {
        this.xsltExecutable = xsltExecutable;
        this.artifactHolder = artifactHolder;
        this.stylesheetType = stylesheetType;
        this.path = path;
        this.processor = processor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(Document document, IProperties properties, OutputStream outputStream) throws ValidatorException {
        try {
            XsltTransformer xsltTransformer = xsltExecutable.load();
            xsltTransformer.setURIResolver(new HolderURIResolver(artifactHolder, path));

            // Look through default values for stylesheet.
            for (SettingType setting : stylesheetType.getSetting())
                setParameter(
                        xsltTransformer,
                        setting.getName(),
                        properties.getString(String.format("stylesheet.%s.%s",
                                stylesheetType.getIdentifier(), setting.getName()), setting.getDefaultValue()));

            // Use transformer to write the result to stream.
            xsltTransformer.setSource(new StreamSource(document.getInputStream()));
            xsltTransformer.setDestination(processor.newSerializer(outputStream));
            xsltTransformer.transform();
            xsltTransformer.close();
        } catch (Exception e) {
            throw new ValidatorException("Unable to render document.", e);
        }
    }

    private static void setParameter(XsltTransformer transformer, String key, String value) {
        transformer.setParameter(new QName(key), new XdmAtomicValue(value));
    }
}
