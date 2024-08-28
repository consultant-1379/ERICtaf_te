package com.ericsson.cifwk.taf.executor.maven;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/03/2016
 */
abstract class AbstractPomGeneratorTest {

    protected String loadXmlFrom(String xmlFilePath) throws IOException {
        URL uri = Resources.getResource(xmlFilePath);
        return Resources.toString(uri, Charsets.UTF_8);
    }

    protected static class StringOutputStream extends OutputStream {

        private StringBuilder string = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            this.string.append((char) b);
        }

        @Override
        public String toString() {
            return this.string.toString();
        }
    }

}
