package com.ericsson.cifwk.taf.executor.helpers;

import com.ericsson.cifwk.taf.executor.ArtifactHelper;
import com.ericsson.cifwk.taf.executor.CommonTestConstants;
import com.ericsson.cifwk.taf.executor.TafScheduleProject;
import com.ericsson.duraci.datawrappers.ArtifactGav;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class TafScheduleProjectAdapter {

    private final TafScheduleProject project;

    public TafScheduleProjectAdapter(TafScheduleProject project) {
        this.project = project;
    }

    public void resolveScheduleAs(ArtifactGav scheduleGav, String schedulePath, String scheduleXmlToBeResolved) {
        ArtifactHelper artifactHelper = project.getArtifactHelper();
        when(artifactHelper.resolveArtefact(
                eq(CommonTestConstants.REPOSITORY_URL),
                eq(scheduleGav.getGroupId() + ":" + scheduleGav.getArtifactId() + ":" + scheduleGav.getVersion()),
                eq(schedulePath))).thenReturn(scheduleXmlToBeResolved);
    }

    public void resolveScheduleByDefaultAs(ArtifactGav scheduleGav, String scheduleXmlToBeResolved) {
        ArtifactHelper artifactHelper = project.getArtifactHelper();
        when(artifactHelper.resolveArtefact(
                eq(CommonTestConstants.REPOSITORY_URL),
                eq(scheduleGav.getGroupId() + ":" + scheduleGav.getArtifactId() + ":" + scheduleGav.getVersion()),
                anyString())).thenReturn(scheduleXmlToBeResolved);
    }

    public TafScheduleProject getProject() {
        return project;
    }

}
