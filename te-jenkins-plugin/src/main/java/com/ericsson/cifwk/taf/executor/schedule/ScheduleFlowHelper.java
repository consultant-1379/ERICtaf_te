package com.ericsson.cifwk.taf.executor.schedule;

import com.ericsson.cifwk.taf.executor.api.ScheduleSource;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ManualTestData;
import com.ericsson.cifwk.taf.executor.api.schedule.model.Schedule;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleComponent;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleGavLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItemGroup;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLoader;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleLocation;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleManualItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.TafScheduleLocation;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;
import com.ericsson.cifwk.taf.executor.model.ScheduleBuildParameters;
import com.ericsson.cifwk.taf.executor.schedule.parser.ScheduleParser;
import com.ericsson.cifwk.taf.executor.schedule.parser.ScheduleParserFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class ScheduleFlowHelper {

    private final ScheduleParserFactory parserFactory;
    private final ScheduleLoaderFactory loaderFactory;
    private final FlowEmitterFactory emitterFactory;
    private final GlobalTeSettings globalTeSettings;
    private final ScheduleBuildParameters mainBuildParams;
    private final ScheduleSource scheduleSource;


    public ScheduleFlowHelper(GlobalTeSettings globalTeSettings, ScheduleBuildParameters mainBuildParams) {
        this(new ScheduleParserFactory(), new ScheduleLoaderFactory(), new FlowEmitterFactory(),
                globalTeSettings, mainBuildParams);
    }

    ScheduleFlowHelper(ScheduleParserFactory parserFactory,
                       ScheduleLoaderFactory loaderFactory,
                       FlowEmitterFactory emitterFactory,
                       GlobalTeSettings globalTeSettings,
                       ScheduleBuildParameters mainBuildParams) {
        String scheduleSourceStr = mainBuildParams.getScheduleSource();
        this.scheduleSource = ScheduleSource.valueOf(scheduleSourceStr);
        this.parserFactory = parserFactory;
        this.loaderFactory = loaderFactory;
        this.emitterFactory = emitterFactory;
        this.globalTeSettings = globalTeSettings;
        this.mainBuildParams = mainBuildParams;
    }

    public Schedule getSchedule(ScheduleItemGavResolver scheduleItemGavResolver, String primaryScheduleXml) {
        ScheduleLoader loader;
        ScheduleLocation location;
        String repositoryUrl = mainBuildParams.getRepositoryUrl();
        String scheduleArtifact = mainBuildParams.getScheduleArtifact();
        switch (scheduleSource) {
            case TAF_SCHEDULER:
                loader = loaderFactory.createForTafSchedule(mainBuildParams.getTafSchedulerUrl());
                location = new TafScheduleLocation(0);
                break;
            default:
                loader = loaderFactory.createForGavSchedule(repositoryUrl, scheduleItemGavResolver);
                String scheduleName = mainBuildParams.getScheduleName();
                location = ScheduleGavLocation.ofArtifact(scheduleArtifact, scheduleName);
                break;
        }
        ScheduleParser parser = parserFactory.create(loader);
        return parser.parse(primaryScheduleXml, location);
    }

    public String getFlow(ScheduleItemGavResolver scheduleItemGavResolver, Schedule schedule) {
        return emitterFactory.create(schedule, scheduleItemGavResolver, globalTeSettings, mainBuildParams).emit();
    }

    public String getFlow(String scheduleXml) {
        ScheduleItemGavResolver gavResolver = scheduleItemResolverFor(mainBuildParams);
        Schedule scheduleObject = getSchedule(gavResolver, scheduleXml);
        return getFlow(gavResolver, scheduleObject);
    }

    public ScheduleItemGavResolver scheduleItemResolverFor(ScheduleBuildParameters buildParams) {
        String scheduleArtifact = buildParams.getScheduleArtifact();
        String testware = buildParams.getTestware();
        ImmutableList<String> testwares = ImmutableList.copyOf(
                Splitter.on(',')
                        .trimResults()
                        .omitEmptyStrings()
                        .split(Strings.nullToEmpty(testware))
        );

        Preconditions.checkArgument(ScheduleComponent.isGav(scheduleArtifact),
                "Schedule '%s' doesn't have a correct GAV format", scheduleArtifact);
        ScheduleComponent scheduleComponent = ScheduleComponent.parseGav(scheduleArtifact);
        return new ScheduleItemGavResolver(scheduleComponent, testwares);
    }

    public static int getSuiteCount(Schedule schedule) {
        List<ScheduleChild> topScheduleItems = schedule.getChildren();
        int suiteCount = 0;
        for (ScheduleChild scheduleChild : topScheduleItems) {
            suiteCount += getSuiteCount(scheduleChild);
        }
        return suiteCount;
    }

    @VisibleForTesting
    static int getSuiteCount(ScheduleChild scheduleChild) {
        if (scheduleChild instanceof ScheduleItem) {
            ScheduleItem scheduleItem = (ScheduleItem) scheduleChild;
            List<String> suites = scheduleItem.getSuites();
            return (suites == null) ? 0 : suites.size();
        } else if (scheduleChild instanceof ScheduleItemGroup) {
            ScheduleItemGroup scheduleItemGroup = (ScheduleItemGroup) scheduleChild;
            int suiteCount = 0;
            for (ScheduleChild groupItem : scheduleItemGroup.getChildren()) {
                suiteCount += getSuiteCount(groupItem);
            }
            return suiteCount;
        } else if (scheduleChild instanceof ScheduleManualItem) {
            ScheduleManualItem scheduleItem = (ScheduleManualItem) scheduleChild;
            ManualTestData manualTestData = scheduleItem.getManualTestData();
            return manualTestData.getTestCampaignIds().size();
        } else {
            throw new UnsupportedOperationException(scheduleChild.getClass() + " is not yet supported");
        }
    }
}
