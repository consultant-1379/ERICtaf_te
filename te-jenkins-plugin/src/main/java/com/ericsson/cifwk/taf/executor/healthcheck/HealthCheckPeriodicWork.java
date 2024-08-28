package com.ericsson.cifwk.taf.executor.healthcheck;

import com.ericsson.cifwk.taf.executor.notifications.NotificationBar;
import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import hudson.Extension;
import hudson.model.PeriodicWork;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Extension
public class HealthCheckPeriodicWork extends PeriodicWork {

    public static final int HEALTH_CHECK_PERIOD_MIN = 10;

    private final Logger LOGGER = LoggerFactory.getLogger(HealthCheckPeriodicWork.class);

    @Override
    public long getRecurrencePeriod() {
        return HEALTH_CHECK_PERIOD_MIN * 60 * 1000;
    }

    @Override
    protected void doRun() throws Exception {
        Jenkins jenkins = JenkinsUtils.getJenkinsInstance();
        List<HealthParam> healthParams = HealthCheck.getInstance(jenkins).healthCheck();

        if (healthParams.size() > 0) {
            StringBuilder logMessage =
                    new StringBuilder(String.format("%n%n######## TAF EXECUTOR HEALTH CHECK ########%n"));

            boolean passed = true;

            for (HealthParam healthParam : healthParams) {
                logMessage.append(
                        String.format("%s (%s) - %s %s%n", healthParam.getName(),
                                healthParam.getScope(),
                                healthParam.isPassed() ? "OK" : "FAIL",
                                healthParam.getDescription()));

                if (!healthParam.isPassed()) passed = false;
            }

            LOGGER.info(logMessage.append(String.format("###########################################%n%n")).toString());

            if (!passed) {
                NotificationBar notificationBar = NotificationBar.getInstance();
                notificationBar.notify(NotificationBar.NotificationType.ERROR,
                        "TAF Executor problems found. Please <a href='/jenkins/log/all'>check log</a> for more details");
            }
        }
    }

}
