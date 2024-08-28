package com.ericsson.cifwk.taf.executor.http.client;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Files.readLines;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newInputStream;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FileDownloadClientTest {

    private static final String DEFAULT_URL = "http://localhost/api/reports/1234";

    private FileDownloadClient downloadClient;

    @Mock
    private HttpClient httpClient;

    private Path reportPath;

    @Before
    public void setUp() throws IOException {
        downloadClient = new FileDownloadClient(httpClient);
        reportPath = createTempFile("report", "zip");
    }

    @Test
    public void download_file_with_error_response() throws Exception {
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), SC_BAD_REQUEST, "Not OK");
        HttpResponse response = new BasicHttpResponse(statusLine);
        doReturn(response).when(httpClient).execute(any(HttpGet.class));

        assertThatThrownBy(() -> downloadClient.download(DEFAULT_URL, reportPath))
                .isInstanceOf(FileDownloadException.class)
                .hasMessage("Failed to download file from %s - status code was 400", DEFAULT_URL);
        verify(httpClient).execute(any(HttpGet.class));
    }

    @Test
    public void download_file_with_empty_response() throws Exception {
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 0), SC_OK, "OK");
        HttpResponse response = new BasicHttpResponse(statusLine);
        response.setEntity(null);
        doReturn(response).when(httpClient).execute(any(HttpGet.class));

        assertThatThrownBy(() -> downloadClient.download(DEFAULT_URL, reportPath))
                .isInstanceOf(FileDownloadException.class)
                .hasMessage("No response entity received from %s", DEFAULT_URL);
        verify(httpClient).execute(any(HttpGet.class));
    }

    @Test
    public void download_file_with_response() throws Exception {
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 0), SC_OK, "OK");
        HttpResponse response = new BasicHttpResponse(statusLine);

        response.setEntity(new InputStreamEntity(newInputStream(getResource("report-data.txt"))));
        doReturn(response).when(httpClient).execute(any(HttpGet.class));

        File responseFile = downloadClient.download(DEFAULT_URL, reportPath);

        try {
            verify(httpClient).execute(any(HttpGet.class));
            assertThat(responseFile).isNotNull();

            List<String> records = readLines(responseFile, Charset.forName("UTF-8"));
            assertThat(records).hasSameElementsAs(newArrayList("It is report data."));
        } finally {
            FileUtils.deleteQuietly(responseFile);
        }
    }

    private static Path getResource(String resourceName) throws URISyntaxException {
        return Paths.get(Resources.getResource(resourceName).toURI());
    }
}
