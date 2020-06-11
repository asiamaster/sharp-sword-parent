package com.dili.ss.util;

import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by asiam on 2018/7/4 0004.
 */
public class ExcelUtils {

    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";

    /**
     * 判断Excel的版本,获取Workbook
     *
     * @param in
     * @param filename
     * @return
     * @throws IOException
     */
    public static Workbook getWorkbok(InputStream in, String filename) throws IOException {
        Workbook wb = null;
        if (filename.endsWith(EXCEL_XLS)) {  //Excel 2003
            wb = new HSSFWorkbook(in);
        } else if (filename.endsWith(EXCEL_XLSX)) {  // Excel 2007/2010
            wb = new XSSFWorkbook(in);
        }
        return wb;
    }

    /**
     * 判断文件是否是excel
     *
     * @throws Exception
     */
    public static void checkExcelVaild(File file) throws Exception {
        if (!file.exists()) {
            throw new Exception("文件不存在");
        }
        if (!(file.isFile() && (file.getName().endsWith(EXCEL_XLS) || file.getName().endsWith(EXCEL_XLSX)))) {
            throw new Exception("文件不是Excel");
        }
    }

//    public static void main1(String[] args) throws Exception {
//        // 同时支持Excel 2003、2007
//        File excelFile = new File("d:/CRM.xlsx"); // 创建文件对象
//        checkExcelVaild(excelFile);
//        FileInputStream is = new FileInputStream(excelFile); // 文件流
//        List<List<Map<String, Object>>> list = getSheetsDatas(is, 0);
//        System.out.println(list);
//    }


    /**
     * 根据输入流获取excel信息
     * @param is        数据流
     * @param headerRow 标题所在的行数(从0开始)
     * @return 返回多sheet的列表数据， Map中的key为列头
     */
    public static List<List<Map<String, Object>>> getSheetsDatas(InputStream is, int headerRow) {
        //最终返回的sheets列表数据
        List<List<Map<String, Object>>> sheetsDatas = Lists.newArrayList();
        try {
//            Workbook workbook = getWorkbok(is, "CRM.xlsx");
            Workbook workbook = WorkbookFactory.create(is); // 这种方式 Excel2003/2007/2010都是可以处理的
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                List<Map<String, Object>> sheetDatas = Lists.newArrayList();
                Sheet sheet = workbook.getSheetAt(sheetIndex);   // 遍历第一个Sheet
                //行索引
                int rowIndex = 0;
                //列头索引数据,key为列索引，值为列头
                Map<Integer, String> rowIndexData = new HashMap<>();
                for (Row row : sheet) {
                    //行数据,key为列头，值为行数据
                    Map<String, Object> rowData = new HashMap<>();
                    // 跳过标题所在的行数
                    if (rowIndex < headerRow) {
                        rowIndex++;
                        continue;
                    }
                    int lastCellNum = row.getLastCellNum();
                    //如果是列头行或者列头索引为空，记录列头
                    if (rowIndex == headerRow || rowIndexData.isEmpty()) {
                        for (int colIndex = 0; colIndex < lastCellNum; colIndex++) {
                            Cell cell = row.getCell(colIndex);
                            if (cell == null) {
                                continue;
                            }
                            Object value = getValue(cell);
                            if (value == null) {
                                continue;
                            }
//                          key为列头，值为列索引
                            rowIndexData.put(colIndex, value.toString());
                        }
                        rowIndex++;
                        continue;
                    }
                    //读取列数据
                    for (int colIndex = 0; colIndex < lastCellNum; colIndex++) {
                        rowData.put(rowIndexData.get(colIndex), getValue(row.getCell(colIndex)));
                    }
                    sheetDatas.add(rowData);
                    rowIndex++;
                }
                sheetsDatas.add(sheetDatas);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sheetsDatas;
    }

    /**
     * 获取单元格的值
     *
     * @param cell
     * @return
     */
    private static Object getValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellTypeEnum()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC: {
                Double value = cell.getNumericCellValue();
                return isInteger(value) ? value.longValue() : value;
            }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                return "错误";
            case BLANK:
                return "空";
            case FORMULA:
                return "错误";
            default:
                return cell.getStringCellValue();
        }
    }

    /**
     * 判断是整数
     * @param d
     * @return
     */
    private static boolean isInteger(double d){
        return d % 1 ==0;
    }
}