package dk.kb.provide_dod_info;

/**
 * The constants, which are used across different classes.
 */
public class Constants {

    /** The suffix of XML files.*/
    private static final String XML_SUFFIX = ".xml";

    private static final String PDF_SUFFIX = ".pdf";
    /** The suffix of Publizon metadata XML files from pubhub.*/
    public static final String PUBHUB_METADATA_SUFFIX = ".pubhub" + XML_SUFFIX;

    public static final String PDF_FILE_SUFFIX = "_bw" + PDF_SUFFIX;
    /** The suffix for the MODS files.*/
    public static final String MODS_METADATA_SUFFIX = ".mods" + XML_SUFFIX;
    /** The suffix for the MARC files.*/
    public static final String MARC_METADATA_SUFFIX = ".marc" + XML_SUFFIX;

    /** The suffix for a file containing an error.*/
    public static final String ERROR_SUFFIX = ".error";

    /** The XPATH for extracting the Identifier from the pubhub Book xml file.*/
    public static final String XPATH_FIND_IDENTIFIER = "/*[local-name()='Book']/*[local-name()='Identifier']/text()";
    /** The XPATH for extracting the IdentifierType from the pubhub Book xml file.*/
    public static final String XPATH_FIND_IDENTIFIER_TYPE = "/*[local-name()='Book']/*[local-name()='IdentifierType']/text()";

    /** The XPATH for extracting the Identifier from the pubhub Book xml file.*/
    public static final String XPATH_FIND_YEAR = "/*[local-name()='mods']/*[local-name()='originInfo']/*[local-name()='dateIssued']/text()";
    /** The XPATH for extracting the IdentifierType from the pubhub Book xml file.*/
    public static final String XPATH_FIND_YEAR_TYPE = "/*[local-name()='Book']/*[local-name()='IdentifierType']/text()";

}
