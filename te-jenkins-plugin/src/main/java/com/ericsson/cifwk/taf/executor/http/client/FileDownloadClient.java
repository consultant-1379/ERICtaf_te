package com.ericsson.cifwk.taf.executor.http.client;

import com.google.common.io.ByteStreams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static java.lang.String.format;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.http.HttpStatus.SC_OK;

public class FileDownloadClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadClient.class);

    private HttpClient httpClient;

    public FileDownloadClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public File download(String url, Path destination) throws IOException, FileDownloadException {
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            checkFileDownload(statusCode == SC_OK, "Failed to download file from %s - status code was %s", url, statusCode);
            HttpEntity entity = response.getEntity();
            checkFileDownload(entity != null, "No response entity received from %s", url);

            LOGGER.info("Downloading file {} to {}", url, destination);
            persist(entity, destination);
        } finally {
            request.releaseConnection();
        }
        return destination.toFile();
    }

    private static void checkFileDownload(boolean condition, String message, Object... params) throws FileDownloadException {
        if (!condition) {
            throw new FileDownloadException(format(message, params));
        }
    }

    private static void persist(HttpEntity entity, Path destination) throws IOException {
        try (InputStream data = entity.getContent();
                FileOutputStream fileOutputStream = new FileOutputStream(destination.toFile());
                BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream)) {
            ByteStreams.copy(data, outputStream);
            outputStream.flush();
        }
    }
}
