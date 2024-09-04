package no.difi.vefa.validator.build.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;

import lombok.Getter;
import no.difi.vefa.validator.api.IValidation;
import no.difi.xsd.vefa.validator._1.Configurations;

@Getter
public class Build {

    private Map<String, String> setting = new HashMap<>();

    private Path projectPath;
    private Path[] sourcePath;
    private Path targetFolder;

    private Configurations configurations;

    private List<Path> testFolders = new ArrayList<>();
    private List<IValidation> testValidations = new ArrayList<>();

    public static Build of(String arg, CommandLine cmd) {
        Build build = new Build(Paths.get(arg),
                cmd.getOptionValue("source", ""),
                cmd.getOptionValue("target", cmd.hasOption("profile") ? String.format("target-%s", cmd.getOptionValue("profile")) : "target"));
        build.setSetting("config", cmd.getOptionValue("config", cmd.hasOption("profile") ? String.format("buildconfig-%s.xml", cmd.getOptionValue("profile")) : "buildconfig.xml"));
        build.setSetting("name", cmd.getOptionValue("name", "rules"));
        build.setSetting("build", cmd.getOptionValue("build", UUID.randomUUID().toString()));
        build.setSetting("weight", cmd.getOptionValue("weight", "0"));

        return build;
    }

    public Build(Path projectPath) {
        this(projectPath, "", "target");
    }

    public Build(Path projectPath, String sourceFolder, String targetFolder) {
        this.projectPath = projectPath;

        String[] sfs = sourceFolder.split(",");
        this.sourcePath = new Path[sfs.length];
        for (int i = 0; i < sfs.length; i++)
            sourcePath[i] = projectPath.resolve(sfs[i]);

        this.targetFolder = projectPath.resolve(targetFolder);
    }

    public Build(Path projectPath, Path[] sourceFolder, Path targetFolder) {
        this.projectPath = projectPath;
        this.sourcePath = sourceFolder;
        this.targetFolder = targetFolder;
    }

    public Configurations getConfigurations() {
        if (configurations == null) {
            configurations = new Configurations();
            configurations.setName(getSetting("name"));
            configurations.setTimestamp(System.currentTimeMillis());
        }

        return configurations;
    }

    public void setSetting(String key, String value) {
        setting.put(key, value);
    }

    public String getSetting(String key) {
        return setting.get(key);
    }

    public void addTestFolder(File testFolder) {
        addTestFolder(testFolder.toPath());
    }

    public void addTestFolder(Path testFolder) {
        testFolders.add(testFolder);
    }

    public void addTestValidation(IValidation validation) {
        testValidations.add(validation);
    }

}
