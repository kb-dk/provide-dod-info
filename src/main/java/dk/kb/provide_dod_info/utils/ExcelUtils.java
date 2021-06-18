package dk.kb.provide_dod_info.utils;

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

import java.util.Map;
import java.util.Set;

public class ExcelUtils {
    /**
     * Add the collected data to the excel sheet
     * @param sheet The sheet
     * @param data The data
     */
    public static void populateSheet(XSSFSheet sheet, Map<String, Object[]> data) {
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

        }
    }

    public static void setWorkbookFormats(XSSFWorkbook workbook, XSSFSheet sheet) {
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

}
