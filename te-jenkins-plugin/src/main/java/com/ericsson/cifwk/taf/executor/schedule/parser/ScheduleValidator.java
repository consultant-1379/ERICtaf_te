package com.ericsson.cifwk.taf.executor.schedule.parser;

import com.ericsson.cifwk.taf.executor.schedule.InvalidScheduleException;
import com.google.common.base.Throwables;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.net.URL;

import static java.lang.String.format;

public class ScheduleValidator {

    private static final String NAMESPACE = "http://taf.lmera.ericsson.se/schema/te";

    public static final String DEFAULT_SCHEMA_LOCATION = "com/ericsson/cifwk/taf/executor/schema/schedule.xsd";

    private final Validator validator;

    public ScheduleValidator(Validator validator) {
        this.validator = validator;
    }

    public static ScheduleValidator withDefaultSchema() {
        URL schemaUrl = ScheduleValidator.class.getClassLoader().getResource(DEFAULT_SCHEMA_LOCATION);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = schemaFactory.newSchema(schemaUrl);
            Validator validator = schema.newValidator();
            return new ScheduleValidator(validator);
        } catch (SAXException e) {
            throw Throwables.propagate(e);
        }
    }

    public void validate(String xml) {
        StringReader reader = new StringReader(xml);
        InputSource inputSource = new InputSource(reader);
        try {
            NamespaceFilter filter = new NamespaceFilter(NAMESPACE, XMLReaderFactory.createXMLReader());
            SAXSource source = new SAXSource(filter, inputSource);
            validator.validate(source);
        } catch (Exception e) {
            throw new InvalidScheduleException(format("Provided schedule is malformed (%s) '%s'", e.getMessage(), xml), e);
        }
    }

    private static class NamespaceFilter extends XMLFilterImpl {

        private final String implicitNamespace;

        public NamespaceFilter(String implicitNamespace, XMLReader parent) {
            super(parent);
            this.implicitNamespace = implicitNamespace;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (uri.isEmpty()) {
                uri = implicitNamespace;
            }
            super.startElement(uri, localName, qName, atts);
        }
    }
}
