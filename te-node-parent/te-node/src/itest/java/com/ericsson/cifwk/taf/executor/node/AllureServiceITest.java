package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.TestExecution;
import com.ericsson.cifwk.taf.executor.TestResult;
import com.ericsson.cifwk.taf.itest.EmbeddedJetty;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.apache.http.client.utils.URIBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.google.common.collect.Lists.newArrayList;
import static java.io.File.createTempFile;
import static org.assertj.core.api.Assertions.assertThat;

public class AllureServiceITest extends AbstractTafTestRunnerITest {

    private static final String ALLURE_SERVICE_URL_PATH = "/api/reports";
    private static final int ALLURE_SERVICE_PORT = 7070;
    private EmbeddedJetty jetty;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        jetty = EmbeddedJetty.build()
            .withServlet(new FakeAllureService(), ALLURE_SERVICE_URL_PATH + "/*")
            .withPort(ALLURE_SERVICE_PORT)
            .start();
    }

    @After
    public void tearDown() throws Exception {
        jetty.stop();
    }

    @Test
    public void happyPath() throws Exception {

        String allureServiceUrl = new URIBuilder()
            .setScheme("http")
            .setHost("localhost")
            .setPort(ALLURE_SERVICE_PORT)
            .setPath(ALLURE_SERVICE_URL_PATH)
            .build()
            .toURL().toString();
        TestExecution.Builder execBuilder = TestExecution.builder()
            .withTestware(RELEASED_TESTWARE)
            .withSuites("success.xml")
            .withRepositoryUrl(RELEASES_REPOSITORY_URL)
            .withAllureLogDir(allureLogDirName)
            .withMinTafVersion("2.1.1")
            .withAllureServiceUrl(allureServiceUrl)
            .withGeneralJobExecutionId(UUID.randomUUID().toString());
        TestExecution execution = execBuilder.build();
        runner.setUp(execution);
        TestResult testResult = runner.runTest();

        assertThat(testResult.getStatus()).isEqualTo(TestResult.Status.SUCCESS);
    }

    class FakeAllureService extends HttpServlet {

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertThat(req.getHeader("Accept-Encoding")).isEqualTo("gzip");
            assertThat(req.getContentLength()).isGreaterThan(0);
            File archive = createArchive(req);
            assertZipEntries(archive);
            resp.setStatus(200);
            resp.flushBuffer();
        }

        private File createArchive(HttpServletRequest req) throws IOException {
            byte[] responseBytes = ByteStreams.toByteArray(req.getInputStream());
            File tempFile = createTempFile("allure", "zip");
            Files.write(responseBytes, tempFile);
            return tempFile;
        }

        private void assertZipEntries(File archive) throws IOException {
            final ZipFile file = new ZipFile(archive);
            try {
                final Enumeration<? extends ZipEntry> entries = file.entries();
                List<String> entryNames = newArrayList();
                while (entries.hasMoreElements()) {
                    final ZipEntry entry = entries.nextElement();
                    entryNames.add(entry.getName());
                }
                assertThat(entryNames)
                    .hasSize(6)
                    .doesNotHaveDuplicates();
                Iterable<String> attachments = filter(entryNames, "-attachment.txt");
                Iterable<String> suites = filter(entryNames, "-testsuite.xml");

                assertThat(attachments).hasSize(5);
                assertThat(suites).hasSize(1);
            }
            finally {
                file.close();
            }
        }

        private List<String> filter(List<String> iterable, final String postfix) {
            return newArrayList(Iterables.filter(iterable, new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return input.endsWith(postfix);
                }
            }));
        }
    }
}
