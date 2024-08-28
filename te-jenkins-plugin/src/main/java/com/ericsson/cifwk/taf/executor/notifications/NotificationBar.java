package com.ericsson.cifwk.taf.executor.notifications;


import com.ericsson.cifwk.taf.executor.utils.JenkinsUtils;
import hudson.Extension;
import hudson.model.PageDecorator;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Extension
public class NotificationBar extends PageDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationBar.class);

    private int REFRESH_INTERVAL_MS = 30 * 1000;
    private BlockingQueue<Notification> notifications = new ArrayBlockingQueue<>(20);

    public static NotificationBar getInstance() {
        return JenkinsUtils.getJenkinsInstance().getExtensionList(NotificationBar.class).iterator().next();
    }

    public synchronized void notify(NotificationType type, String text) {
        if (notifications.remainingCapacity() == 0) {
            notifications.poll();
        }

        if (!notifications.offer(new Notification(System.currentTimeMillis(), type, text))) {
            LOGGER.error("Failed to send notification '{}'", text);
        }
    }

    @JavaScriptMethod
    public int getRefreshIntervalMs() {
        return REFRESH_INTERVAL_MS;
    }


    @JavaScriptMethod
    public synchronized JSON getMessages() {
        JSONArray json = new JSONArray();

        while (notifications.peek() != null
                && notifications.peek().getCreationTime() + getRefreshIntervalMs() < System.currentTimeMillis() + 1000) {
            notifications.poll();
        }

        for (Notification notification : notifications) {
            json.add(notification);
        }

        return json;
    }

    public enum NotificationType {
        OK, WARNING, ERROR
    }

    public static class Notification implements Serializable {
        Long creationTime;
        NotificationType type;
        String text;

        Notification(long creationTime, NotificationType type, String text) {
            this.creationTime = creationTime;
            this.type = type;
            this.text = text;
        }

        public Long getCreationTime() {
            return creationTime;
        }

        public NotificationType getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }

}
