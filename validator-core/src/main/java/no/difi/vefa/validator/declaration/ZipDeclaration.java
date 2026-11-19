package no.difi.vefa.validator.declaration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.GuardedBy;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.concurrent.SimpleReadWriteLock;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.stream.StreamHelper;
import com.helger.base.state.EContinue;
import com.helger.io.zip.ZipWalkResult;
import com.helger.io.zip.ZipWalker;
import com.helger.io.zip.ZipWalkerSettings;

import no.difi.vefa.validator.annotation.Type;
import no.difi.vefa.validator.api.CachedFile;
import no.difi.vefa.validator.api.IDeclarationWithChildren;
import no.difi.vefa.validator.api.IExpectation;
import no.difi.vefa.validator.lang.VefaValidatorException;
import no.difi.vefa.validator.util.StreamUtils;

@Type ("zip")
public class ZipDeclaration implements IDeclarationWithChildren
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ZipDeclaration.class);
  private static final byte [] STARTS_WITH = { 0x50, 0x4B, 0x03, 0x04 };
  /** The default maximum number of entries of a ZIP archive */
  public static final int DEFAULT_MAX_ENTRY_COUNT = 10_000;
  /** The default maximum total uncompressed size of a ZIP archive: 100 MB */
  public static final long DEFAULT_MAX_TOTAL_UNCOMPRESSED_BYTES = 100L * 1024 * 1024;

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static ZipWalkerSettings s_aSettings = new ZipWalkerSettings ().setMaxNestingDepth (0)
                                                                         .setMaxEntryCount (DEFAULT_MAX_ENTRY_COUNT)
                                                                         .setMaxTotalUncompressedBytes (DEFAULT_MAX_TOTAL_UNCOMPRESSED_BYTES);

  @Override
  public boolean verify (final byte [] content, final List <String> parent)
  {
    return Arrays.equals (STARTS_WITH, Arrays.copyOfRange (content, 0, STARTS_WITH.length));
  }

  @Override
  public List <String> detect (final InputStream contentStream, final List <String> parent)
  {
    try
    {
      final byte [] content = StreamUtils.read50KAndReset (contentStream);
      try (final ZipInputStream zipInputStream = new ZipInputStream (new NonBlockingByteArrayInputStream (content)))
      {
        final ZipEntry entry = zipInputStream.getNextEntry ();

        if ("mimetype".equals (entry.getName ()))
        {
          return Collections.singletonList (StreamHelper.getAllBytesAsString (zipInputStream,
                                                                              StandardCharsets.ISO_8859_1));
        }
      }
    }
    catch (final IOException e)
    {
      LOGGER.error ("Error reading ZIP as ZIP", e);
    }

    return Collections.singletonList ("application/zip");
  }

  @Override
  public IExpectation expectations (final byte [] content)
  {
    return null;
  }

  /**
   * @return A copy of the limits applied when reading the entries of a ZIP archive. Never
   *         <code>null</code>.
   */
  @NonNull
  @ReturnsMutableCopy
  public static ZipWalkerSettings getSettings ()
  {
    return RW_LOCK.readLockedGet (s_aSettings::getClone);
  }

  /**
   * Set the limits applied when reading the entries of a ZIP archive. All entries are kept in memory,
   * so the total uncompressed size should be limited.
   *
   * @param aSettings
   *        The settings to use. They are copied. May not be <code>null</code>.
   */
  public static void setSettings (@NonNull final ZipWalkerSettings aSettings)
  {
    ValueEnforcer.notNull (aSettings, "Settings");
    final ZipWalkerSettings aCopy = aSettings.getClone ();
    RW_LOCK.writeLocked ( () -> s_aSettings = aCopy);
  }

  @Override
  public Iterable <CachedFile> children (final InputStream inputStream) throws VefaValidatorException
  {
    // Nested ZIP archives are returned as entries and validated on the next nesting level
    final ZipWalkerSettings aSettings = getSettings ();
    final List <CachedFile> ret = new ArrayList <> ();
    final ZipWalkResult aResult = ZipWalker.walk (inputStream, aSettings, (sPath, aEntry, nDepth, aIS) -> {
      ret.add (CachedFile.of (sPath, aIS.readAllBytes ()));
      return EContinue.CONTINUE;
    });

    switch (aResult.getResult ())
    {
      case SUCCESS:
        return ret;
      case MAX_ENTRY_COUNT_EXCEEDED:
        throw new VefaValidatorException ("The ZIP archive contains more than " +
                                          aSettings.getMaxEntryCount () +
                                          " entries");
      case MAX_TOTAL_UNCOMPRESSED_BYTES_EXCEEDED:
        throw new VefaValidatorException ("The total uncompressed size of the ZIP archive exceeds " +
                                          aSettings.getMaxTotalUncompressedBytes () +
                                          " bytes");
      case NOT_A_ZIP:
        throw new VefaValidatorException ("The content is not a ZIP archive");
      default:
        throw new VefaValidatorException ("Failed to read the ZIP archive" +
                                          (aResult.getEntryPath () == null ? "" : " at entry '" +
                                                                                  aResult.getEntryPath () +
                                                                                  "'"),
                                          aResult.getException ());
    }
  }
}
