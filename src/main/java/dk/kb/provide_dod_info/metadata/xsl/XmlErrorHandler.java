package dk.kb.provide_dod_info.metadata.xsl;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.LinkedList;
import java.util.List;

/**
 * Error handler for XML.
 */
public class XmlErrorHandler implements ErrorHandler {

    /** Errors messages. */
    protected List<String> errors = new LinkedList<>();

    /** Fatal errors messages. */
    protected List<String> fatalErrors = new LinkedList<>();

    /** Warning messages. */
    protected List<String> warnings = new LinkedList<>();

    /**
     * Reset accumulated errors counters.
     */
    public void reset() {
        errors.clear();
        fatalErrors.clear();
        warnings.clear();
    }

    /**
     * Returns a boolean indicating whether this handler has recorded any errors.
     * @return a boolean indicating whether this handler has recorded any errors
     */
    public boolean hasErrors() {
        return errors.size() != 0 || fatalErrors.size() != 0 || warnings.size() != 0;
    }

    @Override
    public void warning(SAXParseException exception)  {
        warnings.add(exception.getMessage());

    }

    @Override
    public void error(SAXParseException exception) {
        errors.add(exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception) {
        fatalErrors.add(exception.getMessage());
    }
}
