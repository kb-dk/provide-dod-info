package dk.kb.provide_dod_info.utils;

import dk.kb.provide_dod_info.AlmaRetriever;
import dk.kb.provide_dod_info.Constants;
import dk.kb.provide_dod_info.config.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Utility class for handling Excel files
 */

public class ExcelUtils {
    private static final Logger log = LoggerFactory.getLogger(ExcelUtils.class);
    /**
     * Add the collected data to the Excel sheet
     *
     * @param sheet The sheet
     * @param data  The data
     */
    public static void populateSheet(XSSFSheet sheet, Map<String, Object[]> data) {
        Set<String> keyset = data.keySet();
        int rowNumber = 0;
        for (String key : keyset) {
            XSSFRow row = sheet.createRow(rowNumber++);
            Object[] objArr = data.get(key);
            int cellNumber = 0;
            for (Object obj : objArr) {
                XSSFCell cell = row.createCell(cellNumber++);
                if (obj instanceof String) {
                    cell.setCellValue((String) obj);
                } else if (obj instanceof Integer) {
                    cell.setCellValue((Integer) obj);
                } else if (obj instanceof Double) {
                    cell.setCellValue((Double) obj);
                }
            }
        }
    }

    /**
     * Set specific formats for the workbook e.g. headings, column width,
     * @param workbook The workbook to set formats for
     */
    public static void setWorkbookFormats(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.getSheet(Constants.SHEETNAME);
        XSSFCellStyle defaultCellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
        defaultCellStyle.setFont(font);
        for (int i = 0; i < 8; i++) {
            sheet.setDefaultColumnStyle(i, defaultCellStyle);
        }
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }

        XSSFCellStyle cellStyleErrorCells = workbook.createCellStyle();
        XSSFFont fontErrorCells = workbook.createFont();
        fontErrorCells.setColor(IndexedColors.RED.getIndex());
        cellStyleErrorCells.setFont(fontErrorCells);
        for (Row row : sheet) {
            Cell descriptions = row.getCell(1);
            final String stringCellValue = descriptions.getStringCellValue();
            if (stringCellValue.startsWith(Constants.NOK)) {
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

    /**
     * Extract Barcode and Year values from Excel file
     * @param pathToExcel Absolute path to Excel file
     * @return map with barcode and year
     */
    public static Map<String, String> getValues(String pathToExcel) throws IOException {
        log.info("getValues entered");
        DataFormatter formatter = new DataFormatter();
        FileInputStream fis = null;
        try {
            File file = new File(String.valueOf(pathToExcel));
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            log.error("The file '{}' was not found", pathToExcel);
//            e.printStackTrace();
        }
        //creating Workbook instance that refers to .xlsx file
        XSSFWorkbook wb = null;
        try {
            if (fis != null) {
                wb = new XSSFWorkbook(fis);
            }
            XSSFSheet sheet = null;
            if (wb != null) {
                sheet = wb.getSheetAt(0);
            }
            if (sheet != null) {
                Map<String, String> data = new TreeMap<>();
                int barcodeColumn = 0;
                int yearColumn = 0;

                // table header row; get column index for barcodes and years
                Row row0 = sheet.getRow(0);
                Iterator<Cell> headerIterator = row0.cellIterator();
                Cell header;
                if (row0.getRowNum() == 0) {
                    while (headerIterator.hasNext()) {
                        header = headerIterator.next();
                        if ("Barcode".equalsIgnoreCase(header.getStringCellValue())) {
                            barcodeColumn = header.getColumnIndex();
                        }
                        if ("Year".equalsIgnoreCase(header.getStringCellValue())) {
                            yearColumn = header.getColumnIndex();
                        }
                    }
                }
                for  (Row row : sheet) {
                    // Add the data
                    if (row.getRowNum() > 0) {
                        Cell barcodeCell = row.getCell(barcodeColumn);
                        Cell yearCell = row.getCell(yearColumn);
                        String barcode = formatter.formatCellValue(barcodeCell);
                        String year = formatter.formatCellValue(yearCell);
                        if (AlmaRetriever.isNumeric(year) && StringUtils.isNotEmpty(barcode)) {
                            data.put(barcode, year);
                        }
                    }
                }
                log.info("getValues, returning data");
                return data;
            }
        }
        finally {
            if (wb != null) {
                fis.close();
                wb.close();
            }
        }

        throw new IllegalStateException("Sheet was not found");
//        return null;
    }

    public static void createExcel(XSSFWorkbook workbook, Configuration conf) {
        try {
            log.info("createExcel entered");
            FileOutputStream out = new FileOutputStream(conf.getTempDir() + "/" + conf.getOutFileName());
            workbook.write(out);
            out.flush();
            out.close();
            log.info("createExcel done");

        }
        catch (Exception e) {
            log.error("Failed to write Excel-file.");
            e.printStackTrace();
        }
    }

}

