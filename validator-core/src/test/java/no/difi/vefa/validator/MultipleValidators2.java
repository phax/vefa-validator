package no.difi.vefa.validator;

import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import no.difi.vefa.validator.source.ClasspathSource;

/**
 * Using multiple validators at the same time.
 */
public class MultipleValidators2 {

    @Test
    public void simple() {
        Validator validator1 = ValidatorBuilder.newValidator()
                .setSource(new ClasspathSource("/rules/"))
                .build();
        Validator validator2 = ValidatorBuilder.newValidator()
                .setSource(new ClasspathSource("/rules/"))
                .build();

        assertNotSame(validator1, validator2);

        validator1.close();
        validator2.close();
    }
}
