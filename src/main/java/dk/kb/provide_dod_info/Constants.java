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

    /** The XPATH for extracting the dateIssued from the generated alma mods.xml */
    public static final String XP_MODS_FIND_YEAR = "/*[local-name()='mods']/*[local-name()='originInfo']/*[local-name()='dateIssued']/text()";
    // ovenstående kan forkortes til "/mods/originInfo/dateIssued/text()"

    /**  The XPATH for extracting the dateIssued from the generated alma marc.xml*/
    public static final String XP_MARC_FIND_YEAR = "recordData/record/controlfield[@tag='008']/text()"; //

    /**  The XPATH for extracting the link to the electronic edition from the alma metadata xml*/
    public static final String XPATH_LINK_TO_E_EDITION = "/*[local-name()='searchRetrieveResponse']" +
            "/*[local-name()='records']/*[local-name()='record']/*[local-name()='recordData']/*[local-name()='record']" +
            "/*[local-name()='datafield'][@tag='856']/*[local-name()='subfield'][@code='u']/text()";

    public static final String XPATH_LINK_TO_ECOLLECTION = "/*[local-name()='searchRetrieveResponse']" +
            "/*[local-name()='records']/*[local-name()='record']/*[local-name()='recordData']/*[local-name()='record']" +
            "/*[local-name()='datafield'][@tag='999']/*[local-name()='subfield'][@code='a']/text()";

    // Looks like SRU does not retrieve 900 fields anymore (2024-04-15)
//    public static final String XP_MARC_FIND_ECOLLECTION999 = "recordData/record/datafield[@tag='999']/subfield[@code='a']/text()";
//    public static final String XP_MARC_FIND_ECOLLECTION997 = "recordData/record/datafield[@tag='997']/subfield[@code='a']/text()";

    /** The XPATH for the number of records.
            * Using '*' as wildcard for the namespace.*/
    public static final String XPATH_NUM_RESULTS = "/*[local-name()='searchRetrieveResponse']/*[local-name()='numberOfRecords']/text()";

    /** The XPATH for extracting the Author from the generated alma mods.xml*/
    public static final String XP_MODS_FIND_AUTHOR = "/*[local-name()='mods']/*[local-name()='name']/*[local-name()='namePart']/text()";
    // "/mods/name[@type='personal']/namePart/text()";
    /** The XPATH for extracting the Author from the generated alma marc.xml*/
    public static final String XP_MARC_FIND_AUTHOR = "recordData/record/datafield[@tag='100']/subfield[@code='a']/text()";
    /** The XPATH for extracting the Title from the generated alma marc.xml*/
    public static final String XP_MARC_FIND_TITLE = "recordData/record/datafield[@tag='245']/subfield[@code='a']/text()";
    /** The XPATH for extracting the Place of publication from the generated alma marc.xml*/
    public static final String XP_MARC_FIND_PUBPLACE = "recordData/record/datafield[@tag='260']/subfield[@code='a']/text()";
    /** The XPATH for extracting the Publisher from the generated alma marc.xml*/
    public static final String XP_MARC_FIND_PUBLISHER = "recordData/record/datafield[@tag='260']/subfield[@code='b']/text()";
    /** The XPATH for extracting the Classification from the generated alma marc.xml*/
    public static final String XP_MARC_FIND_CLASSIFICATION = "recordData/record/datafield[@tag='084']/subfield[@code='o']/text()";

    public static final String OK = "OK";
    public static final String NOK = "NOK";
    public static final String SHEETNAME = "Alma results";

    public enum ExtractInfo {
        YEAR, AUTHOR, TITLE, PUBPLACE, PUBLISHER, CLASSIFICATION, E_COLLECTION
    }


}
