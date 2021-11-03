package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.config.Configuration;
import dk.kb.provide_dod_info.exception.ArgumentCheck;
import dk.kb.provide_dod_info.metadata.AlmaMetadataRetriever;
import dk.kb.provide_dod_info.metadata.MetadataValidator;
import dk.kb.provide_dod_info.utils.ExcelUtils;
import dk.kb.provide_dod_info.utils.FileUtils;
import dk.kb.provide_dod_info.utils.UxCmdUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static dk.kb.provide_dod_info.Constants.*;
import static dk.kb.provide_dod_info.Constants.ExtractInfo.*;

/**
 * The AlmaRetriever
 * It will iterate through the received directory containing digitized DOD pdf files, extract the barcode from the
 * wanted pdf-files (ends with -color.pdf). (-bw->-color)
 * It will then use this barcode to retrieve the MARC data for the related record from Alma in a xml file.
 * The result of the Alma extract -whether it succeeded or failed- is saved to an excel-file together with specific
 * metadata from the MARC xml file above.
 * A txt file is extracted from pdf-files with pdftotext
 */
public class AlmaRetriever {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlmaRetriever.class);
    /** The configuration.*/
    protected final Configuration conf;
    /** The metadata retriever for the Alma metadata.*/
    protected final AlmaMetadataRetriever almaMetadataRetriever;
    /** The metadata validator.*/
    protected final MetadataValidator validator;
    /** The document builder factory.*/
    protected final DocumentBuilderFactory factory;
    /** The XPath factory.*/
    protected final XPathFactory xPathfactory;
    /** The row used in the excel-sheet*/
    private int row;
    /** The value containing the 140 years cut for records without Copyrights*/
    private final int cutYear;

    private String releaseYear;

    /**
     * Constructor.
     * @param conf The configuration.
     * @param almaMetadataRetriever The retriever of Alma metadata.
     */
    public AlmaRetriever(Configuration conf, AlmaMetadataRetriever almaMetadataRetriever) {
        ArgumentCheck.checkNotNull(conf, "Configuration conf");
        ArgumentCheck.checkNotNull(almaMetadataRetriever, "AlmaMetadataRetriever almaMetadataRetriever");
        this.conf = conf;
        this.almaMetadataRetriever = almaMetadataRetriever;
        this.factory = DocumentBuilderFactory.newInstance();
        this.xPathfactory = XPathFactory.newInstance();
        this.validator = new MetadataValidator();
        this.row = 0;
        this.cutYear = conf.getCutYear();
    }

    public void retrieveAlmaMetadataForFiles(XSSFWorkbook workbook) {
        /* */
        Map<String, Object[]> data = new TreeMap<>();
        row++;
        // Head row in excel:
        data.put(String.valueOf(row), new Object[] {"Barcode", "Alma", "Year", "Place", "Author", "Publisher",
                "Classification", "Title"});

        traverseFilesInFolder(conf.getCorpusOrigDir(), data);

        XSSFSheet sheet = workbook.createSheet(SHEETNAME);
        ExcelUtils.populateSheet(sheet, data);
//        ExcelUtils.setWorkbookFormats(workbook);
        ExcelUtils.createExcel(workbook, conf);

    }

    /**
     * Retrieves specific metadata for a barcode (Bibliographic post of an item).
     * Uses the file with already retrieved marc metadata to extract the wanted
     * metadata from that file.
     * Will return "N/A"" if data is not available
     * @param file The previously generated file, which should contain the retrieved metadata.
     * @return The metadata, or "N/A" if no metadata could be found.
     */
    protected String getDataFromXml(File file, Constants.ExtractInfo extractInfo) {
        File barcodeMetadataFile = FileUtils.getExistingFile(file.toString());
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(barcodeMetadataFile);
            XPath xpath = xPathfactory.newXPath();
            String res = "";
            switch (extractInfo) {
                case YEAR:
                    log.trace("Extracting Year");
                    XPathExpression issuedYearXpath = xpath.compile(XP_MARC_FIND_YEAR);
                    String  rYr = (String) issuedYearXpath.evaluate(doc, XPathConstants.STRING);
                    releaseYear = rYr.substring(7,11);
                    if (!isNumeric(releaseYear) || "0000".equals(releaseYear)){
                        throw new IllegalStateException();
                    }
                    res = releaseYear;
                    break;
                case AUTHOR:
                    log.trace("Extracting Author");
                    XPathExpression authorXpath = xpath.compile(XP_MARC_FIND_AUTHOR);
                    String author = (String) authorXpath.evaluate(doc, XPathConstants.STRING);
                    if (StringUtils.isNotEmpty(author)) {
                        res = author;
                    } else {res = "N/A";}
                    break;
                case TITLE:
                    log.trace("Extracting Title");
                    XPathExpression titleXpath = xpath.compile(XP_MARC_FIND_TITLE);
                    String title = (String) titleXpath.evaluate(doc, XPathConstants.STRING);
                    if (StringUtils.isNotEmpty(title)){
                        res = title.replaceAll("[\\[\\]:/]", "");
                    } else { res = "N/A";}
                    break;
                case PUBPLACE:
                    log.trace("Extracting Place of Publication");
                    XPathExpression pubPlaceXpath = xpath.compile(XP_MARC_FIND_PUBPLACE);
                    String pubPlace = (String) pubPlaceXpath.evaluate(doc, XPathConstants.STRING);
                    if (StringUtils.isNotEmpty(pubPlace)) {
                        res = pubPlace.replaceAll("[\\[\\]:;,]", "");
                    } else { res = "N/A";}
                    break;
                case PUBLISHER:
                    log.trace("Extracting Publisher");
                    XPathExpression publisherXpath = xpath.compile(XP_MARC_FIND_PUBLISHER);
                    String publisher = (String) publisherXpath.evaluate(doc, XPathConstants.STRING);
                    if (StringUtils.isNotEmpty(publisher)) {
                        res = publisher.replaceAll("[ \\]\\[:,]", "");
                    } else { res = "N/A";}
                    break;
                case CLASSIFICATION:
                    log.trace("Extracting Classification");
                    XPathExpression classificationXpath = xpath.compile(XP_MARC_FIND_CLASSIFICATION);
                    String classification = (String) classificationXpath.evaluate(doc, XPathConstants.STRING);
                    if (StringUtils.isNotEmpty(classification)) {
                        res = classification;
                    } else { res = "N/A";}
                    break;
            }
            log.trace("Returning data: {}", res);
            return res;

        }catch (IllegalStateException e){
            log.warn("Year of release was not found!");
            releaseYear = null;
            return null;
        }
        catch (Exception e) {
            log.warn("Could not extract '{}' from the file '{}'. Returning a null", extractInfo.name(), barcodeMetadataFile, e);
            return null;
        }
    }

    /**
     * Retrieves the ISBN number for a book.
     * Uses the directory for the packaged book to locate the already retrieved Publizon metadata file,
     * and then extracts the ISBN number from that file.
     * Will return null if it  to get the Publizon metadata file, if it  to extract the ISBN,
     * or if the identifier is not of the type ISBN.
     * @param dir The directory for a packaged book, which should contain the publizon metadata.
     * @return The ISBN number, or null if no ISBN could be found.
     */
    protected String getIsbn(File dir) {
        File pubhubMetadataFile = new File(dir, dir.getName() + Constants.PUBHUB_METADATA_SUFFIX);
        if(!pubhubMetadataFile.isFile()) {
            log.warn("No pubhub metadata file for '" + dir.getName() + "', thus cannot extract ISBN. "
                + "Returning a null.");
            return null;
        }
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pubhubMetadataFile);
            XPath xpath = xPathfactory.newXPath();
            XPathExpression identifierXpath = xpath.compile(XPATH_FIND_IDENTIFIER);
            XPathExpression identifierTypeXpath = xpath.compile(XPATH_FIND_IDENTIFIER_TYPE);
            String idType = (String) identifierTypeXpath.evaluate(doc, XPathConstants.STRING);
            if(!idType.startsWith("ISBN") && !idType.startsWith("GTIN13")) {
                log.info("Not an ISBN or GTIN13 type of identifier. Found: '" + idType + "'. Returning a null.");
                return null;
            }
            return (String) identifierXpath.evaluate(doc, XPathConstants.STRING);
        } catch (Exception e) {
            log.warn("Could not extract the ISBN number from the file '" + pubhubMetadataFile + "'. Returning a null",
                e);
            return null;
        }
    }

    /**
     * Extracts the barcode from the file name
     * @param fileName The file to extract the barcode from
     * @return The barcode
     */
    protected String getBarcode(String fileName) {
        log.debug("Getting barcode.");
        String barcode;
        try {
            barcode = fileName.replaceAll("-color.pdf", "").replaceAll(".pdf", ""); //-bw.pdf->-color.pdf
        }
        catch (Exception e) {
            log.debug("Wrong fileName format. Barcode could not be retrieved, returning 'null'");
            e.printStackTrace();
            return null;
        }

        return barcode;
    }

    /**
     * Traverses the files in the base directory to retrieve the Alma metadata.
     * Generate txt-files from the pdf-files
     * @param dir The base directory for the pdf-files.
     */
    private void traverseFilesInFolder(File dir, Map<String, Object[]> data) {
        FilenameFilter filter = (f, name) -> name.endsWith(".pdf");
        File[] files = dir.listFiles(filter);

        if(files == null ) {
            row++;
            data.put(String.valueOf(row), new Object[] {"", "No files to retrieve and get Alma metadata for in this directory: "
                    + dir.getAbsolutePath()});
            log.warn("No files to retrieve and transform Alma metadata for within the directory: "
                    + dir.getAbsolutePath());
        } else {
            List<String> fileNames = Arrays.stream(files)
                    .map(File::getName)
                    .filter(name -> name.matches("^(?!.*(-bw|_color|_bw|xml)).*$")) // remove unwanted files (-color->-bw)
                    .collect(Collectors.toList());

            for(String fileName : fileNames) {
                String barcode = getBarcode(fileName);
                if (StringUtils.isNotEmpty(barcode)){
                    retrieveMetadataForBarcode(dir, barcode, data, fileName);
                }
            }
        }
    }

    /**
     * Retrieve the metadata for a given barcode.
     * It retrieves the Alma metadata in MARC format, creates an xml file with the data
     *  and puts the xml file in outDir from Yaml configuration file.
     * @param dir The directory, where the metadata-file will be placed.
     */
    protected void retrieveMetadataForBarcode(File dir, String barcode, Map<String, Object[]> data, String  fileName) {
        try {
            File metadataFile = new File(conf.getTempDir(), barcode + Constants.MARC_METADATA_SUFFIX);
            getAlmaMetadataForBarcode(barcode, metadataFile, data, fileName);
        } catch (Exception e) {
            log.info("Failure while trying to retrieve the Alma metadata for the directory '"
                + dir.getAbsolutePath() + "'" + "Barcode: " + barcode, e);
        }
    }

    /**
     * Retrieves the Alma record metadata file for a given barcode.
     * "OK" is written in the excel sheet if success and data is added.
     * A fail message is written in the excel sheet if no data is retrieved.
     * @param barcode The barcode for The Item, whose metadata record will be retrieved.
     * @param xmlFile The output file where the metadata will be placed.
     * @throws IOException If it somehow fails to retrieve or write the output file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void getAlmaMetadataForBarcode(String barcode, File xmlFile, Map<String, Object[]> data, String fileName) throws IOException {
        try (OutputStream out = new FileOutputStream(xmlFile)) {

            // Create BARCODE.marc.xml-file and put retrieved metadata in it
            almaMetadataRetriever.retrieveMetadataForBarcode(barcode, out);
            //Get releaseYear
            getDataFromXml(xmlFile, YEAR );

            if (isNumeric(releaseYear)) {
                if ((Integer.parseInt(releaseYear) < cutYear)) {
                    row++;
                    String author = getDataFromXml(xmlFile, AUTHOR);
                    String title = getDataFromXml(xmlFile, TITLE);
                    String pubPlace = getDataFromXml(xmlFile, PUBPLACE);
                    String publisher = getDataFromXml(xmlFile, PUBLISHER);
                    String classification = getDataFromXml(xmlFile, CLASSIFICATION);
                    try {
                        UxCmdUtils.execCmd("pdftotext "                                        // command
                                + conf.getCorpusOrigDir().getAbsolutePath() + "/" + fileName + " "   // input file
                                + conf.getTempDir().getAbsolutePath() + "/" + barcode + ".txt");     // output file
                    } catch (Exception e) {
                        log.warn("Could not make text file from pdf for: {}\n", fileName);
                                log.trace("Stack: "); e.printStackTrace();
                    }
                    if (FileUtils.checkFileExist(conf.getTempDir().getAbsolutePath() + "/" + barcode + ".txt")) {
                        data.put(String.valueOf(row), new Object[]{barcode, OK, releaseYear, pubPlace, author, publisher,
                                classification, title});
                    }
                }
            }
            out.flush();
        } finally {
            if (isNumeric(releaseYear)) {
                if (xmlFile.exists() && (Integer.parseInt(releaseYear) >= cutYear)) {
                    xmlFile.delete();
                }
            } else {
                if (xmlFile.exists()){
                    xmlFile.delete();
                }
            }
            if(xmlFile.exists() && xmlFile.length() == 0) {
                xmlFile.delete();
                row++;
                data.put(String.valueOf(row), new Object[] {barcode, NOK});
                log.info("No marc data retrieved for Barcode: {}", barcode);
            }
        }
    }

    /**
     * Check if passed String is a numeric value
     * @param strNum string to check
     * @return true if strNum is an integer representation (and not null), otherwise false
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /* Currently unused methods */

    /**
     * Traverses the books in the base directory to retrieve ISBNs.
     * @param baseBookDir The base directory for the books (either E-books or Audio books).
     */
    protected List<String> retrieveIsbnForBooksInFolder(File baseBookDir) {
        File[] files = baseBookDir.listFiles();
        List<String> isbnList = new ArrayList<>();
        if(files == null) {
            log.warn("No books to retrieve and transform Alma metadata for within the directory: "
                + baseBookDir.getAbsolutePath());
        } else {
            for(File dir : files) {
                isbnList.add(getIsbnFromModsFile(dir));
            }
        }
        return isbnList;
    }

    /**
     * Get ISBN for a given book.
     * @param dir The book package directory, where the Publizon metadata already is placed.
     */
    protected String getIsbnFromModsFile(File dir) {
        try {
            String isbn = getIsbn(dir);
            if(isbn == null) {
                log.debug("Could not retrieve a ISBN or GTIN from '" + dir.getAbsolutePath()
                    + "'.");
                return null;
            }
            return isbn;
        } catch (Exception e) {
            log.info("Failure while trying to retrieve the Alma metadata for the book directory '"
                + dir.getAbsolutePath() + "'", e);
        }
        return null;
    }

    /**
     * Checks whether an XML file is valid, and if it is not, then move it to 'XXX.error'.
     * @param xmlFile The XML file to validate.
     */
    protected void handleXmlValidity(File xmlFile) throws IOException {
        if(validator.isValid(xmlFile)) {
            log.debug("Valid MODS!");
        } else {
            log.warn("Invalid MODS! Moving it to error");
            File errorFile = new File(xmlFile.getAbsolutePath() + Constants.ERROR_SUFFIX);
            FileUtils.moveFile(xmlFile, errorFile);
        }
    }
}
