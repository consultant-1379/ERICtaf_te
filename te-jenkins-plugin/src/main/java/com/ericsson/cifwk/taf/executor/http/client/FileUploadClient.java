package com.ericsson.cifwk.taf.executor.http.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;

public class FileUploadClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadClient.class);

    private HttpClient httpClient;

    public FileUploadClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void upload(String url, File entity) throws IOException {

        HttpPut request = new HttpPut(url);
        request.setEntity(new FileEntity(entity, APPLICATION_OCTET_STREAM));

        LOGGER.info("upload file {} to {}", entity, url);

        try {
            HttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            checkArgument(statusCode == SC_OK, "%s: error while upload file to %s", statusCode, url);
        } finally {
            request.releaseConnection();
        }
    }
}
