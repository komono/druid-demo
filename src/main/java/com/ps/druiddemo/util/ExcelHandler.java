package com.ps.druiddemo.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sun.nio.cs.UnicodeEncoder;

import java.io.*;
import java.util.*;

/**
 * Desc: ExcelHandler
 * Created by mskj-panshuai on 2019-08-28 15:29:58.
 * Copr: © 2019 CMBC. All rights reserved.
 */
public class ExcelHandler {

    private final static String FILE_POSTFIX_XLSX = ".xlsx";
    private final static String FILE_POSTFIX_XLS = ".xls";

    private Map<String, HashMap[]> tempFileMap = new HashMap<>();
    private Map<String, Map<String, DataCell>> cellMap = new HashMap<>();
    private Map<String, InputStream> tempStream = new HashMap<>();
    private Map<String, Workbook> tempWorkbook = new HashMap<>();
    private Map<String, Workbook> dataWorkbook = new HashMap<>();

    /**
     * 填充list数据（<!%）
     * @param tempFilePath 模板路径
     * @param cellList 单元格列表（模板<!%后的字符串）
     * @param dataList 数据列表
     * @param sheetNo 需要填充的sheet，从0开始
     * @throws IOException
     */
    public void writeListData(String tempFilePath, List<String> cellList, List<Map<String, Object>> dataList, int sheetNo) throws IOException {
        //获取模板填充格式（位置、样式等）
        HashMap temp = getTemp(tempFilePath, sheetNo);
        // 获取写入模板
        Workbook tempWorkbook = getTempWorkbook(tempFilePath);
        // 获取起始行
        int startCell = Integer.parseInt((String) temp.get("STARTCELL"));
        Sheet sheet = tempWorkbook.getSheetAt(sheetNo);
        sheet.removeRow(sheet.getRow(startCell));
        if(CollectionUtils.isNotEmpty(dataList)){
            for (int i=0;i<dataList.size();i++){
                Map<String,Object> map = dataList.get(dataList.size()-1-i);
                if(i>0){
                    // 插入一行
                    // int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight
                    sheet.shiftRows(startCell,sheet.getLastRowNum(),1,true,false);
                }
                for (String cellName : cellList){
                    DataCell cell = getDataCell(cellName,temp,tempWorkbook,tempFilePath);
                    ExcelUtils.setCellValue(sheet,startCell, cell.getColum(),map.get(cellName), cell.getCellStyle());
                }
            }
        }
    }

    /**
     * 填充list数据（<!%）
     * @param tempFilePath 模板路径
     * @param cellList 单元格列表（模板<!%后的字符串）
     * @param dataList 数据列表
     * @param sheetNo 需要填充的sheet，从0开始
     * @throws IOException
     */
    public void writeListDataInOldLine(String tempFilePath, List<String> cellList, List<Map<String, Object>> dataList, int sheetNo) throws IOException {
        //获取模板填充格式（位置、样式等）
        HashMap temp = getTemp(tempFilePath, sheetNo);
        // 获取写入模板
        Workbook tempWorkbook = getTempWorkbook(tempFilePath);
        // 获取起始行
        int startCell = Integer.parseInt((String) temp.get("STARTCELL"));
        Sheet sheet = tempWorkbook.getSheetAt(sheetNo);
//        sheet.removeRow(sheet.getRow(startCell));
        if(CollectionUtils.isNotEmpty(dataList)){
            for (int i=0;i<dataList.size();i++){
                Map<String,Object> map = dataList.get(dataList.size()-1-i);
//                if(i>0){
//                    sheet.shiftRows(startCell,sheet.getLastRowNum(),1,true,false);
//                }
                int row = startCell+i;
                for (String cellName : cellList){
                    DataCell cell = getDataCell(cellName,temp,tempWorkbook,tempFilePath);
                    ExcelUtils.setCellValue(sheet,row, cell.getColum(),map.get(cellName), null);
                }
            }
        }else{
            for (String cellName : cellList){
                DataCell cell = getDataCell(cellName,temp,tempWorkbook,tempFilePath);
//                ExcelUtils.setCellValue(sheet,startCell, cell.getColum(),"", cell.getCellStyle());
                ExcelUtils.setCellValueBlank(sheet,startCell, cell.getColum(), null);
            }

        }

    }
    /**
     * 填充数据（<%）
     * @param tempFilePath 模板路径
     * @param cellList 单元格列表（模板<%后的字符串）
     * @param dataMap 填充的数据
     * @param sheetNo 需要填充的sheet，从0开始
     * @throws IOException
     */
    public void writeData(String tempFilePath, List<String> cellList,Map<String,Object> dataMap,int sheetNo) throws IOException {
        //获取模板填充格式（位置、样式等）
        HashMap temp = getTemp(tempFilePath, sheetNo);
        // 获取写入模板
        Workbook tempWorkbook = getTempWorkbook(tempFilePath);
        Sheet sheet = tempWorkbook.getSheetAt(sheetNo);
        if(dataMap !=null && dataMap.size()>0){
            for(String cellName : cellList){
                // 获取单元格 位置、样式
                DataCell cell = getDataCell(cellName,temp,tempWorkbook,tempFilePath);
                // 写入单元格数据
                ExcelUtils.setCellValue(sheet,cell.getLine(), cell.getColum(),dataMap.get(cellName), cell.getCellStyle());
            }
        }
    }

    /**
     *  文件数据读取
     * @param tempFilePath 模板路径
     * @param cellList 单元格列表（模板<！%后的字符串）
     * @param sheetNo 需要读取的sheet，从0开始
     * @param excelFile 需要读取的Excel文件
     * @return
     * @throws IOException
     */
    public List<Map<String,Object>> getListValue(String tempFilePath,List<String> cellList,int sheetNo,File excelFile) throws IOException{
        List<Map<String,Object>> dataList = new ArrayList<>();
        //获取模板填充格式（位置、样式等）
        HashMap temp = getTemp(tempFilePath, sheetNo);
        // 获取写入模板
        Workbook dataWorkbook = getDataWorkbook(tempFilePath,excelFile);
        // 获取起始行
        int startCell = Integer.parseInt((String) temp.get("STARTCELL"));
        Sheet sheet = dataWorkbook.getSheetAt(sheetNo);
        int lastLine =sheet.getLastRowNum();

        for(int i=startCell;i<=lastLine;i++){
            dataList.add(getListLineValue(i, tempFilePath,cellList,sheetNo,excelFile));
        }
        return dataList;
    }

    /**
     * 读取某一行数据
     * @param line 行号
     * @param tempFilePath 模板路径
     * @param cellList 单元格列表（模板<！%后的字符串）
     * @param sheetNo 需要读取的sheet，从0开始
     * @param excelFile 需要读取的Excel文件
     * @return
     * @throws IOException
     */
    public Map<String ,Object> getListLineValue(int line, String tempFilePath,List<String> cellList,int sheetNo,File excelFile) throws IOException {
        Map<String ,Object> dataMap = new HashMap<>();
        // 获取模板格式
        HashMap temp = getTemp(tempFilePath,sheetNo);
        Workbook tempWorkbook = getTempWorkbook(tempFilePath);
        Workbook dataWorkbook = getDataWorkbook(tempFilePath,excelFile);
        Sheet dataSheet = dataWorkbook.getSheetAt(sheetNo);

        for(String cellName : cellList){
            DataCell cell = getDataCell(cellName,temp,tempWorkbook,tempFilePath);
            dataMap.put(cellName,ExcelUtils.getCellValue(dataSheet,line,cell.getColum()));
        }

        return  dataMap;
    }

    /**
     * 输出到输出流关闭相关资源
     * @param tempFilePath 模板路径
     * @param os 输出流
     * @throws IOException
     */
    public void writeAndClose(String tempFilePath, OutputStream os) throws IOException {
        if(getTempWorkbook(tempFilePath)!=null){
            getTempWorkbook(tempFilePath).setForceFormulaRecalculation(true);
            getTempWorkbook(tempFilePath).write(os);
            tempWorkbook.remove(tempFilePath);
        }
        if(getFileInputStream(tempFilePath)!=null){
            getFileInputStream(tempFilePath).close();
            tempStream.remove(tempFilePath);
        }
    }

    /**
     * 获取工作区
     * @param tempFilePath 模板路径
     * @param excelFile Excel文件
     * @return
     * @throws IOException
     */
    private Workbook getDataWorkbook(String tempFilePath, File excelFile) throws IOException {
        if(!dataWorkbook.containsKey(tempFilePath)){
            if(tempFilePath.endsWith(FILE_POSTFIX_XLSX)){
                dataWorkbook.put(tempFilePath,new XSSFWorkbook(FileUtils.openInputStream(excelFile)));
            }else if(tempFilePath.endsWith(FILE_POSTFIX_XLS)){
                dataWorkbook.put(tempFilePath,new HSSFWorkbook(FileUtils.openInputStream(excelFile)));
            }else{
                throw new IOException("unknown file type");
            }
        }
        return dataWorkbook.get(tempFilePath);
    }

    /**
     * 获取单元格数据
     * @param cellName
     * @param temp
     * @param workbook
     * @param tempFilePath
     * @return
     */
    private DataCell getDataCell(String cellName,HashMap temp,Workbook workbook,String tempFilePath){
        if(!cellMap.get(tempFilePath).containsKey(cellName)){
            DataCell cell = new DataCell();

            int[] pos = ExcelUtils.getPos(temp,cellName);
            if(pos.length>1){
                cell.setLine(pos[1]);
            }
            cell.setColum(pos[0]);
            cell.setCellStyle(ExcelUtils.getCellStyle(temp,cellName,workbook));
            cellMap.get(tempFilePath).put(cellName,cell);
        }
        return cellMap.get(tempFilePath).get(cellName);
    }

    /**
     * 获取输入工作区
     *
     * @param tempFilePath
     * @return
     * @throws IOException
     */
    private Workbook getTempWorkbook(String tempFilePath) throws IOException {
        if (!tempWorkbook.containsKey(tempFilePath)) {
            if (tempFilePath.endsWith(FILE_POSTFIX_XLSX)) {
                tempWorkbook.put(tempFilePath, new XSSFWorkbook(getFileInputStream(tempFilePath)));
            } else if (tempFilePath.endsWith(FILE_POSTFIX_XLS)) {
                tempWorkbook.put(tempFilePath, new HSSFWorkbook(getFileInputStream(tempFilePath)));
            }else{
                throw new IOException("unknown file type");
            }
        }
        return tempWorkbook.get(tempFilePath);
    }

    /**
     * 获取模板输入流
     *
     * @param tempFilePath
     * @return
     * @throws IOException
     */
    private InputStream getFileInputStream(String tempFilePath) throws IOException {
        if (!tempStream.containsKey(tempFilePath)) {
            InputStream is =ExcelUtils.class.getResourceAsStream(tempFilePath);//FileUtils.openInputStream(FileUtils.getFile(tempFilePath));//
            tempStream.put(tempFilePath, is);
        }
        return tempStream.get(tempFilePath);
    }

    /**
     * 获取模板数据
     *
     * @param tempFilePath
     * @param sheet
     * @return
     * @throws IOException
     */
    private HashMap getTemp(String tempFilePath, int sheet) throws IOException {
        if (!tempFileMap.containsKey(tempFilePath)) {
            tempFileMap.put(tempFilePath, ExcelUtils.getTemplateFile(tempFilePath));
            cellMap.put(tempFilePath, new HashMap<String, DataCell>());
        }
        return tempFileMap.get(tempFilePath)[sheet];
    }

    /**
     * 读取后关闭
     * @param tempFilePath
     */
    public void readClose(String tempFilePath){
        dataWorkbook.remove(tempFilePath);
    }

    class DataCell {
        private int colum;// 列
        private int line;// 行
        private CellStyle cellStyle;

        public int getColum() {
            return colum;
        }

        public void setColum(int colum) {
            this.colum = colum;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public CellStyle getCellStyle() {
            return cellStyle;
        }

        public void setCellStyle(CellStyle cellStyle) {
            this.cellStyle = cellStyle;
        }
    }

    public static void main(String[] args) throws IOException {

        // test_bom.txt     fs_mygj_ContractInfo
//        InputStream is = FileUtils.openInputStream(FileUtils.getFile("D:\\tomcat\\log\\cmbccap-app\\risk\\20191120\\fs_mygj_ContractInfo.txt"));
//        byte[] head =new byte[3];
//        int n = is.read(head);
//        System.out.println(head[0]+" "+head[1]+" "+head[2]);
//        System.out.println("-17 -69 -65");


        System.out.println(System.getProperty("user.name"));
        String fileTemplatePath = "/META-INF/cmbccap-biz/services/template/Payroll.xls";
        List<String> cells = new ArrayList<String>(){
            {
                add("remark");
                add("email");}
        };
        List<Map<String,Object>> dataList = new ArrayList<>();
        dataList.add(new HashMap<String, Object>(){{
            put("remark","remark1");
            put("email","email1");
        }});
        dataList.add(new HashMap<String, Object>(){{
            put("remark","remark2");
            put("email","email2");
        }});

        List<String> cells1 = new ArrayList<String>(){
            {
                add("companyNameEnglish");
        }};
        Map dataMap = new HashMap<String, Object>(){{
            put("companyNameEnglish","test");
        }};
//
        ExcelHandler handler = new ExcelHandler();
        handler.writeData(fileTemplatePath,cells1,dataMap,0);
        handler.writeListDataInOldLine(fileTemplatePath,cells,dataList,0);
        File file = FileUtils.getFile("C://test.xls");
        OutputStream os = FileUtils.openOutputStream(file);
        handler.writeAndClose(fileTemplatePath,os);
        os.flush();
        os.close();

//
//        ExcelHandler handlerRead = new ExcelHandler();
//        System.out.println(handlerRead.getListValue(fileTemplatePath,cells,0,file));
//        handlerRead.readClose(fileTemplatePath);

//        String ACCOUNT_FILE_TEMPLATE_PATH = "/META-INF/cmbccap-biz/services/template/BankAccount.xlsx";
//        String BASE_INFO_FILE_TEMPLATE_PATH = "/META-INF/cmbccap-biz/services/template/BaseInfo.xlsx";
//        List<String> ACCOUNT_FILE_TEMPLATE_CELLS = Arrays.asList("companyNameEnglish","department1","department2","employeeNo","surname","givenName","alias","name","grading","bankCode","accountNo","accountName");
//        List<String> BASE_INFO_FILE_TEMPLATE_CELLS = Arrays.asList("no","company","employeeNo","nameEnglish","name","department","grading","dateJoined","totalSalary","remark");
//
//        File file = FileUtils.getFile("D://Document//00-需求//20190725-民银资本贷款合约//人力附件//BankAccount.xlsx");
//        ExcelHandler handlerRead = new ExcelHandler();
//        System.out.println(handlerRead.getListValue(ACCOUNT_FILE_TEMPLATE_PATH,ACCOUNT_FILE_TEMPLATE_CELLS,0,file));
//        handlerRead.readClose(ACCOUNT_FILE_TEMPLATE_PATH);
    }
}
