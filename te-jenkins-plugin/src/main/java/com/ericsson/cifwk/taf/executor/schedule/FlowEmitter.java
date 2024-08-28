package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.TestwareRuntimeLimitations;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleEnvironmentProperty;
import com.ericsson.cifwk.taf.executor.model.CommonBuildParameters;
import com.ericsson.cifwk.taf.executor.model.ExecutorBuildParameters;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ManualTestsBuildParameters;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class FlowEmitter {

    private static final Joiner DSL_BUILD_JOINER = Joiner.on(';').skipNulls();
    private static final Joiner DSL_CSV_JOINER = Joiner.on(',').skipNulls();
    private static final String DSL_PARALLEL = "parallel(%s)";
    private static final String DSL_PARALLEL_SET = "{%s}";

    private static final String FLOW_DSL_TEMPLATE_LOCATION = "templates/flow.dsl.ftl";

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowEmitter.class);

    private final Schedule schedule;
    private final ScheduleItemGavResolver scheduleItemGavResolver;
    private final GlobalTeSettings globalTeSettings;
    private final ScheduleBuildParameters mainBuildParameters;
    private final Function<ScheduleChild, String> childTransformer;
    private final Function<ScheduleChild, String> parallelChildTransformer;

    public FlowEmitter(Schedule schedule, ScheduleItemGavResolver scheduleItemGavResolver,
                       GlobalTeSettings globalTeSettings, ScheduleBuildParameters mainBuildParameters) {
        this.schedule = schedule;
        this.scheduleItemGavResolver = scheduleItemGavResolver;
        this.globalTeSettings = globalTeSettings;
        this.mainBuildParameters = mainBuildParameters;
        childTransformer = this::emitChild;
        parallelChildTransformer = Functions.compose(dsl -> String.format(DSL_PARALLEL_SET, dsl), childTransformer);
    }

    public String emit() {
        List<ScheduleChild> children = schedule.getChildren();
        return emitChildren(children, childTransformer);
    }

    public String emitChildren(List<ScheduleChild> children,
                               Function<? super ScheduleChild, String> transformFunction) {
        return DSL_BUILD_JOINER.join(Iterables.transform(children, transformFunction));
    }

    protected String process(Map<String, ?> input) {
        try {
            Configuration cfg = new Configuration();
            cfg.setClassForTemplateLoading(this.getClass(), "/");
            cfg.setDefaultEncoding("UTF-8");
            Template template = cfg.getTemplate(FLOW_DSL_TEMPLATE_LOCATION);
            StringWriter stringWriter = new StringWriter();
            template.process(input, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public String emitChild(ScheduleChild child) {
        return child.accept(new ScheduleChild.Visitor<String>() {
            @Override
            public String visitItem(String name,
                                    ScheduleComponent component,
                                    List<String> suiteList,
                                    List<String> groupList,
                                    String agentLabel,
                                    boolean stopOnFail,
                                    Integer optionalTimeoutInSeconds,
                                    List<ScheduleEnvironmentProperty> environmentProperties) {
                ScheduleComponent resolvedComponent = scheduleItemGavResolver.resolve(component);
                if (resolvedComponent == null) {
                    LOGGER.info("Schedule component '" + component
                            + "' is not in the list of provided testware, bypassing");
                    return null;
                }
                String suites = DSL_CSV_JOINER.join(suiteList);
                String groups = DSL_CSV_JOINER.join(groupList);
                String timeoutInSeconds = optionalTimeoutInSeconds != null ? optionalTimeoutInSeconds.toString() : "";

                Map<String, Object> input = new HashMap<>();
                input.put("stopOnFail", stopOnFail);
                Map<String, String> agentJobsMap = globalTeSettings.getAgentJobsMap();
                String jobName = getJobNameForLabel(agentJobsMap, agentLabel);
                input.put("tafJobName", jobName);

                ExecutorBuildParameters executorBuildParameters =
                        getExecutorBuildParameters(name, resolvedComponent, suites, groups, timeoutInSeconds, environmentProperties);
                input.put("params", jobParamsList(executorBuildParameters));

                return process(input);
            }

            @Override
            public String visitItemGroup(List<ScheduleChild> children, boolean parallel) {
                if (parallel) {
                    Iterable<String> builds = Iterables.transform(children, parallelChildTransformer);
                    String joinedBuilds = DSL_CSV_JOINER.join(builds);
                    return String.format(DSL_PARALLEL, joinedBuilds);
                } else {
                    return emitChildren(children, childTransformer);
                }
            }

            @Override
            public String visitManualTestItem(ManualTestData manualTestData) {
                ManualTestsBuildParameters buildParameters = new ManualTestsBuildParameters();
                buildParameters.setExecutionId(mainBuildParameters.getExecutionId());
                Set<String> testCampaignIds = manualTestData.getTestCampaignIds();
                buildParameters.setManualTestCampaignIdsAsCsv(DSL_CSV_JOINER.join(testCampaignIds));
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("stopOnFail", false);
                // Default TEST_EXECUTOR job name
                parameters.put("tafJobName", getJobNameForLabel(globalTeSettings.getAgentJobsMap(), null));
                parameters.put("params", jobParamsList(buildParameters));
                return process(parameters);
            }
        });
    }

    @VisibleForTesting
    String getJobNameForLabel(Map<String, String> agentJobsMap, String agentLabel) {
        String labelToSearch = isNotBlank(agentLabel) ? agentLabel : TAFExecutor.TAF_NODE_LABEL;
        return agentJobsMap.entrySet().stream()
                .filter(entry -> StringUtils.equals(entry.getKey(), labelToSearch))
                .findAny()
                .map(Map.Entry::getValue)
                .get();
    }

    private ExecutorBuildParameters getExecutorBuildParameters(String name, ScheduleComponent resolvedComponent,
                                                               String suites, String groups, String timeoutInSeconds,
                                                               List<ScheduleEnvironmentProperty> environmentProperties) {
        String jobExecutionId = mainBuildParameters.getExecutionId();
        ExecutionId scheduleItemExecutionId = createScheduleItemExecutionId();
        String scheduleItemExecutionIdStr = scheduleItemExecutionId.toString();

        ExecutorBuildParameters executorBuildParameters = new ExecutorBuildParameters();
        executorBuildParameters.setTestStepName(Strings.nullToEmpty(name));
        executorBuildParameters.setExecutionId(Strings.nullToEmpty(jobExecutionId));
        executorBuildParameters.setEiffelScheduleItemExecutionId(Strings.nullToEmpty(scheduleItemExecutionIdStr));
        executorBuildParameters.setTafTestwareGav(Strings.nullToEmpty(resolvedComponent.toString()));
        executorBuildParameters.setTafSuites(Strings.nullToEmpty(suites));
        executorBuildParameters.setTafGroups(Strings.nullToEmpty(groups));
        executorBuildParameters.setTimeoutInSeconds(Strings.nullToEmpty(timeoutInSeconds));
        if (!environmentProperties.isEmpty()) {
            executorBuildParameters.setEnvPropertyJson(serializeToJson(environmentProperties));
        }
        TestwareRuntimeLimitations defaultRuntimeLimitations = globalTeSettings.getDefaultRuntimeLimitations();
        if (defaultRuntimeLimitations != null) {
            executorBuildParameters.setRuntimeLimitationsJson(serializeToJson(defaultRuntimeLimitations));
        }

        return executorBuildParameters;
    }

    @VisibleForTesting
    String serializeToJson(Object pojo) {
        return new Gson().toJson(pojo);
    }

    @VisibleForTesting
    protected ExecutionId createScheduleItemExecutionId() {
        return new ExecutionId();
    }

    private List<String[]> jobParamsList(CommonBuildParameters parameters) {
        List<String[]> result = Lists.newArrayList();
        Map<String, String> allParameters = parameters.getAllParameters();
        for (Map.Entry<String, String> entry : allParameters.entrySet()) {
            String value = entry.getValue();
            result.add(new String[] { entry.getKey(), escapeFlowParamValue(value) });
        }
        return result;
    }

    @VisibleForTesting
    String escapeFlowParamValue(String value) {
        return StringUtils.replace(value, "'", "\\'");
    }

}
