package no.difi.vefa.validator.build.task;

import com.google.inject.Singleton;

import no.difi.vefa.validator.api.IValidation;
import no.difi.vefa.validator.build.model.Build;
import no.difi.vefa.validator.tester.Tester;
import no.difi.xsd.vefa.validator._1.FlagType;

/**
 * @author erlend
 */
@Singleton
public class TestTask {

    public boolean perform(Build build) {
        for (IValidation validation : Tester.perform(build.getTargetFolder(), build.getTestFolders()))
            build.addTestValidation(validation);

        for (IValidation validation : build.getTestValidations())
            if (validation.getReport().getFlag().compareTo(FlagType.EXPECTED) > 0)
                return false;

        return true;
    }
}
