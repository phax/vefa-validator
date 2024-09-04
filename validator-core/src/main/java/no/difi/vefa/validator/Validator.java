package no.difi.vefa.validator;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import no.difi.vefa.validator.api.IProperties;
import no.difi.vefa.validator.api.IValidation;
import no.difi.vefa.validator.api.IValidationSource;
import no.difi.xsd.vefa.validator._1.PackageType;

/**
 * Validator containing an instance of validation configuration and validation artifacts.
 * <p/>
 * Validator is thread safe and should normally be created only once in a program.
 */
@Slf4j
@Singleton
public class Validator implements Closeable {

    /**
     * Current validator instance.
     */
    @Inject
    private ValidatorInstance validatorInstance;

    /**
     * Validate file.
     *
     * @param file File to validate.
     * @return Validation result.
     * @throws IOException
     */
    public IValidation validate(File file) throws IOException {
        return validate(file.toPath());
    }

    /**
     * Validate file.
     *
     * @param file File to validate.
     * @return Validation result.
     * @throws IOException
     */
    public IValidation validate(Path file) throws IOException {
        try (InputStream inputStream = Files.newInputStream(file)) {
            return validate(inputStream);
        }
    }

    /**
     * Validate content of stream.
     *
     * @param inputStream Stream containing content.
     * @return Validation result.
     */
    public IValidation validate(InputStream inputStream) {
        return validate(new ValidationSourceImpl(inputStream));
    }

    /**
     * Validate content of stream.
     *
     * @param inputStream Stream containing content.
     * @param properties  Properties used for individual validation.
     * @return Validation result.
     */
    public IValidation validate(InputStream inputStream, IProperties properties) {
        return validate(new ValidationSourceImpl(inputStream, properties));
    }

    /**
     * Validate content of packaged stream.
     *
     * @param validationSource Package containing source.
     * @return Validation result.
     */
    public IValidation validate(IValidationSource validationSource) {
        return ValidationInstance.of(this.validatorInstance, validationSource);
    }

    /**
     * Validate file from filePath string
     *
     * @param filePath string representing filePath
     * @return Validation result
     * @throws IOException
     */
    public IValidation validate(String filePath) throws IOException {
        return validate(Paths.get(filePath));
    }

    /**
     * List of packages supported by validator.
     *
     * @return List of packages.
     */
    public List<PackageType> getPackages() {
        return this.validatorInstance.getPackages();
    }

    @Override
    public void close() {
        try {
            if (validatorInstance != null)
                validatorInstance.close();
        } catch (IOException e) {
            log.warn("Exception when closing Validator: {}", e.getMessage(), e);
        } finally {
            validatorInstance = null;
        }
    }
}
