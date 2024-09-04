package no.difi.vefa.validator.build.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.helger.asic.AsicWriterFactory;
import com.helger.asic.ESignatureMethod;
import com.helger.asic.IAsicWriter;
import com.helger.asic.SignatureHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.security.keystore.EKeyStoreType;

/**
 * @author erlend
 */
public class AsicArchiver extends SimpleFileVisitor <Path>
{

  private static final AsicWriterFactory ASIC_WRITER_FACTORY = AsicWriterFactory.newFactory (ESignatureMethod.CAdES);

  private static final SignatureHelper SIGNATURE_HELPER = new SignatureHelper (EKeyStoreType.JKS,
                                                                               "/keystore-self-signed.jks",
                                                                               "changeit",
                                                                               "self-signed",
                                                                               "changeit");

  private final IAsicWriter asicWriter;

  private final Path directory;

  public static void archive (final Path target, final Path directory) throws IOException
  {
    final IAsicWriter asicWriter = ASIC_WRITER_FACTORY.newContainer (target);
    Files.walkFileTree (directory.toAbsolutePath (), new AsicArchiver (asicWriter, directory.toAbsolutePath ()));
    asicWriter.sign (SIGNATURE_HELPER);
  }

  public AsicArchiver (final IAsicWriter asicWriter, final Path directory)
  {
    this.asicWriter = asicWriter;
    this.directory = directory;
  }

  @Override
  public FileVisitResult visitFile (final Path file, final BasicFileAttributes attrs) throws IOException
  {
    final String path = file.toString ().substring (directory.toString ().length () + 1).replace ("\\", "/");
    asicWriter.add (file, path, CMimeType.APPLICATION_XML);
    return super.visitFile (file, attrs);
  }
}
