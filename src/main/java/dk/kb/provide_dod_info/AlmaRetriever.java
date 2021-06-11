package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.config.Configuration;
import dk.kb.provide_dod_info.exception.ArgumentCheck;
import dk.kb.provide_dod_info.metadata.AlmaMetadataRetriever;
import dk.kb.provide_dod_info.metadata.MetadataValidator;
import dk.kb.provide_dod_info.utils.FileUtils;
import dk.kb.provide_dod_info.utils.LinuxCmdUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static dk.kb.provide_dod_info.Constants.XPATH_FIND_AUTHOR;
import static dk.kb.provide_dod_info.Constants.XPATH_FIND_IDENTIFIER;
import static dk.kb.provide_dod_info.Constants.XPATH_FIND_IDENTIFIER_TYPE;
import static dk.kb.provide_dod_info.Constants.XPATH_FIND_YEAR;

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
        data.put(String.valueOf(row), new Object[] {"Barcode", "Extract Result", "Date", "Place", "Author", "Title"});

        traverseFilesInFolder(conf.getCorpusOrigDir(), data);

        XSSFSheet sheet = workbook.createSheet("Alma results");
        populateSheet(sheet, data);
        setWorkbookFormats(workbook, sheet);

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

    protected String getDataFromModsXml(File file, String barcode, String data) {
        File barcodeMetadataFile = FileUtils.getExistingFile(file.toString()); //   new File(dir, barcode + Constants.MARC_METADATA_SUFFIX);
//        if(!barcodeMetadataFile.isFile()) {
//            log.warn("No metadata file for '" + barcode + "', thus cannot extract DateIssued. "
//                + "Returning a null.");
//            return null;
//        }
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(barcodeMetadataFile);
            XPath xpath = xPathfactory.newXPath();
            String res = null;
            switch (data) {
                case  "ISBN":
                    XPathExpression identifierXpath = xpath.compile(XPATH_FIND_IDENTIFIER);
                    XPathExpression identifierTypeXpath = xpath.compile(XPATH_FIND_IDENTIFIER_TYPE);
                    String idType = (String) identifierTypeXpath.evaluate(doc, XPathConstants.STRING);
                    if (!idType.startsWith(data) && !idType.startsWith("GTIN13")) {
                        log.info("Not an ISBN or GTIN13 type of identifier. Found: '" + idType + "'. Returning a null.");
                        return null;
                    }
                    res = (String) identifierXpath.evaluate(doc, XPathConstants.STRING);
                    break;

                case "YEAR":
                    XPathExpression issuedYearXpath = xpath.compile(XPATH_FIND_YEAR);
                    String year = (String) issuedYearXpath.evaluate(doc, XPathConstants.STRING);
                    res = year;
                    break;

                case "AUTHOR":
                    XPathExpression authorXpath = xpath.compile(XPATH_FIND_AUTHOR);
                    String author = (String) authorXpath.evaluate(doc, XPathConstants.STRING);
                    res = author;
                    break;
            }


//                    XPathExpression issuedYearTypeXpath = xpath.compile(XPATH_FIND_YEAR_TYPE);
//                    String yearType = (String) issuedYearTypeXpath.evaluate(doc, XPathConstants.STRING);
//                    if (!yearType.startsWith(data)) {
//                        log.info("Not a YEAR type of identifier. Found: '" + yearType + "'. Returning a null.");
//                        return null;
//                    }
//                    res = (String) issuedYearXpath.evaluate(doc, XPathConstants.STRING);




            return res;
        } catch (Exception e) {
            log.warn("Could not extract the 'DateIssued' from the file '" + barcodeMetadataFile + "'. Returning a null",
                e);
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
            barcode = fileName.substring(0, fileName.indexOf("-bw"));
        } catch (Exception e) {
            log.debug("Wrong file format. Barcode could not be retrieved, returning 'null'");
            e.printStackTrace();
            return null;
        }

        return barcode;
    }


    /**
     * Traverses the books in the base directory to retrieve the Alma metadata.
     * @param baseBookDir The base directory for the books (either E-books or Audio books).
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
        File[] files = dir.listFiles();
        if(files == null) {
            row++;
            data.put(String.valueOf(row), new Object[] {"", "No files to retrieve and get Alma metadata for in this directory: "
                + dir.getAbsolutePath()});
            log.warn("No files to retrieve and transform Alma metadata for within the directory: "
                + dir.getAbsolutePath());
        } else {
//            List<String> barcodes = new ArrayList<>();
            for(File file : files) {
                String barcode = getBarcode(file);
//                barcodes.add(barcode);
                if (StringUtils.isNotEmpty(barcode)){
                    // Create the textfile from the pdf
                    LinuxCmdUtil.execCmd("pdftotext", file.toString(), conf.getOutDir().getAbsolutePath() + "/" + barcode + ".txt");

                    retrieveMetadataForBarcode(dir, barcode, data);
                }

            }
        }
    }

    /**
     * Retrieve the metadata for a given barcode.
     * It retrieves the Alma metadata in MARC put the result in outDir from Yaml configuration file.
     * @param dir The book package directory, where the Publizon metadata already is placed.
     */
    protected void retrieveMetadataForBarcode(File dir, String barcode, Map<String, Object[]> data) {
        try {
            File marcMetadataFile = new File(conf.getOutDir(), barcode + Constants.MODS_METADATA_SUFFIX);
            getAlmaMetadataForBarcode(barcode, marcMetadataFile, data);
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
     * Retrieves the Alma MARC record metadata file for a given barcode.
     * "OK" is written in the excel sheet if success and data is added.
     * A fail message is written in the excel sheet if no data is retrieved.
     * @param barcode The barcode for The Item, whose metadata record will be retrieved.
     * @param marcFile The output file where the MODS will be placed.
     * @throws IOException If it somehow fails to retrieve or write the output file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void getAlmaMetadataForBarcode(String barcode, File marcFile, Map<String, Object[]> data) throws IOException {
        try (OutputStream out = new FileOutputStream(marcFile)) {
            almaMetadataRetriever.retrieveMetadataForBarcode(barcode, out); // make marc.xml-file

            String year = getDataFromModsXml(marcFile, barcode, "YEAR");
//            if (Integer.parseInt(year) < 1881) { //todo: make 1881 configurable
                row++;
                // todo: extract data below and add correct values
//                String place = getDataFromMarcXml(place, ?);
                String author = getDataFromModsXml(marcFile, barcode, "AUTHOR");
                data.put(String.valueOf(row), new Object[]{barcode, "OK", year, "testSted" + row,
                    author, "testTitel" + row});

                out.flush();
//            }
        } finally {
            if(marcFile.exists() && marcFile.length() == 0) {
                marcFile.delete();
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

    /**
     * Add the collected data to the excel sheet
     * @param sheet The sheet
     * @param data The data
     */
    private void populateSheet(XSSFSheet sheet, Map<String, Object[]> data) {
        Set<String> keyset = data.keySet();
        int rowNumber = 0;
        for (String key : keyset)
        {
            XSSFRow row = sheet.createRow(rowNumber++);
            Object [] objArr = data.get(key);
            int cellNumber = 0;
            for (Object obj : objArr)
            {
                XSSFCell cell = row.createCell(cellNumber++);
                if(obj instanceof String)
                    cell.setCellValue((String)obj);
                else if(obj instanceof Integer)
                    cell.setCellValue((Integer)obj);
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
        }
    }

    private void setWorkbookFormats(XSSFWorkbook workbook, XSSFSheet sheet) {
        XSSFCellStyle defaultCellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
        defaultCellStyle.setFont(font);
        sheet.setDefaultColumnStyle(0, defaultCellStyle);
        sheet.setDefaultColumnStyle(1, defaultCellStyle);
        sheet.setDefaultColumnStyle(2, defaultCellStyle);
        sheet.setDefaultColumnStyle(3, defaultCellStyle);
        sheet.setDefaultColumnStyle(4, defaultCellStyle);
        sheet.setDefaultColumnStyle(5, defaultCellStyle);

        XSSFCellStyle cellStyleErrorCells = workbook.createCellStyle();
        XSSFFont fontErrorCells = workbook.createFont();
        fontErrorCells.setColor(IndexedColors.RED.getIndex());
        cellStyleErrorCells.setFont(fontErrorCells);
        for (Row row : sheet){
            Cell descriptions = row.getCell(1);
            final String stringCellValue = descriptions.getStringCellValue();
            if (stringCellValue.startsWith("No")){
                descriptions.setCellStyle(cellStyleErrorCells);
            }
        }

        XSSFRow topRow = sheet.getRow(0);
        XSSFCellStyle cellStyleTopRow = workbook.createCellStyle();
        XSSFFont fontTopRow = workbook.createFont();
        fontTopRow.setBold(true);
        cellStyleTopRow.setFont(fontTopRow);
        cellStyleTopRow.setBorderBottom(BorderStyle.MEDIUM);
        topRow.setRowStyle(cellStyleTopRow);

    }



    /* Currently unused methods */
    /**
     * Get list of ISBN for all books; both E-books and Audio books.
     * Will not retrieve the metadata, if it has already been retrieved.
     *
     * If the e-book package base directory and the audio book package base directory are the same, then they
     * are only traversed once.
     */
//    protected List<String> getIsbnListFromPubHub() {
//        List<String> result;
//        result = retrieveIsbnForBooksInFolder(conf.getEbookOutputDir());
//        if(conf.getEbookOutputDir().getAbsolutePath().equals(conf.getAudioOutputDir().getAbsolutePath())) {
//            log.debug("Ebooks and Audio books have same base-dir.");
//        } else {
//            List<String> audioIsbns;
//            audioIsbns = retrieveIsbnForBooksInFolder(conf.getAudioOutputDir());
//            result.addAll(audioIsbns);
//        }
//        return result;
//    }

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
