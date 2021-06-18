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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static dk.kb.provide_dod_info.Constants.*;
import static dk.kb.provide_dod_info.Constants.ExtractInfo.*;
//import static dk.kb.provide_dod_info.Constants.XPATH_FIND_IDENTIFIER_TYPE;
//import static dk.kb.provide_dod_info.Constants.XP_MARC_FIND_AUTHOR;
//import static dk.kb.provide_dod_info.Constants.XP_MARC_FIND_PUBLISHER;
//import static dk.kb.provide_dod_info.Constants.XP_MARC_FIND_PUBPLACE;
//import static dk.kb.provide_dod_info.Constants.XP_MARC_FIND_TITLE;
//import static dk.kb.provide_dod_info.Constants.XP_MARC_FIND_YEAR;
//import static dk.kb.provide_dod_info.Constants.releaseYear;
//import static dk.kb.provide_dod_info.Constants.ExtractInfo.YEAR;
//import static dk.kb.provide_dod_info.Constants.ExtractInfo.AUTHOR;
//import static dk.kb.provide_dod_info.Constants.ExtractInfo.TITLE;
//import static dk.kb.provide_dod_info.Constants.ExtractInfo.PUBPLACE;
//import static dk.kb.provide_dod_info.Constants.ExtractInfo.PUBLISHER;

// todo: update description
/**
 * The AlmaRetriever
 * It will iterate through all the books - both E-books and Audio books.
 * It will extract the ISBN number from the Publizon metadata file, then use this ISBN to retrieve the
 * MODS from Alma.
 * The result of the Alma extract -whether it succeeded or failed- is saved to an excel-file
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
    private int cutYear;

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

    /**
     * Retrieve Alma metadata for all books; both E-books and Audio books.
     * If the e-book package base directory and the audio book package base directory are the same, then they
     * are only traversed once.
     * @param workbook The Excel workbook
     */
//    public void retrieveAlmaMetadataForBooks(XSSFWorkbook workbook) {
//
//        Map<String, Object[]> data = new TreeMap<>();
//        row++;
//        data.put(String.valueOf(row), new Object[] {"ISBN", "DESCRIPTION"});
//
//        traverseBooksInFolder(conf.getEbookOutputDir(), data);
//        if(conf.getEbookOutputDir().getAbsolutePath().equals(conf.getAudioOutputDir().getAbsolutePath())) {
//            log.debug("Ebooks and Audio books have same base-dir.");
//        } else {
//            traverseBooksInFolder(conf.getAudioOutputDir(), data);
//        }
//
//        XSSFSheet sheet = workbook.createSheet("Alma results");
//        populateSheet(sheet, data);
//        setWorkbookFormats(workbook, sheet);
//
//    }

    public void retrieveAlmaMetadataForFiles(XSSFWorkbook workbook) {
        /* */

        Map<String, Object[]> data = new TreeMap<>();
        row++;
        data.put(String.valueOf(row), new Object[] {"Barcode", "AlmaExtract", "Date", "Place", "Author", "Publisher",
                "Classification", "Title"});

        traverseFilesInFolder(conf.getCorpusOrigDir(), data);

        XSSFSheet sheet = workbook.createSheet("Alma results");
        ExcelUtils.populateSheet(sheet, data);
        ExcelUtils.setWorkbookFormats(workbook, sheet);
    }

    /**
     * Retrieves the  number for a book.
     * Uses the directory for the packaged book to locate the already retrieved Publizon metadata file,
     * and then extracts the ISBN number from that file.
     * Will return null if it  to get the Publizon metadata file, if it  to extract the ISBN,
     * or if the identifier is not of the type ISBN.
     * @param file The directory for a packaged book, which should contain the publizon metadata.
     * @return The ISBN number, or null if no ISBN could be found.
     */

    protected String getDataFromXml(File file, Constants.ExtractInfo extractInfo) {
        File barcodeMetadataFile = FileUtils.getExistingFile(file.toString()); //   new File(dir, barcode + Constants.MARC_METADATA_SUFFIX);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(barcodeMetadataFile);
            XPath xpath = xPathfactory.newXPath();
            String res = "";
            switch (extractInfo) {
                case YEAR:
                    XPathExpression issuedYearXpath = xpath.compile(XP_MARC_FIND_YEAR);
                    String  rYr = (String) issuedYearXpath.evaluate(doc, XPathConstants.STRING);
                    releaseYear = rYr.substring(7,11);
                    res = releaseYear;
                    break;
                case AUTHOR:
                    XPathExpression authorXpath = xpath.compile(XP_MARC_FIND_AUTHOR);
                    String author = (String) authorXpath.evaluate(doc, XPathConstants.STRING);
                    if (StringUtils.isNotEmpty(author)) {
                        res = author;
                    } else {res = "N/A";}
                    break;
                case TITLE:
                    XPathExpression titleXpath = xpath.compile(XP_MARC_FIND_TITLE);
                    res = (String) titleXpath.evaluate(doc, XPathConstants.STRING);
                    break;
                case PUBPLACE:
                    XPathExpression pubPlaceXpath = xpath.compile(XP_MARC_FIND_PUBPLACE);
                    String pubPlace = (String) pubPlaceXpath.evaluate(doc, XPathConstants.STRING);
                    if (StringUtils.isNotEmpty(pubPlace)) {
                        res = pubPlace;
                    } else { res = "N/A";}
                    break;
                case PUBLISHER:
                    XPathExpression publisherXpath = xpath.compile(XP_MARC_FIND_PUBLISHER);
                    String publisher = (String) publisherXpath.evaluate(doc, XPathConstants.STRING);
                    if (StringUtils.isNotEmpty(publisher)) {
                        res = publisher;
                    } else { res = "N/A";}
                    break;
                case CLASSIFICATION:
                    XPathExpression classificationXpath = xpath.compile(XP_MARC_FIND_CLASSIFICATION);
                    String classification = (String) classificationXpath.evaluate(doc, XPathConstants.STRING);
                    if (StringUtils.isNotEmpty(classification)) {
                        res = classification;
                    } else { res = "N/A";}
                    break;
            }

            return res;

        } catch (Exception e) {
            log.warn("Could not extract {} from the file '{} '. Returning a null", extractInfo.name(), barcodeMetadataFile, e);
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

    protected String getBarcode(File file) {
        String fileName = file.getName();
        String barcode;
        try {
            barcode = fileName.replaceAll("-bw.pdf", "").replaceAll(".marc.xml", "");
//            barcode = fileName.substring(0, fileName.indexOf("-bw")); //todo: must be sorted to have only correct .pdf-files

        } catch (Exception e) {
            log.debug("Wrong file format. Barcode could not be retrieved, returning 'null'");
            e.printStackTrace();
            return null;
        }

        return barcode;
    }


    /**
     * Traverses the books in the base directory to retrieve the Alma metadata.
//     * @param baseBookDir The base directory for the books (either E-books or Audio books).
     */
//    protected void traverseBooksInFolder(File baseBookDir, Map<String, Object[]> data) {
//        File[] files = baseBookDir.listFiles();
//        if(files == null) {
//            row++;
//            data.put(String.valueOf(row), new Object[] {"", "No books to retrieve and get Alma metadata for in this directory: "
//                + baseBookDir.getAbsolutePath()});
//            log.warn("No books to retrieve and transform Alma metadata for within the directory: "
//                + baseBookDir.getAbsolutePath());
//        } else {
//            for(File dir : files) {
//                retrieveMetadataForBook(dir, data);
//            }
//        }
//    }

    /**
     * Traverses the files in the base directory to retrieve the Alma metadata.
     * @param dir The base directory for the pdf-files.
     */
    private void traverseFilesInFolder(File dir, Map<String, Object[]> data) {

        FilenameFilter filter = (f, name) -> {
            return name.endsWith(".pdf");
        };
        File[] files = dir.listFiles(filter);
        if(files == null) {
            row++;
            data.put(String.valueOf(row), new Object[] {"", "No files to retrieve and get Alma metadata for in this directory: "
                + dir.getAbsolutePath()});
            log.warn("No files to retrieve and transform Alma metadata for within the directory: "
                + dir.getAbsolutePath());
        } else {
            for(File file : files) {
                String barcode = getBarcode(file);
                if (StringUtils.isNotEmpty(barcode)){
                    retrieveMetadataForBarcode(dir, barcode, data);
                    // Create the textfile from the pdf
                    if (Integer.parseInt(releaseYear) < cutYear) {
                        UxCmdUtils.execCmd("pdftotext", file.toString(), conf.getOutDir().getAbsolutePath()
                                + "/" + barcode + ".txt");
                    }
                }

            }
        }
    }

    /**
     * Retrieve the metadata for a given barcode.
     * It retrieves the Alma metadata in MARC/MODS put the result in outDir from Yaml configuration file.
     * @param dir The directory, where the metadata-file will be placed.
     */
    protected void retrieveMetadataForBarcode(File dir, String barcode, Map<String, Object[]> data) {
        try {
            File metadataFile = new File(conf.getOutDir(), barcode + Constants.MARC_METADATA_SUFFIX);
            getAlmaMetadataForBarcode(barcode, metadataFile, data);
        } catch (Exception e) {
            log.info("Non-critical failure while trying to retrieve the Alma metadata for the book directory '"
                + dir.getAbsolutePath() + "'", e);
        }
    }


    /**
     * Retrieve the metadata for a given book.
     * It retrieves the Alma metadata in MODS put the result in outDir from Yaml configuration file.
     * @param dir The book package directory, where the Publizon metadata already is placed.
     */
//    protected void retrieveMetadataForBook(File dir, Map<String, Object[]> data) {
//        try {
//            File modsMetadata = new File(conf.getOutDir(), dir.getName() + Constants.MODS_METADATA_SUFFIX);
//            String isbn = getIsbn(dir);
//            if(isbn == null) {
//                row++;
//                data.put(String.valueOf(row), new Object[] {"", "No metadata file was found, could not retrieve an ISBN from "+dir.getAbsolutePath()});
//                    log.debug("Could not retrieve a ISBN or GTIN from '" + dir.getAbsolutePath() + "'.");
//                return;
//            }
//
//            getAlmaMetadata(isbn, modsMetadata, data);
//        } catch (Exception e) {
//            log.info("Non-critical failure while trying to retrieve the Alma metadata for the book directory '"
//                + dir.getAbsolutePath() + "'", e);
//        }
//    }

    /**
     * Retrieves the Alma  record metadata file for a given barcode.
     * "OK" is written in the excel sheet if success and data is added.
     * A fail message is written in the excel sheet if no data is retrieved.
     * @param barcode The barcode for The Item, whose metadata record will be retrieved.
     * @param xmlFile The output file where the MODS/MARC will be placed.
     * @throws IOException If it somehow fails to retrieve or write the output file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void getAlmaMetadataForBarcode(String barcode, File xmlFile, Map<String, Object[]> data) throws IOException {
        try (OutputStream out = new FileOutputStream(xmlFile)) {
            // Retrieve metadata for barcode and put it in .xml-file
            almaMetadataRetriever.retrieveMetadataForBarcode(barcode, out); // HERE marc/mods.xml-file IS MADE

            String year = getDataFromXml(xmlFile, YEAR );
            if (Integer.parseInt(year) < cutYear) {
                row++;
                // todo: extract data below and add correct values, ongoing...
//
                String author = getDataFromXml(xmlFile, AUTHOR);
                String title = getDataFromXml(xmlFile, TITLE);
                String pubPlace = getDataFromXml(xmlFile, PUBPLACE);
                String publisher = getDataFromXml(xmlFile, PUBLISHER);
                String classification = getDataFromXml(xmlFile, CLASSIFICATION);
                // 653
                data.put(String.valueOf(row), new Object[]{barcode, "OK", year, pubPlace, author, publisher,
                        classification, title});
                out.flush();


            }
        } finally {
            if (xmlFile.exists() && (Integer.parseInt(releaseYear) > 1881)){
                xmlFile.delete();
            }
            if(xmlFile.exists() && xmlFile.length() == 0) {
                xmlFile.delete();
                row++;
                data.put(String.valueOf(row), new Object[] {barcode, "No marc data retrieved from Alma."});
                log.info("No marc data retrieved for Barcode: {}", barcode);
            }
        }
    }

    /**
     * Retrieves the Alma MODS record metadata file for a given ISBN number.
     * "OK" is written in the excel sheet if success.
     * A fail message is written in the excel sheet if no data is retrieved.
     * @param isbn The ISBN number for book, whose metadata record will be retrieved.
     * @param modsFile The output file where the MODS will be placed.
     * @throws IOException If it somehow  to retrieve or write the output file.
     */
//    @SuppressWarnings("ResultOfMethodCallIgnored")
//    protected void getAlmaMetadata(String isbn, File modsFile, Map<String, Object[]> data) throws IOException {
//        try (OutputStream out = new FileOutputStream(modsFile)) {
//            almaMetadataRetriever.retrieveMetadataForISBN(isbn, out);
//            row++;
//            data.put(String.valueOf(row), new Object[] {isbn, "OK, retrieved metadata from Alma. " + "Alma returned "
//                + AlmaMetadataRetriever.getNumRes() + " file(s)" });
//            out.flush();
//        } finally {
//            if(modsFile.exists() && modsFile.length() == 0) {
//                modsFile.delete();
//                row++;
//                data.put(String.valueOf(row), new Object[] {isbn, "No mods data retrieved from Alma. " + "Alma found "
//                    + AlmaMetadataRetriever.getNumRes() + " files"});
//                log.info("No mods data retrieved for Barcode: {}", isbn);
//            }
////            modsFile.delete();
//        }
//    }




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
            log.info("Non-critical failure while trying to retrieve the Alma metadata for the book directory '"
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
