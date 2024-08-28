package com.ericsson.cifwk.taf.executor.maven;

import com.ericsson.cifwk.taf.executor.NodeConfigurationProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.invoker.InvocationRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Map;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/03/2016
 */
public abstract class AbstractPomGenerator {

    protected void emit(String templateFileName, Map<String, Object> parameterMap, OutputStream outputStream) {
        Preconditions.checkArgument(StringUtils.isNotBlank(templateFileName));

        freemarker.template.Configuration cfg = new freemarker.template.Configuration();
        cfg.setClassForTemplateLoading(this.getClass(), "/");
        cfg.setDefaultEncoding("UTF-8");
        try {
            Template template = cfg.getTemplate("templates/" + templateFileName);
            process(outputStream, template, parameterMap);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    protected void addIfNotBlank(Map<String, Object> map, String key, String value) {
        if (!StringUtils.isBlank(value)) {
            map.put(key, value);
        }
    }

    protected void process(OutputStream outputStream, Template template, Map<String, Object> input)
            throws IOException, TemplateException {
        Writer consoleWriter = new OutputStreamWriter(outputStream);
        template.process(input, consoleWriter);
    }

    protected InvocationRequest setOutputHandlers(final InvocationRequest request, final PrintStream buildLog){
        request.setOutputHandler(new MavenToTeOutputHandler(buildLog));
        request.setErrorHandler(new MavenToTeOutputHandler(buildLog));
        return request;
    }

    @VisibleForTesting
    protected NodeConfigurationProvider nodeConfigProvider() {
        return NodeConfigurationProvider.getInstance();
    }

    static public class Property {
        private final String name;
        private final String value;

        public Property(String name, String value) {
            Preconditions.checkArgument(StringUtils.isNotBlank(name), "Property name cannot be null");
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Property property = (Property) o;

            if (!name.equals(property.name)) return false;
            if (value != null ? !value.equals(property.value) : property.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

}
