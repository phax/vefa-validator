package no.difi.vefa.validator;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.io.resource.IReadableResource;

import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IValidation;
import no.difi.vefa.validator.api.IValidationSource;
import no.difi.xsd.vefa.validator._1.PackageType;

/**
 * Validator containing an instance of validation configuration and validation artifacts.
 * <p>
 * Validator is thread safe and should normally be created only once in a program.
 */
public class Validator implements Closeable
{
  private static final Logger log = LoggerFactory.getLogger (Validator.class);

  /**
   * Current validator instance.
   */
  private ValidatorInstance validatorInstance;

  Validator (final ValidatorInstance validatorInstance)
  {
    this.validatorInstance = validatorInstance;
  }

  /**
   * Validate file.
   *
   * @param file
   *        File to validate.
   * @return Validation result.
   * @throws IOException
   */
  public IValidation validate (@NonNull final File file) throws IOException
  {
    return validate (file.toPath ());
  }

  /**
   * Validate file.
   *
   * @param file
   *        File to validate.
   * @return Validation result.
   * @throws IOException
   */
  public IValidation validate (final Path file) throws IOException
  {
    try (final InputStream inputStream = Files.newInputStream (file))
    {
      return _validate (inputStream, null);
    }
  }

  public IValidation validate (final IReadableResource aRes) throws IOException
  {
    try (final InputStream inputStream = aRes.getInputStream ())
    {
      return _validate (inputStream, null);
    }
  }

  /**
   * Validate content of stream.
   *
   * @param inputStream
   *        Stream containing content.
   * @return Validation result.
   */
  public IValidation validate (final InputStream inputStream)
  {
    return _validate (inputStream, null);
  }

  public IValidation validate (final IReadableResource aRes, final IProperties properties) throws IOException
  {
    try (final InputStream inputStream = aRes.getInputStream ())
    {
      return _validate (inputStream, properties);
    }
  }

  /**
   * Validate content of stream.
   *
   * @param inputStream
   *        Stream containing content.
   * @param properties
   *        Properties used for individual validation.
   * @return Validation result.
   */
  private IValidation _validate (final InputStream inputStream, final IProperties properties)
  {
    return validate (new ValidationSourceImpl (inputStream, properties));
  }

  /**
   * Validate content of packaged stream.
   *
   * @param validationSource
   *        Package containing source.
   * @return Validation result.
   */
  public IValidation validate (final IValidationSource validationSource)
  {
    return ValidationInstance.of (this.validatorInstance, validationSource);
  }

  /**
   * Validate file from filePath string
   *
   * @param filePath
   *        string representing filePath
   * @return Validation result
   * @throws IOException
   */
  public IValidation validate (final String filePath) throws IOException
  {
    return validate (Paths.get (filePath));
  }

  /**
   * List of packages supported by validator.
   *
   * @return List of packages.
   */
  public List <PackageType> getPackages ()
  {
    return this.validatorInstance.getPackages ();
  }

  @Override
  public void close ()
  {
    try
    {
      if (validatorInstance != null)
        validatorInstance.close ();
    }
    catch (final IOException e)
    {
      log.warn ("Exception when closing Validator: {}", e.getMessage (), e);
    }
    finally
    {
      validatorInstance = null;
    }
  }
}
