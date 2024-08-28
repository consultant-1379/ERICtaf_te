package com.ericsson.cifwk.taf.executor.healthcheck;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 05/06/2017
 */
public class TeNodeLogMountAccessCheckCallableTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private TeNodeLogMountAccessCheckCallable unit;

    @Before
    public void init() {
        unit = new TeNodeLogMountAccessCheckCallable("nodeName", tmpFolder.getRoot().toString(), 2);
        unit = spy(unit);
    }

    @Test
    public void doCheck_fileCreation() throws Exception {
        doReturn(true).when(unit).checkMount();
        HealthParam check = new HealthParam("checkName", "nodeName");

        unit.doCheck(check);

        assertThat(check.isPassed()).isTrue();
        verify(unit).createTmpFile(anyString());
        verify(unit).deleteTmpFile(any(Path.class));
    }

}