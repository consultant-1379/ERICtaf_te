package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.cifwk.taf.ui.sdk.Link;
import com.google.common.truth.Truth;

import org.junit.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.List;

public class ExternalGridAccessToLocalVmTest {

    @Test
    public void test() {
        // Should get resolved with Grid settings
        Browser defaultBrowser = UI.newBrowser();
        // TE's jenkinsm1
        BrowserTab tab = defaultBrowser.open("http://192.168.0.108:8080/jenkins");
        List<Link> links = tab.getGenericView().getViewComponents(".task-icon-link", Link.class);
        Truth.assertThat(links.isEmpty()).isTrue();
    }
}
