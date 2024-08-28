package com.ericsson.cifwk.taf.executor.cluster;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Node;
import org.apache.commons.lang.exception.ExceptionUtils;

import static java.lang.String.format;

/**
 * Adds a link 'View generated test POM' on every TEST_EXECUTOR build to view the actually generated test POM
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 26/06/2017
 */
@Extension
public class TestPomViewBadge implements Action {

    private String pom;

    // Needed for compilation to proceed
    public TestPomViewBadge() {}

    @SuppressFBWarnings
    public TestPomViewBadge(AbstractBuild build, String testPomLocation) {
        Preconditions.checkArgument(build != null);
        Node node = build.getBuiltOn();
        Preconditions.checkState(node != null, "Unable to get jenkins node that did the build");
        FilePath pathToPom = node.createPath(testPomLocation);
        try {
            if (pathToPom != null && pathToPom.exists()) {
                pom = pathToPom.readToString();
            } else {
                pom = format("Test POM not found in %s", pathToPom);
            }
        } catch (Exception e) {
            pom = format("Error while retrieving Test POM: %s%n%n%s", e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public String getIconFileName() {
        return "document-properties.png"; // One of the default Jenkins icons
    }

    @Override
    public String getDisplayName() {
        return "View generated test POM";
    }

    @Override
    public String getUrlName() {
        return "teTestPom";
    }

    public String getPom() {
        return pom;
    }
}
