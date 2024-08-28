package com.ericsson.cifwk.taf.executor;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static com.ericsson.cifwk.taf.executor.TafScheduleBuilder.TE_LOGS_DIR;
import static org.assertj.core.api.Assertions.assertThat;

public class TafScheduleBuilderTest {

    private TafScheduleBuilder unit;

    @Before
    public void setUp() {
        unit = new TafScheduleBuilder();
    }

    @Test
    public void stringToProperties() throws Exception {
        Properties properties = new Properties();
        properties.put("c", "3");
        properties.put("b", "2");
        properties.put("a", "1");
        Gson gson = new Gson();
        String propsAsString = gson.toJson(properties);
        Assert.assertEquals(properties, unit.stringToProperties(propsAsString));
        Assert.assertEquals(new Properties(), unit.stringToProperties(null));
    }

    @Test
    public void shouldPackFewFiles() throws Exception {
        Path rootDir = Files.createTempDirectory("TafScheduleBuilderTestPack");
        Path unpackResults = Files.createTempDirectory("TafScheduleBuilderTestUnpackResults");
        try {
            Path consoleLogsDirPath = rootDir.resolve(TE_LOGS_DIR);
            consoleLogsDirPath.toFile().mkdir();
            consoleLogsDirPath.resolve("file1.txt").toFile().createNewFile();
            consoleLogsDirPath.resolve("file2.txt").toFile().createNewFile();
            Path envProperties = rootDir.resolve("allure-environment.properties");
            envProperties.toFile().createNewFile();

            File zip = TafScheduleBuilder.pack(consoleLogsDirPath, envProperties);
            ZipUtil.unpack(zip, unpackResults.toFile());

            String[] unpackedFiles = unpackResults.toFile().list();
            assertThat(unpackedFiles).containsExactlyInAnyOrder(TE_LOGS_DIR, "allure-environment.properties");
            String[] unpackedTeLogs = unpackResults.resolve(TE_LOGS_DIR).toFile().list();
            assertThat(unpackedTeLogs).containsExactlyInAnyOrder("file1.txt", "file2.txt");
        } finally {
            FileUtils.deleteDirectory(rootDir.toFile());
            FileUtils.deleteDirectory(unpackResults.toFile());
        }

    }

}