package com.ericsson.cifwk.taf.executor.maven;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.executor.NodeConfigurationProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.invoker.InvocationRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.taf.executor.maven.InvocationRequestHelper.mavenInvocationRequest;

public class TestPomGenerator extends AbstractPomGenerator {

    private static final String TAF_TAG = "<tafversion>%s</tafversion>";
    private static String comparisonFileName = "comparison.txt";
    private static final Pattern ARTIFACT_VERSION_PATTERN = Pattern.compile("\\d[\\d\\.]+$|\\d[\\d\\.]+\\-SNAPSHOT");

    public void emit(PomValues pomValues, OutputStream outputStream) {
        Map<String, Object> input = new HashMap<>();
        input.put("repositoryUrl", pomValues.getRepositoryUrl());
        input.put("testware", pomValues.getTestware());
        input.put("suites", pomValues.getSuites());
        input.put("groups", pomValues.getGroups());
        input.put("allureLogDir", pomValues.getAllureLogDir());
        input.put("ALLURE_SERVICE_URL", pomValues.getAllureServiceUrl());
        input.put("GENERAL_EXECUTION_ID", pomValues.getGeneralExecutionId());
        input.put("additionalDependencies", pomValues.getAdditionalDependencies());
        input.put("user_defined_boms", pomValues.getUserDefinedBOMs());
        input.put("user_defined_poms", pomValues.getUserDefinedPOMs());

        addIfNotBlank(input, "taf_version", pomValues.getMinTafVersion());
        addIfNotBlank(input, "allure_version", pomValues.getAllureVersion());

        NodeConfigurationProvider nodeConfigurationProvider = nodeConfigProvider();
        addIfNotBlank(input, "taf_maven_plugin_version", nodeConfigurationProvider.getTafMavenPluginVersion());
        addIfNotBlank(input, "taf_surefire_provider_version", nodeConfigurationProvider.getTafSurefireProviderVersion());
        addIfNotBlank(input, "te_version", nodeConfigurationProvider.getTeVersion());

        Set<Property> properties = Sets.newLinkedHashSet();
        Map<String, String> systemSettings = pomValues.getSystemProperties();
        for (Map.Entry<String, String> entry : systemSettings.entrySet()) {
            properties.add(new Property(entry.getKey(), entry.getValue()));
        }
        String skipTestsValue = pomValues.getSkipTests();
        if (!StringUtils.isBlank(skipTestsValue) && skipTestsValue.equals("true")){
                    properties.add(new Property("skipTests", pomValues.getSkipTests()));
        }
        properties.add(new Property("ER_REPORTING_PARENT_EVENT_ID", pomValues.getParentEventId()));
        properties.add(new Property("ER_REPORTING_PARENT_EXECUTION_ID", pomValues.getParentExecutionId()));
        properties.add(new Property("ER_REPORTING_TEST_EXECUTION_ID", pomValues.getTestExecutionId()));
        properties.add(new Property("ER_REPORTING_MB_HOST", pomValues.getMbHost()));
        properties.add(new Property("ER_REPORTING_MB_EXCHANGE", pomValues.getMbExchange()));
        properties.add(new Property("ER_REPORTING_MB_DOMAIN", pomValues.getMbDomain()));
        properties.add(new Property("BUILD_URL", pomValues.getLogUrl()));
        properties.add(new Property(TafConfiguration.TAF_HTTP_CONFIG_URL, pomValues.getConfigUrl()));
        input.put("properties", properties);

        emit("taf_test.xml.ftl", input, outputStream);
    }

    public void updateTafVersion(Pom pom, PomValues values, File workingDir, PrintStream buildLog) {
        GAV testware = values.getTestware();
        runPomComparison(pom, workingDir, buildLog, testware);

        File comparison = new File(workingDir, comparisonFileName);
        String line;
        String versionInTeamsTestware = null;
        try (BufferedReader br = new BufferedReader(new FileReader(comparison))) {
            while (((line = br.readLine()) != null) && (versionInTeamsTestware == null)) {
                if (line.contains("com.ericsson.cifwk:taf")) {
                    versionInTeamsTestware = matchVersion(line);
                }
            }
        } catch (FileNotFoundException e) {
            buildLog.println("Dependency versions comparison file not found: " + e);
        } catch (IOException e) {
            buildLog.println("Exception when reading Dependency versions comparison file: " + e);
        }
        //if remote version greater than values.getMinTafVersion replace in pom
        if ((versionInTeamsTestware != null) && newerVersion("Taf", versionInTeamsTestware, values.getMinTafVersion(), buildLog)) {
            writeChangeToPom(pom, TAF_TAG, values.getMinTafVersion(), versionInTeamsTestware, buildLog);
        }
    }

    private int runPomComparison(Pom pom, File workingDir, final PrintStream buildLog, GAV testware) {
        InvocationRequest siteRequest = mavenInvocationRequest();
        siteRequest.setPomFile(pom.getFile());
        List<String> goals = new ArrayList<>();
        String goal = String.format("org.codehaus.mojo:versions-maven-plugin:2.2:compare-dependencies -DremotePom=%s:%s:%s " +
                        "-DreportOutputFile=%s", testware.getGroupId(), testware.getArtifactId(),
                testware.getVersion(), comparisonFileName);
        goals.add(goal);

        buildLog.println("Settings goals to execute: " + goals.toString());
        siteRequest.setGoals(goals);

        siteRequest = setOutputHandlers(siteRequest, buildLog);

        return InvocationRequestHelper.invokeRequest(workingDir, siteRequest, buildLog);
    }

    @VisibleForTesting
    void writeChangeToPom(Pom pom, String tag, String oldVersion, String newVersion, PrintStream buildLog) {
        Path path = Paths.get(pom.getFile().getAbsolutePath());
        Charset charset = StandardCharsets.UTF_8;
        try {
            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll(String.format(tag, oldVersion),
                    String.format(tag, newVersion));
            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            buildLog.println("Exception thrown when updating dependency versions in generated pom " + e);
        }
    }

    @VisibleForTesting
    String matchVersion(String output) {
        Matcher m = ARTIFACT_VERSION_PATTERN.matcher(output);
        String version = null;
        while (m.find()) {
            version = m.group();
        }
        return version;
    }

    @VisibleForTesting
    boolean newerVersion(String dependency, String versionInTeamsTestware, String versionFromBuild, PrintStream buildLog) {
        buildLog.println(String.format("Remote version of %s is: %s", dependency, versionInTeamsTestware));
        buildLog.println(String.format("Checking if remote version of %s is greater than build specified version, %s",
                dependency, versionFromBuild));
        List<String> testwareVersion = Arrays.asList(versionInTeamsTestware.split("\\."));
        List<String> buildVersion = Arrays.asList(versionFromBuild.split("\\."));
        Iterator testwareVersionIterator = testwareVersion.iterator();
        Iterator buildVersionIterator = buildVersion.iterator();
        while (buildVersionIterator.hasNext()) {
            if (!testwareVersionIterator.hasNext()) {
                return false;
            }
            String testwareVersionStr = ((String) testwareVersionIterator.next()).replaceAll("\\D+", "");
            String buildVersionStr = ((String) buildVersionIterator.next()).replaceAll("\\D+", "");
            int testwareVersionDigit = Integer.parseInt(testwareVersionStr);
            int buildVersionDigit = Integer.parseInt(buildVersionStr);
            if (testwareVersionDigit > buildVersionDigit) {
                buildLog.println(String.format("Setting version of %s to: %s", dependency, versionInTeamsTestware));
                return true;
            }
            if (testwareVersionDigit < buildVersionDigit) {
                buildLog.println(String.format("Setting version of %s to: %s", dependency, versionFromBuild));
                return false;
            }
        }
        if (testwareVersionIterator.hasNext()) {
            return true;
        }
        boolean testwareVersionIsSnapshot = versionInTeamsTestware.contains("SNAPSHOT");
        boolean buildVersionIsSnapshot = versionFromBuild.contains("SNAPSHOT");
        boolean bothSnapshots = testwareVersionIsSnapshot && buildVersionIsSnapshot;
        boolean bothNotSnapshots = !testwareVersionIsSnapshot && !buildVersionIsSnapshot;
        if (bothSnapshots || bothNotSnapshots) {
            buildLog.println(String.format("Build and Remote versions of %s are equal", dependency));
            return false;
        }
        if (!testwareVersionIsSnapshot) {
            buildLog.println(String.format("Setting version of %s to: %s", dependency, versionInTeamsTestware));
            return true;
        }
        buildLog.println(String.format("Setting version of %s to: %s", dependency, versionFromBuild));
        return false;
    }
}
