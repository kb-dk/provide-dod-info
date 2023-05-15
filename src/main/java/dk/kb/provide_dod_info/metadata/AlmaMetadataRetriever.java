package dk.kb.provide_dod_info.metadata;

import dk.kb.provide_dod_info.HttpClient;
import dk.kb.provide_dod_info.config.Configuration;
import dk.kb.provide_dod_info.exception.ArgumentCheck;
import static dk.kb.provide_dod_info.Constants.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Alma Metadata Retriever.
 * Makes a direct barcode search in Alma and extracts the MARC records.
 * It should create MARC retrieval URLs like the following:
 * https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&startRecord=1&maximumRecords=2&recordSchema=mods&query=isbn=$ISBN
 * https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&startRecord=1&maximumRecords=2&recordSchema=marcxml&query=alma.barcode=$BARCODE
 * https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&startRecord=1&maximumRecords=1&recordSchema=marcxml&query=alma.packageName=$ELECTRONIC_COLLECTION
 * To get all query options use:
 * <a href="https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=explain">...</a>
 */
public class AlmaMetadataRetriever {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlmaMetadataRetriever.class);

    /** The search range parameters for retrieving records from Alma. */
    protected static final String ALMA_SEARCH_RANGE = "startRecord=1&maximumRecords=2&";
    protected static final String ALMA_START_RECORD = "startRecord=";
    protected static final String ALMA_MAXIMUM_RECORDS = "&maximumRecords=1&";

    /** The schema parameters for retrieving MODS records from Alma.*/
    protected static final String ALMA_SCHEMA_MODS = "recordSchema=mods&";
    protected static final String ALMA_SCHEMA_MARCXML = "recordSchema=marcxml&";

    /** The base query for performing ISBN search in Alma.*/
    protected static final String ALMA_QUERY_ISBN = "query=isbn=";
    protected static final String ALMA_QUERY_BARCODE = "query=alma.barcode=";
    protected static final String ALMA_QUERY_ECOLLECTION = "query=alma.packageName=";


    /** The XPATH for the MODS record.
     * Using '*' as wildcard for the namespace.*/
    protected static final String XPATH_MODS_RECORD = "/*[local-name()='searchRetrieveResponse']/*[local-name()='records']/*[local-name()='record']/*[local-name()='recordData']/*[local-name()='mods']";
    /** The XPATH for the MARC record.
     * Using '*' as wildcard for the namespace.*/
    protected static final String XPATH_MARC_RECORD = "/*[local-name()='searchRetrieveResponse']/*[local-name()='records']/*[local-name()='record']/*[local-name()='recordData']";



    /** The configuration.*/
    protected final Configuration conf;
    /** The HTTP client for making the HTTP Get operations towards the Alma server.*/
    protected final HttpClient httpClient;

    /** The document builder factory.*/
    protected final DocumentBuilderFactory documentBuilderFactory;
    /** The XPath factory.*/
    protected final XPathFactory xPathFactory;

    public static String numRes;

    /**
     * Constructor.
     * @param configuration The configurations regarding dealing with Alma.
     * @param httpClient The HTTP client for performing the HTTP Get operations.
     */
    public AlmaMetadataRetriever(Configuration configuration, HttpClient httpClient) {
        ArgumentCheck.checkNotNull(configuration, "Configuration configuration");
        ArgumentCheck.checkNotNull(httpClient, "HttpClient httpClient");
        this.conf= configuration;
        this.httpClient = httpClient;
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        xPathFactory = XPathFactory.newInstance();
        numRes = "";
    }

    /**
     * Retrieves the MARC metadata for a given barcode from a physical Alma record.
     * @param barcode The ID to retrieve the Alma metadata for.
     * @param out The output stream, where the MARC metadata from Alma will be written.
     */
    public void retrieveMetadataForBarcode(String barcode, OutputStream out) {
        ArgumentCheck.checkNotNullOrEmpty(barcode, "String barcode");
        ArgumentCheck.checkNotNull(out, "OutputStream out");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        retrieveAlmaMetadataBarcode(barcode, byteArrayOutputStream); //MARC

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

//        extractModsFromAlma(byteArrayInputStream, out);
        extractMarcFromAlma(byteArrayInputStream, out);
    }

    public ByteArrayInputStream retrieveMetadataForECollection(String eCollection, int recNo, OutputStream out) {
        ArgumentCheck.checkNotNullOrEmpty(eCollection, "String eCollection");
        ArgumentCheck.checkNotNull(out, "OutputStream out");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        retrieveAlmaMetadataECollection(eCollection, recNo, byteArrayOutputStream);

        return  new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    private void retrieveAlmaMetadataECollection(String eCollection, int recNo, OutputStream out) {
        try {
            String rN = String.valueOf(recNo);
            String requestUrl = conf.getAlmaSruSearch() + ALMA_START_RECORD + rN + ALMA_MAXIMUM_RECORDS
                    + ALMA_SCHEMA_MARCXML + ALMA_QUERY_ECOLLECTION + eCollection;
            httpClient.retrieveUrlContent(requestUrl, out);
        } catch (IOException e) {
            throw new IllegalStateException("Could not download the metadata for set '" + eCollection + "'", e);
        }
    }

    /**
     *  Extract the value(s) of a specific XPATH
     * @param almaInput the Alma xml-input stream
     * @param xPath The path to the wanted Node
     * @return A list containing the values of the XPATH
     */
    public List<String> extractXpathValue(InputStream almaInput, String xPath ){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(almaInput);
            XPath xpath = xPathFactory.newXPath();
            NodeList nodeList = (NodeList) xpath.compile(xPath).evaluate(doc, XPathConstants.NODESET);
            List<String> linkList = new ArrayList<>();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nLink = nodeList.item(i);
                String link = nLink.getNodeValue();
                linkList.add(link);
            }
            return linkList;

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            throw new IllegalStateException("Could not get a value from requested XPATH ", e);
        }
    }

//    /**
//     * Retrieves the MODS metadata for a given ISBN from Alma.
//     * @param isbn The ID to retrieve the Alma metadata for.
//     * @param out The output stream, where the MODS metadata from Alma will be written.
//     */
//    public void retrieveMetadataForISBN(String isbn, OutputStream out) {
//        ArgumentCheck.checkNotNullOrEmpty(isbn, "String isbn");
//        ArgumentCheck.checkNotNull(out, "OutputStream out");
//
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        retrieveAlmaMetadata(isbn, byteArrayOutputStream);
//
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
//        extractModsFromAlma(byteArrayInputStream, out);
//    }

    /**
     * Retrieves the Alma metadata for the given barcode and writes it to the output stream.
     * @param barcode The barcode for the record to retrieve metadata for.
     * @param out Output stream where the retrieved metadata is written.
     */
    protected void retrieveAlmaMetadataBarcode(String barcode, OutputStream out) {
        log.debug("Retrieving Alma metadata for Barcode: " + barcode);

        try {
            String requestUrl = conf.getAlmaSruSearch() + ALMA_SEARCH_RANGE + ALMA_SCHEMA_MARCXML /*ALMA_SCHEMA_MODS*/ + ALMA_QUERY_BARCODE + barcode;
            httpClient.retrieveUrlContent(requestUrl, out);
        } catch (IOException e) {
            throw new IllegalStateException("Could not download the metadata for set '" + barcode + "'", e);
        }
    }
//    /**
//     * Retrieves the Alma metadata for the given ISBN and writes it to the output stream.
//     * @param isbn The ISBN number for the record to retrieve metadata for.
//     * @param out Output stream where the retrieved metadata is written.
//     */
//    protected void retrieveAlmaMetadata(String isbn, OutputStream out) {
//        log.debug("Retrieving Alma metadata for ISBN: " + isbn);
//
//        try {
//            String requestUrl = conf.getAlmaSruSearch() + ALMA_SEARCH_RANGE + ALMA_SCHEMA_MODS + ALMA_QUERY_ISBN + isbn;
//             httpClient.retrieveUrlContent(requestUrl, out);
//        } catch (IOException e) {
//            throw new IllegalStateException("Could not download the metadata for set '" + isbn + "'", e);
//        }
//    }

    /**
     * Extracts the MARC record from the Alma record.
     * @param almaInput The input stream with the Alma metadata.
     * @param marcOutput The output stream with the MARC metadata.
     */
    protected void extractMarcFromAlma(InputStream almaInput, OutputStream marcOutput) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(almaInput);
            XPath xpath = xPathFactory.newXPath();

            if(conf.getElectronicCollection() == null) {
                String numResults = (String) xpath.evaluate(XPATH_NUM_RESULTS, doc, XPathConstants.STRING);
                numRes = numResults;
                if(!"1".equals(numResults)) {
                    throw new IllegalStateException("Did not receive exactly 1 result from Alma. Received: " + numResults);
                }
            }
            XPathExpression marcResultsXpath = xpath.compile(XPATH_MARC_RECORD);
            NodeList marcResults = (NodeList) marcResultsXpath.evaluate(doc, XPathConstants.NODESET);

            Node marc = marcResults.item(0);
            System.setProperty("line.separator", "\n");
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Turn the node into a string
            transformer.transform(new DOMSource(marc), new StreamResult(marcOutput));
        } catch (Exception e) {
            throw new IllegalStateException("Could not extract the MARC record", e);
        }
    }

    /**
     * Extracts the MODS record from the Alma record.
     * @param almaInput The input stream with the Alma metadata.
     * @param modsOutput The output stream with the MODS metadata.
     */
    protected void extractModsFromAlma(InputStream almaInput, OutputStream modsOutput) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(almaInput);
            XPath xpath = xPathFactory.newXPath();

            String numResults = (String) xpath.evaluate(XPATH_NUM_RESULTS, doc, XPathConstants.STRING);
            numRes = numResults;
            if(!"1".equals(numResults)) {
                throw new IllegalStateException("Did not receive exactly 1 result from Alma. Received: " + numResults);
            }

            XPathExpression modsResultsXpath = xpath.compile(XPATH_MODS_RECORD);
            NodeList modsResults = (NodeList) modsResultsXpath.evaluate(doc, XPathConstants.NODESET);

            Node mods = modsResults.item(0);
            System.setProperty("line.separator", "\n");
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Turn the node into a string
            transformer.transform(new DOMSource(mods), new StreamResult(modsOutput));
        } catch (Exception e) {
            throw new IllegalStateException("Could not extract the MODS record", e);
        }
    }

    public static String getNumRes() {
        return numRes;
    }
}
