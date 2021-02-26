package com.ps.druiddemo;

import com.ps.druiddemo.dao.dto.UserInfo;
import com.ps.druiddemo.dao.dto.UserInfoCriteria;
import com.ps.druiddemo.dao.mapper.UserInfoMapper;
import com.ps.druiddemo.util.ExcelUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Db2MateDataTest {
    private final static String FILE_POSTFIX_XLSX = ".xlsx";
    private final static String FILE_POSTFIX_XLS = ".xls";
    private final static String TEMPLATE_FILE_PATH = "E:\\data\\template\\matedata.xlsx";
    private final static List<String> catalogCells = Arrays.asList("tableCode","tableName","tableComment");
    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Test
    public void db2MateData() throws IOException {
        // 创建excel
        Workbook tempWorkbook = getTempWorkbook(TEMPLATE_FILE_PATH);
//        Workbook workbook = createWorkbook(TEMPLATE_FILE_PATH);
        // 写入目录
        Sheet sheet = tempWorkbook.getSheetAt(0);
        int startRow = 1;
        List<Map<String, Object>> tables =  getTables();
        for (int i = 0; i < tables.size(); i++) {
            sheet.shiftRows(startRow,sheet.getLastRowNum(),1,true,false);
//            ExcelUtils.setCellValue(sheet,i+startRow,1,String.valueOf(tables.get(i).get("TABLE_NAME")),sheet.getRow(startRow+1+i).getCell(1).getCellStyle());
//            ExcelUtils.setCellValue(sheet,i+startRow,2,String.valueOf(tables.get(i).get("TABLE_COMMENT")),sheet.getRow(startRow+1+i).getCell(2).getCellStyle());
        }
        tempWorkbook.write(FileUtils.openOutputStream(FileUtils.getFile("E:\\data\\template\\"+System.currentTimeMillis()+FILE_POSTFIX_XLSX)));
    }


    public List<Map<String, Object>> getTables(){
        List<Map<String, Object>> res = jdbcTemplate.execute("SELECT TABLE_NAME,TABLE_COMMENT FROM information_schema.TABLES WHERE table_schema ='szbtax_db' ORDER BY table_name;",
                new CallableStatementCallback<List<Map<String, Object>>>() {
                    @Override
                    public List<Map<String, Object>> doInCallableStatement(
                            CallableStatement cs) throws SQLException,
                            DataAccessException {
                        // 执行存储过程，获得结果集
                        boolean hasResult = cs.execute();
                        if (hasResult) {
                            ResultSet rs = cs.getResultSet();
                            List list = convertResultSetToList(rs);
                            return list;
                        }
                        return null;
                    }
                });
        return res;
    }

    public List convertResultSetToList(ResultSet rs) throws SQLException {
        // 封装到 List
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {// 转换每行的返回值到Map中
            Map rowMap = new HashMap();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                rowMap.put(columnName, rs.getString(columnName));
            }
            resultList.add(rowMap);
        }
        rs.close();
        return resultList;
    }


    public static Workbook getTempWorkbook(String tempFilePath) throws IOException {
        if (tempFilePath.endsWith(FILE_POSTFIX_XLSX)) {
            return new XSSFWorkbook(FileUtils.openInputStream(FileUtils.getFile(tempFilePath)));
        } else if (tempFilePath.endsWith(FILE_POSTFIX_XLS)) {
            return new HSSFWorkbook(FileUtils.openInputStream(FileUtils.getFile(tempFilePath)));
        }else{
            throw new IOException("unknown file type");
        }
    }
    public static Workbook createWorkbook(String tempFilePath) throws IOException {
        if (tempFilePath.endsWith(FILE_POSTFIX_XLSX)) {
            return new XSSFWorkbook();
        } else if (tempFilePath.endsWith(FILE_POSTFIX_XLS)) {
            return new HSSFWorkbook();
        }else{
            throw new IOException("unknown file type");
        }
    }
}
