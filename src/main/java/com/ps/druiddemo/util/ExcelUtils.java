package com.ps.druiddemo.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Desc: ExcelUtils
 * Created by mskj-panshuai on 2019-08-28 11:28:44.
 * Copr: © 2019 CMBC. All rights reserved.
 */
public class ExcelUtils {
    private ExcelUtils() {
    }

    private final static String FILE_POSTFIX_XLSX = ".xlsx";
    private final static String FILE_POSTFIX_XLS = ".xls";

    /**
     * 获取单元格的值
     *
     * @param sheet
     * @param rowNo
     * @param cellNo
     * @return
     */
    public static String getCellValue(Sheet sheet, int rowNo, int cellNo) {
        String cellValue = null;
        Row row = sheet.getRow(rowNo);
        if(row==null){
            return cellValue;
        }
        Cell cell = row.getCell(cellNo);
        if (cell != null) {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                if(isCellDateFormatted(cell.getCellStyle())){
                    cellValue = DateFormatUtils.format(DateUtil.getJavaDate(cell.getNumericCellValue()),
                            "yyyy-MM-dd");
                }else {
                    DecimalFormat df = new DecimalFormat("0");
                    cellValue = getCutDotStr(df.format(cell.getNumericCellValue()));
                }
            } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                cellValue = cell.getStringCellValue();
            }else if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN){
                cellValue = String.valueOf(cell.getBooleanCellValue());
            }else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
                try {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                }catch (IllegalStateException e){
                    cellValue =  String.valueOf(cell.getRichStringCellValue());
                }
            }
            if (cellValue != null) {
                cellValue = cellValue.trim();
            }
        } else {
            cellValue = null;
        }
        return cellValue;
    }

    /**
     * 取整数
     *
     * @param str
     * @return
     */
    private static String getCutDotStr(String str) {
        if (StringUtils.isNotEmpty(str) && str.endsWith(".0")) {
            return str.substring(0, str.length() - 2);
        } else {
            return str;
        }
    }


    /**
     * 读取template文件
     * @param templateFileName
     * @return
     * @throws IOException
     */
    public static HashMap[] getTemplateFile(String templateFileName) throws IOException {
        InputStream fis = ExcelUtils.class.getResourceAsStream(templateFileName);
//        String path = ExcelUtils.class.getResource(templateFileName).getPath();
//        InputStream fis = FileUtils.openInputStream(FileUtils.getFile(path));
//        BufferedInputStream bis = new BufferedInputStream(fis);
        try {
//            Workbook workbook = WorkbookFactory.create(fis);
            Workbook workbook = null;
//            if(!fis.markSupported()){
////                fis = new PushbackInputStream(fis,8);
////            }
            if (templateFileName.endsWith(FILE_POSTFIX_XLSX)) {
//            if(POIXMLDocument.hasOOXMLHeader(fis)){
                workbook = new XSSFWorkbook(fis);
            } else if (templateFileName.endsWith(FILE_POSTFIX_XLS)) {
//            } else if (POIFSFileSystem.hasPOIFSHeader(fis)) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IOException("unknown file type");
            }
            int numOfSheet = workbook.getNumberOfSheets();
            HashMap[] templateMap = new HashMap[numOfSheet];
            for (int i = 0; i < numOfSheet; i ++) {
                Sheet sheet = workbook.getSheetAt(i);
                templateMap[i] = new HashMap();
                readTemplateSheet(templateMap[i], sheet);
            }
            return templateMap;
//        } catch (InvalidFormatException e) {
//            throw new IOException("unknown file type");
        } finally {
            if (fis != null) {
                fis.close();
            }
//            if (bis != null) {
//                bis.close();
//            }
        }
    }

    /**
     * 设置单元格值
     * @param sheet
     * @param row
     * @param cell
     * @param value
     * @param cellStyle
     */
    public static void setCellValue(Sheet sheet,int row,int cell, Object value,CellStyle cellStyle){
        Row rowIn = sheet.getRow(row);
        if (rowIn==null){
            rowIn = sheet.createRow(row);
        }
        Cell cellIn = rowIn.getCell(cell);
        if(cellIn == null){
            cellIn = rowIn.createCell(cell);
        }
        if(cellStyle != null){
            cellIn.setCellStyle(cellStyle);
        }
        if(value==null){
            cellIn.setCellValue("");
        }else{
            if(isCellDateFormatted(cellStyle)){
                cellIn.setCellValue((Date) value);
            }else if(value instanceof Double){
                cellIn.setCellValue((Double) value);
            }else if(value instanceof BigDecimal){
                cellIn.setCellValue( ((BigDecimal)value).doubleValue());
            }else{
                cellIn.setCellValue(value.toString());
            }
        }
    }

    /**
     * 设置单元格值
     * @param sheet
     * @param row
     * @param cell
     * @param cellStyle
     */
    public static void setCellValueBlank(Sheet sheet,int row,int cell, CellStyle cellStyle){
        Row rowIn = sheet.getRow(row);
        if (rowIn==null){
            rowIn = sheet.createRow(row);
        }
        Cell cellIn = rowIn.getCell(cell);
        if(cellIn == null){
            cellIn = rowIn.createCell(cell);
        }
        if(cellStyle != null){
            cellIn.setCellStyle(cellStyle);
        }
        cellIn.setCellType(Cell.CELL_TYPE_BLANK);
    }

    /**
     * 判断是否是日期
     * @param cellStyle
     * @return
     */
    public static  boolean isCellDateFormatted(CellStyle cellStyle){
        if(cellStyle == null){
            return false;
        }
        int i = cellStyle.getDataFormat();
        String f = cellStyle.getDataFormatString();
        return org.apache.poi.ss.usermodel.DateUtil.isADateFormat(i,f);
    }

    /**
     * 读取TemplateSheet
     * @param keyMap
     * @param sheet
     */
    private static void readTemplateSheet(HashMap keyMap, Sheet sheet) {
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        for (int i = firstRowNum; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            int firstCellNum = row.getFirstCellNum();
            int lastCellNum = row.getLastCellNum();
            for (int j = firstCellNum; j <= lastCellNum; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    continue;
                }

                int cellType = cell.getCellType();
                if (cellType != Cell.CELL_TYPE_STRING) {
                    continue;
                }
                String cellValue = cell.getStringCellValue();
                if (StringUtils.isEmpty(cellValue)) {
                    continue;
                }
                cellValue = cellValue.trim();
                if (cellValue.length() > 2 && cellValue.substring(0, 2).equals("<%")) {
                    String key = cellValue.substring(2);
                    String keyPos = Integer.toString(j) + "," + Integer.toString(i);
                    keyMap.put(key, keyPos);
                    keyMap.put(key + "CellStyle", cell.getCellStyle());
                } else if (cellValue.length() > 3 && cellValue.substring(0, 3).equals("<!%")) {
                    String key = cellValue.substring(3);
                    keyMap.put("STARTCELL", Integer.toString(i));
                    keyMap.put(key, Integer.toString(j));
                    keyMap.put(key + "CellStyle", cell.getCellStyle());
                }
            }
        }
    }

    /**
     * 取得单元格的行列值
     * @param keyMap 所有单元格数据
     * @param key 单元格标识
     * @return 0：列 1：行（列表型数据不记行）
     */
    public static int[] getPos(HashMap keyMap,String key){
        int[] ret = new int[0];
        String val = (String) keyMap.get(key);
        if(StringUtils.isEmpty(val)){
            return ret;
        }
        String pos[] = val.split(",");
        if(pos.length==1||pos.length==2){
            ret = new int[pos.length];
            for (int i=0;i<pos.length;i++){
                if(StringUtils.isBlank(pos[i])){
                    ret[i] = 0;
                }else{
                    ret[i] = Integer.parseInt(pos[i].trim());
                }
            }
        }
        return ret;
    }

    public static CellStyle  getCellStyle(HashMap map, String key,Workbook workbook){
        CellStyle cellStyle = null;
        cellStyle = (CellStyle) map.get(key+"CellStyle");
        // 自动换行
        cellStyle.setWrapText(true);
        CellStyle newCellStyle = workbook.createCellStyle();
        newCellStyle.cloneStyleFrom(cellStyle);
        return  newCellStyle;
    }
}
