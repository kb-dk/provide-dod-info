package dk.kb.provide_dod_info.metadata;

import dk.kb.provide_dod_info.exception.ArgumentCheck;
import dk.kb.provide_dod_info.metadata.xsl.XmlErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class for validating the XML file, primarily against its schema definition.
 */
public class MetadataValidator {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(MetadataValidator.class);

    /** Schema validation enabler name. */
    public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /** Schema validation enabler value. */
    public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    /** Cached document builder with DTD/Schema validation. */
    protected DocumentBuilder validationBuilder;

    /**
     * Construct an <code>XmlValidator</code> instance.
     */
    public MetadataValidator() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        try {
            validationBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Could not create a new 'DocumentBuilder'!");
        }
    }

    /**
     * Checks whether or not a given XML file is valid, according to XSD og DDT.
     * @param xmlFile The XML file to validate.
     * @return Whether or not the XML file is valid.
     */
    public boolean isValid(File xmlFile) {
        try (InputStream in = new FileInputStream(xmlFile)) {
            return validate(in, null, null);
        } catch (IOException e) {
            log.warn("Issue occurred while trying to validate XML file. Returning not valid.", e);
            return false;
        }
    }

    /**
     * Validate XML document for well-formed-ness and also against any DTD/XSD found.
     * @param in The XML input stream.
     * @param entityResolver XML entity resolver or null.
     * @param errorHandler error handler or null.
     * @return XML validation result.
     * @throws IOException If the validation .
     */
    public boolean validate(InputStream in, EntityResolver entityResolver,
            XmlErrorHandler errorHandler) throws IOException {
        ArgumentCheck.checkNotNull(in, "InputStream in");
        if (errorHandler == null) {
            errorHandler = new XmlErrorHandler();
        }
        errorHandler.reset();
        try {
            validationBuilder.reset();
            validationBuilder.setErrorHandler(errorHandler);
            validationBuilder.setEntityResolver(entityResolver);
            validationBuilder.parse(in);
            return !errorHandler.hasErrors();
        } catch (Throwable t) {
            log.error("Exception validating XML stream!", t);
            return false;
        }
    }
}
