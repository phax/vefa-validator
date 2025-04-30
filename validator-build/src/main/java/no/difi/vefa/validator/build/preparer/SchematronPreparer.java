package no.difi.vefa.validator.build.preparer;

import java.io.IOException;
import java.nio.file.Path;

import org.w3c.dom.Document;

import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.resource.FileSystemResource;
import com.helger.schematron.CSchematron;
import com.helger.schematron.pure.binding.IPSQueryBinding;
import com.helger.schematron.pure.binding.PSQueryBindingRegistry;
import com.helger.schematron.pure.exchange.PSReader;
import com.helger.schematron.pure.model.PSSchema;
import com.helger.schematron.pure.preprocess.PSPreprocessor;
import com.helger.schematron.pure.preprocess.SchematronPreprocessException;
import com.helger.schematron.sch.SchematronProviderXSLTFromSCH;
import com.helger.schematron.sch.TransformerCustomizerSCH;
import com.helger.schematron.svrl.CSVRL;
import com.helger.xml.microdom.serialize.MicroWriter;
import com.helger.xml.namespace.MapBasedNamespaceContext;
import com.helger.xml.serialize.write.EXMLSerializeIndent;
import com.helger.xml.serialize.write.IXMLWriterSettings;
import com.helger.xml.serialize.write.XMLWriter;
import com.helger.xml.serialize.write.XMLWriterSettings;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.IPreparer;

@Type ({ ".sch", ".scmt" })
public class SchematronPreparer implements IPreparer
{
  @Override
  public void prepare (final Path source, final Path target, final EPreparerType type) throws IOException
  {
    try
    {
      final MapBasedNamespaceContext aNSCtx = new MapBasedNamespaceContext ();
      aNSCtx.addDefaultNamespaceURI (CSchematron.NAMESPACE_SCHEMATRON);
      aNSCtx.addMapping ("xsl", "http://www.w3.org/1999/XSL/Transform");
      aNSCtx.addMapping ("svrl", CSVRL.SVRL_NAMESPACE_URI);
      final IXMLWriterSettings aXWS = new XMLWriterSettings ().setIndent (EXMLSerializeIndent.INDENT_AND_ALIGN)
                                                              .setNamespaceContext (aNSCtx);

      if (target.toString ().endsWith (".sch"))
      {
        // Read Schematron
        final PSSchema aSchema = new PSReader (new FileSystemResource (source)).readSchema ();
        final IPSQueryBinding aQueryBinding = PSQueryBindingRegistry.getQueryBindingOfNameOrThrow (aSchema.getQueryBinding ());
        final PSPreprocessor aPreprocessor = PSPreprocessor.createPreprocessorWithoutInformationLoss (aQueryBinding);
        // Pre-process
        final PSSchema aPreprocessedSchema = aPreprocessor.getAsPreprocessedSchema (aSchema);
        if (aPreprocessedSchema == null)
          throw new SchematronPreprocessException ("Failed to preprocess schema " +
                                                   aSchema +
                                                   " with query binding " +
                                                   aQueryBinding);
        // Convert to XML string
        MicroWriter.writeToFile (aPreprocessedSchema.getAsMicroElement (), target.toFile (), aXWS);

        // schematronPrepare.get ().compile (source, target);
      }
      else
      {
        final Document aDoc = SchematronProviderXSLTFromSCH.createSchematronXSLT (new FileSystemResource (source),
                                                                                  new TransformerCustomizerSCH ());
        XMLWriter.writeToStream (aDoc, FileHelper.getOutputStream (target.toFile ()), aXWS);
        // schematronCompile.get ().compile (source, target);
      }
    }
    catch (final Exception e)
    {
      throw new IOException ("Unable to handle Schematron.", e);
    }
  }
}
