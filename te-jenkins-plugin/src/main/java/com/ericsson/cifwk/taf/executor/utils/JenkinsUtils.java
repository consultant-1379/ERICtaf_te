package com.ericsson.cifwk.taf.executor.utils;

import com.cloudbees.plugins.flow.BuildFlow;
import com.cloudbees.plugins.flow.FlowRun;
import com.ericsson.cifwk.taf.executor.BuildParameterNames;
import com.ericsson.cifwk.taf.executor.TAFExecutor;
import com.ericsson.cifwk.taf.executor.TafExecutionBuild;
import com.ericsson.cifwk.taf.executor.TafExecutionProject;
import com.ericsson.cifwk.taf.executor.TafScheduleBuild;
import com.ericsson.cifwk.taf.executor.TafScheduleProject;
import com.ericsson.cifwk.taf.executor.model.BuildParameterHolderFactory;
import com.ericsson.cifwk.taf.executor.model.BuildParametersHolder;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.model.AbstractBuild;
import hudson.model.Actionable;
import hudson.model.Build;
import hudson.model.Node;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Project;
import hudson.util.VariableResolver;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import static org.apache.commons.lang.StringUtils.contains;

public final class JenkinsUtils {

    private JenkinsUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Project> T getProjectOfType(Jenkins jenkins, final Class<T> clazz) {
        List<? extends Project> projectsOfType = getProjectsOfType(jenkins, clazz);
        return (T) Iterables.getFirst(projectsOfType, null);
    }

    public static FlowRun waitForFlowRun(final Jenkins jenkins, final ExecutionId executionId, int timeoutInSeconds) {
        return TimeLimitedTask.performUntilTimeout(new TimeLimitedWorker<FlowRun>() {
            @Override
            public Optional<FlowRun> doWork() {
                FlowRun job = getFlowRun(jenkins, executionId);
                return job == null ? Optional.<FlowRun>absent() : Optional.of(job);
            }
        }, timeoutInSeconds);
    }

    public static FlowRun getFlowRun(Jenkins jenkins, ExecutionId executionId) {
        List<BuildFlow> projectsOfType = getProjectsOfType(jenkins, BuildFlow.class);
        for (BuildFlow project : projectsOfType) {
            // should be only 1 build per project
            FlowRun lastBuild = project.getLastBuild();
            if (lastBuild != null && isExecutionIdDefined(lastBuild, executionId)) {
                return lastBuild;
            }
        }
        return null;
    }

    public static List<Project> getAllProjects(Jenkins jenkins) {
        return getProjectsOfType(jenkins, Project.class);
    }

    public static <T extends Project> List<T> getProjectsOfType(Jenkins jenkins, final Class<T> clazz) {
        return jenkins.getAllItems(clazz);
    }

    public static TafScheduleBuild waitForSchedulerJob(final TafScheduleProject tafScheduleProject,
                                                       final ExecutionId executionId,
                                                       int timeoutInSeconds) {
        return TimeLimitedTask.performUntilTimeout(new TimeLimitedWorker<TafScheduleBuild>() {
            @Override
            public Optional<TafScheduleBuild> doWork() {
                TafScheduleBuild job = findSchedulerJob(tafScheduleProject, executionId);
                return job == null ? Optional.<TafScheduleBuild>absent() : Optional.of(job);
            }
        }, timeoutInSeconds);
    }

    public static TafScheduleBuild getSchedulerJob(Jenkins jenkins, String executionId) {
        return getSchedulerJob(jenkins, new ExecutionId(executionId));
    }

    public static TafScheduleBuild getSchedulerJob(Jenkins jenkins, ExecutionId executionId) {
        TafScheduleProject tafScheduleProject = getProjectOfType(jenkins, TafScheduleProject.class);
        return findSchedulerJob(tafScheduleProject, executionId);
    }

    public static TafScheduleBuild findSchedulerJob(TafScheduleProject tafScheduleProject, String executionId) {
        return findSchedulerJob(tafScheduleProject, new ExecutionId(executionId));
    }

    public static TafScheduleBuild findSchedulerJob(TafScheduleProject tafScheduleProject, ExecutionId executionId) {
        Preconditions.checkArgument(tafScheduleProject != null,
                "Cannot find the TAF Schedule project in Jenkins. It is needed for TE job execution");
        SortedMap<Integer, TafScheduleBuild> buildsAsMap = tafScheduleProject.getBuildsAsMap();
        for (TafScheduleBuild lastBuild : buildsAsMap.values()) {
            if (isExecutionIdDefined(lastBuild, executionId)) {
                return lastBuild;
            }
        }
        return null;
    }

    public static boolean isExecutionIdDefined(Actionable build, ExecutionId executionId) {
        Preconditions.checkArgument(executionId != null);
        List<ParametersAction> actions = build.getActions(ParametersAction.class);
        for (ParametersAction action : actions) {
            ParameterValue buildExecutionId = action.getParameter(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID);
            if (buildExecutionId != null && buildExecutionId.getShortDescription() != null &&
                    buildExecutionId.getShortDescription().contains(executionId.toString())) {
                return true;
            }
        }

        return false;
    }

    public static List<TafExecutionBuild> findExecutorJobs(TafExecutionProject executionProject, ExecutionId executionId) {
        Preconditions.checkArgument(executionProject != null,
                "Cannot find the TAF Execution project in Jenkins. It is needed for TE job execution");
        List<TafExecutionBuild> result = Lists.newArrayList();
        SortedMap<Integer, TafExecutionBuild> buildsAsMap = executionProject.getBuildsAsMap();
        for (TafExecutionBuild lastBuild : buildsAsMap.values()) {
            if (isExecutionIdDefined(lastBuild, executionId)) {
                result.add(lastBuild);
            }
        }
        return result;
    }

    public static String getBuildParameter(Build build, String paramName) {
        List<ParametersAction> actions = build.getActions(ParametersAction.class);
        for (ParametersAction action : actions) {
            String result = getBuildParameter(build, action, paramName);
            if (StringUtils.isNotBlank(result)) {
                return result;
            }
        }
        return null;
    }

    private static String getBuildParameter(AbstractBuild<?, ?> build, ParametersAction action, String paramName) {
        ParameterValue param = action.getParameter(paramName);
        return (param == null) ? null : getStringParamValue(build, param);
    }

    public static String getStringParamValue(AbstractBuild<?, ?> build, ParameterValue parameterValue) {
        VariableResolver variableResolver = parameterValue.createVariableResolver(build);
        return (String) variableResolver.resolve(parameterValue.getName());
    }

    public static String toString(ParametersAction action) {
        StringBuilder toString = new StringBuilder();
        Iterator<ParameterValue> iterator = action.iterator();
        while (iterator.hasNext()) {
            ParameterValue param = iterator.next();
            toString.append(param.getName()).append("=").append(param.toString())
                    .append(iterator.hasNext() ? "\n\r" : "");
        }
        return toString.toString();
    }

    public static <T extends Project> void checkIfCanCreateNewProject(Jenkins jenkins, final Class<T> clazz, int maxAmount) {
        List<T> existingProjects = JenkinsUtils.getProjectsOfType(jenkins, clazz);
        if (existingProjects.size() >= maxAmount) {
            throw new IllegalStateException(String.format("Cannot have more than %d instance(-s) of %s project",
                    maxAmount, clazz.getName()));
        }
    }

    public static List<Node> getTeNodes(Jenkins jenkins) {
        Iterable<Node> teSlaves = Iterables.filter(jenkins.getNodes(), new Predicate<Node>() {
            @Override
            public boolean apply(Node input) {
                return input != null && contains(input.getLabelString(), TAFExecutor.TAF_NODE_LABEL);
            }
        });
        return Lists.newArrayList(teSlaves);
    }

    public static Optional<ExecutionId> findExecutionId(List<ParametersAction> actions) {
        if (!actions.isEmpty()) {
            for (ParametersAction action : actions) {
                ParameterValue executionId = action.getParameter(BuildParameterNames.EIFFEL_JOB_EXECUTION_ID);
                if (executionId != null){
                    return Optional.of(new ExecutionId(executionId.getValue().toString()));
                }
            }
        }
        return Optional.absent();
    }

    public static <T extends BuildParametersHolder> T getBuildParameters(final AbstractBuild<?, ?> build, Class<T> clazz) {
        final List<ParametersAction> actions = build.getActions(ParametersAction.class);
        if (actions.isEmpty()) {
            throw new IllegalStateException("Build doesn't have any parameters");
        }
        return BuildParameterHolderFactory.createHolder(clazz, new Function<String, String>() {
            @Nullable
            @Override
            public String apply(String parameterName) {
                return getBuildParameter(build, actions.get(0), parameterName);
            }
        });
    }

    public static Jenkins getJenkinsInstance() {
        Jenkins instance = Jenkins.getInstance();
        Preconditions.checkState(instance != null, "Jenkins instance is undefined");
        return instance;
    }

}
