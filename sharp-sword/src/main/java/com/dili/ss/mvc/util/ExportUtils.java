package com.dili.ss.mvc.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dili.http.okhttp.OkHttpUtils;
import com.dili.ss.domain.ExportParam;
import com.dili.ss.domain.TableHeader;
import com.dili.ss.java.B;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.util.BeanConver;
import com.dili.ss.util.IExportThreadPoolExecutor;
import com.dili.ss.util.SpringUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 通用导出工具
 * Created by asiamaster on 2017/6/15 0015.
 */
@Component
@DependsOn("initConfig")
public class ExportUtils {

    public final static Logger log = LoggerFactory.getLogger(ExportUtils.class);

    //每次去后台获取条数
    private final static int FETCH_COUNT = 20000;
    private OkHttpClient okHttpClient = null;

    /**
     * 表单参数
     */
    private final static String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    /**
     * JSON参数，以HTTP Body方式请求
     */
    private final static String CONTENT_TYPE_JSON = "application/json";


    //    多线程执行器
    private ExecutorService executor;

    @PostConstruct
    public void init(){
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();
        OkHttpUtils.initClient(okHttpClient);
        try {
            executor = ((Class<IExportThreadPoolExecutor>) B.b.g("threadPoolExecutor")).newInstance().getCustomThreadPoolExecutor();
        } catch (Exception e) {
        }
    }

    /**
     *  通用beans导出方法
     * @param response
     * @param title 标题/文件名
     * @param tableHeaders   表头，使用List是为了排序
     * @param beans 数据列表,Bean集合
     * @param providerMeta provider的metadata, key为字段名， value为provider的beanId，如果不需要转义，则为null
     */
    public void exportBeans(HttpServletResponse response, String title, List<TableHeader> tableHeaders, List beans, Map providerMeta) throws Exception {
        List<Map> datas = new ArrayList<>(beans.size());
        for(Object bean : beans){
            Map map = BeanConver.transformObjectToMap(bean);
            datas.add(map);
        }
        exportMaps(response, title, tableHeaders, datas, providerMeta);
    }

    /**
     *  通用maps导出方法
     * @param response
     * @param title 标题/文件名
     * @param tableHeaders   表头，使用List是为了排序
     * @param datas 数据列表,Map集合
     * @param providerMeta provider的metadata, key为字段名， value为provider的beanId，如果不需要转义，则为null
     */
    public void exportMaps(HttpServletResponse response, String title, List<TableHeader> tableHeaders, List<Map> datas, Map providerMeta){
        SXSSFWorkbook workbook = new SXSSFWorkbook(FETCH_COUNT);                     // 创建工作簿对象
        Sheet sheet = workbook.createSheet(title);

        //构建表头
        CellStyle columnTopStyle = getHeaderColumnStyle(workbook);  //获取列头样式对象
        Row headerRow = sheet.createRow(0);
        for (int j = 0; j < tableHeaders.size(); j++) {
            //列头信息
            TableHeader tableHeader = tableHeaders.get(j);
            Cell cell = headerRow.createCell(j, CellType.STRING);               //创建列头对应个数的单元格
            RichTextString text = new XSSFRichTextString(tableHeader.getTitle().replaceAll("\\n", "").trim());
            cell.setCellValue(text);                                 //设置列头单元格的值
            cell.setCellStyle(columnTopStyle);                       //设置列头单元格样式
        }
        //构建数据
        //渲染数据列
        CellStyle dataColumnStyle = getDataColumnStyle(workbook);//获取列头样式对象
        //用于缓存providerBean
        Map<String, ValueProvider> providerBuffer = new HashMap<>();
        //迭代数据
        for(int i=0; i<datas.size(); i++ ){
            Map rowDataMap = datas.get(i);
            Row dataRow = sheet.createRow(i+1);
            //迭代列头
            for(int j=0; j<tableHeaders.size(); j++){
                TableHeader tableHeader = tableHeaders.get(j);
                Object value = rowDataMap.get(tableHeader.getField());
                CellType cellType = value instanceof Number ? CellType.NUMERIC : CellType.STRING;
                Cell cell = dataRow.createCell(j, cellType);
                cell.setCellStyle(dataColumnStyle);
                if(providerMeta != null && providerMeta.containsKey(tableHeader.getField())){
                    ValueProvider valueProvider = null;
                    //value是provider的beanId
                    String providerBeanId = (String)providerMeta.get(tableHeader.getField());
                    if(providerBuffer.containsKey(providerBeanId)){
                        valueProvider = providerBuffer.get(providerBeanId);
                    }else {
                        valueProvider = SpringUtil.getBean(providerBeanId, ValueProvider.class);
                        providerBuffer.put(providerBeanId, valueProvider);
                    }
                    setCellValue(cell, valueProvider.getDisplayText(value, null, null), dataColumnStyle);
                }else {
                    setCellValue(cell, value, dataColumnStyle);
                }
            }
        }
        //执行导出
        write(title, workbook, response);
    }

    /**
     * 根据ExportParam导出数据<br></>
     * 用于ExportController的导出datagrid表格
     */
    public void export(HttpServletRequest request, HttpServletResponse response, ExportParam exportParam) {
        try {
//            HSSFWorkbook workbook = new HSSFWorkbook();
            SXSSFWorkbook workbook = new SXSSFWorkbook(FETCH_COUNT);// 创建工作簿对象
            String title = exportParam.getTitle();
            SXSSFSheet sheet = workbook.createSheet(StringUtils.isBlank(title) ? "sheet1" : title);
//            解决高版本poi autoSizeColumn方法异常的情况
            sheet.trackAllColumnsForAutoSizing();
            //构建表头
            buildHeader(exportParam, workbook, sheet);
            //构建数据
            buildData(exportParam, workbook, sheet, request);
            //执行导出
            write(exportParam.getTitle(), workbook, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建数据列
     * @param exportParam
     * @param workbook
     * @param sheet
     * @param request
     */
    private void buildData(ExportParam exportParam, SXSSFWorkbook workbook, Sheet sheet, HttpServletRequest request){
        //渲染数据列
        CellStyle dataColumnStyle = getDataColumnStyle(workbook);//获取列头样式对象
        String url = exportParam.getUrl();
        if(!url.startsWith("http")){
            url = url.startsWith("/") ? url : "/" + url;
            String basePath = SpringUtil.getProperty("project.serverPath", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
            url = basePath + url;
        }
        //这里获取到的是nginx转换后的(IP)地址和端口号，如果是跳板机这种，有可能会禁止访问，后续改为从配置读取
//        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();

//        Map<String, String> queryParams = exportParam.getQueryParams();
        //先获取总数
        int total = getCount(url, exportParam.getContentType(), exportParam.getQueryParams(), request);
        //查询次数
        int queryCount = total % FETCH_COUNT == 0 ? total/ FETCH_COUNT : total/ FETCH_COUNT +1;

        //如果只查一次，就不启线程了，直接在主线程查
        if(queryCount == 1) {
            JSONArray rowDatas = new ExportDataThread(0, exportParam.getQueryParams(), url, exportParam.getContentType(), request).queryThreadData();
            buildSingleData(0, exportParam.getColumns(), rowDatas, sheet, dataColumnStyle);
        }else {
            //分别进行取数
            List<Future<JSONArray>> futures = new ArrayList<>(queryCount);
            for (int current = 0; current < queryCount; current++) {
                Map<String, String> queryParams =  new HashMap<>();
                //注意这里直接使用exportParam.getQueryParams()会产生多线程并发缺陷，所有深putAll进行半深拷贝
                //然而putAll也不完全是深拷贝，但它的性能优于字节拷贝，它只能深拷贝基本类型，不过这里也只有基本类型
                queryParams.putAll(exportParam.getQueryParams());
                Future<JSONArray> future = executor.submit(new ExportDataThread(current, queryParams, url, exportParam.getContentType(), request));
                futures.add(future);
            }
            int current = 0;
            try {
                for (Future<JSONArray> future : futures) {
                    JSONArray rowDatas = future.get();
                    buildSingleData(current++, exportParam.getColumns(), rowDatas, sheet, dataColumnStyle);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //构建单次数据
    private void buildSingleData(int current, List<List<Map<String, Object>>> columns, JSONArray rowDatas, Sheet sheet, CellStyle dataColumnStyle){
        Integer headerRowCount = columns.size();
        //迭代数据
        for(int i=0; i<rowDatas.size(); i++ ){
            JSONObject rowDataMap = (JSONObject)rowDatas.get(i);
            Row row = sheet.createRow(current * FETCH_COUNT + i + headerRowCount);
            //直接取最后一行的列头信息
            List<Map<String, Object>> headers = columns.get(columns.size()-1);
            int index = 0;
            //迭代列头
            for(int j=0; j<headers.size(); j++){
                Map<String, Object> headerMap = headers.get(j);
                //隐藏的列不导出
                if(headerMap.get("hidden")!=null && headerMap.get("hidden").equals(true)){
                    continue;
                }
                String field = (String)headerMap.get("field");
                Object value = null;

                int fieldIndex = field.indexOf(".");
                //如果没有提供者，则由导出处理obj.key这种field
                if(StringUtils.isBlank((String)headerMap.get("provider")) && fieldIndex >= 0){
                    String field1 = field.substring(0, fieldIndex);
                    String field2 = field.substring(fieldIndex+1);
                    JSONObject obj = rowDataMap.getJSONObject(field1);
                    if(obj != null) {
                        value = obj.get(field2);
                    }
                }else {
                    value = rowDataMap.get(field);
                }
                CellType cellType = value instanceof Number ? CellType.NUMERIC : CellType.STRING;
                if(value instanceof Long || value instanceof Integer) {
                    dataColumnStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
                }
                Cell cell = row.createCell(index, cellType);
                cell.setCellStyle(dataColumnStyle);
                //判断是否有值提供者需要转义(此功能已经在datagrid的查询中封装，这里不需要处理了)
//                if(headerMap.containsKey("provider")){
//                    value = valueProviderUtils.setDisplayText(headerMap.get("provider").toString(), value, null);
//                }
                setCellValue(cell, value, dataColumnStyle);
                index++;
            }
        }
    }

    /**
     * 设置单元格的值，主要是处理单元格类型
     * @param cell
     * @param value
     * @param dataColumnStyle
     */
    private void setCellValue(Cell cell, Object value, CellStyle dataColumnStyle){
        if(value == null){
            cell.setCellValue("");
        }else if(value instanceof Integer){
            //数据格式只显示整数
            dataColumnStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
            cell.setCellValue(((Integer)value).doubleValue());
        }else if(value instanceof Long){
            dataColumnStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
            cell.setCellValue(((Long)value).doubleValue());
        }else if(value instanceof Short){
            dataColumnStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
            cell.setCellValue(((Short)value).doubleValue());
        }else if(value instanceof Float){
            //float支持小数点后8位
            dataColumnStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00000000"));
            cell.setCellValue(((Float)value).doubleValue());
        }else if(value instanceof Double){
            //double支持小数点后10位(Double最大支持17位)
            dataColumnStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.0000000000"));
            cell.setCellValue((Double)value);
        }else if(value instanceof BigDecimal){
            dataColumnStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.0000000000"));
            cell.setCellValue(((BigDecimal)value).doubleValue());
        }else{
            dataColumnStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));
            RichTextString text = new XSSFRichTextString(value.toString());
            cell.setCellValue(text);
        }
        cell.setCellStyle(dataColumnStyle);
    }

    /**
     * 导出一部分数据
     */
    private class ExportDataThread implements Callable {
        int current;
        Map<String, String> queryParams;
        String exportUrl;
        String contentType;
        HttpServletRequest request;

        ExportDataThread(int current, Map<String, String> queryParams, String exportUrl, String contentType, HttpServletRequest request) {
            this.current = current;
            this.queryParams = queryParams;
            this.exportUrl = exportUrl;
            this.request = request;
            this.contentType = contentType;
        }

        @Override
        public JSONArray call() {
            return queryThreadData();
        }

        //构建每个线程导出的数据
        private JSONArray queryThreadData(){
            queryParams.put("page", String.valueOf(current+1));
            queryParams.put("rows", String.valueOf(FETCH_COUNT));
            String json = syncExecute(exportUrl, contentType, queryParams, "POST", request);
            //简单判断是JSONArray的话，就是不分页的list查询，总数直接取JSONArray
            if(json.trim().startsWith("[") && json.trim().endsWith("]")){
                return JSON.parseArray(json);
            }else {
                return (JSONArray) JSON.parseObject(json).get("rows");
            }
        }
    }

    /**
     * 获取当前需要导出的总数
     * @param url
     * @param queryParams
     * @param contentType
     * @param request   为了数据权限
     * @return
     */
    private int getCount(String url, String contentType, Map<String, String> queryParams, HttpServletRequest request){
        queryParams.put("page", "1");
        queryParams.put("rows", "1");
        String json = syncExecute(url, contentType, queryParams, "POST", request);
        //简单判断是JSONArray的话，就是不分页的list查询，总数直接取JSONArray的长度
        if(json.trim().startsWith("[") && json.trim().endsWith("]")){
            return JSON.parseArray(json).size();
        }else {
            return (int) JSON.parseObject(json).get("total");
        }
    }

    /**
     * 构建表头列, 只支持colspan，不支持rowspan，因为rowspan无法确定是向上还是向下合并,判断的因素太多，暂不支持
     * @param exportParam
     * @param workbook
     * @param sheet
     */
    private void buildHeader(ExportParam exportParam, SXSSFWorkbook workbook, Sheet sheet){
        CellStyle columnTopStyle = getHeaderColumnStyle(workbook);//获取列头样式对象
        //渲染复合表头列
        for (int i = 0; i < exportParam.getColumns().size(); i++) {
            //每行的列信息
            List<Map<String, Object>> rowColumns = exportParam.getColumns().get(i);
            Row row = sheet.createRow(i);
            int colspanAdd = 0;
            int index = 0;
            //迭代生成每一列，如果有hidden或者title为null的，则跳过
            Iterator<Map<String, Object>> it = rowColumns.iterator();
            //列号
            int columnIndex=0;
            while(it.hasNext()){

//            for (int j = 0; j < rowColumns.size(); j++) {
                //列头信息
                Map<String, Object> columnMap = it.next();
                //隐藏的列不导出
                if(columnMap.get("hidden")!=null && columnMap.get("hidden").equals(true)){
                    it.remove();
                    continue;
                }
                if(columnMap.get("title") == null){
                    it.remove();
                    continue;
                }
                String headerTitle = columnMap.get("title").toString().replaceAll("\\n", "").trim();
                //最后一行的列头，适应宽度
                if( i == exportParam.getColumns().size() - 1){
                    sheet.setColumnWidth(columnIndex, headerTitle.getBytes().length*2*256);
//                    sheet.autoSizeColumn(j, true);
                }
                Cell cell = row.createCell(index+colspanAdd, CellType.STRING);               //创建列头对应个数的单元格
                RichTextString text = new XSSFRichTextString(headerTitle);
                cell.setCellValue(text);                                 //设置列头单元格的值
                cell.setCellStyle(columnTopStyle);                       //设置列头单元格样式
                if(columnMap.get("colspan") != null) {
                    Integer colspan = Integer.class.isAssignableFrom(columnMap.get("colspan").getClass())? (Integer)columnMap.get("colspan") : Integer.parseInt(columnMap.get("colspan").toString());
                    if(colspan > 1) {
                        Cell tempCell = row.createCell(index + colspanAdd + colspan - 1, CellType.STRING);               //创建合并最后一列的列头，保证最后一列有右边框
                        tempCell.setCellStyle(columnTopStyle);
                        sheet.addMergedRegion(new CellRangeAddress(i, i, index + colspanAdd, index + colspanAdd + colspan - 1));
                        colspanAdd = colspanAdd + colspan - 1;
                    }
                }
                columnIndex++;
                index++;
            }
        }
    }

    /**
     * 同步调用远程方法
     * @param url
     * @param paramObj
     * @param httpMethod
     * @param contentType
     * @param request   为了数据权限
     * @return
     */
    private String syncExecute(String url, String contentType, Object paramObj, String httpMethod, HttpServletRequest request){
        Response resp = null;
        try{
            Map<String, String> headersMap = new HashMap<>();
//            headersMap.put("Content-Type", "application/json;charset=utf-8");
//	            传入权限的SessionConstants.SESSION_ID(值为SessionId，需要注意和权限系统同步，毕竟框架不依赖权限系统)
            if(request != null) {
                Enumeration<String> enumeration = request.getHeaderNames();
//		            param.put("SessionId", request.getHeader("SessionId"));
                while(enumeration.hasMoreElements()) {
                    String key = enumeration.nextElement();
                    if("Accept-Encoding".equalsIgnoreCase(key.trim())) {
                        continue;
                    }
                    headersMap.put(key, request.getHeader(key));
                }
            }

            if("POST".equalsIgnoreCase(httpMethod)){
                JSONObject paramJo = (JSONObject)JSONObject.toJSON(paramObj);
//                FormBody.Builder builder = new FormBody.Builder();
//                if (jo!=null&&!jo.isEmpty()){
//                    for(Map.Entry<String,Object> entry:jo.entrySet()){
//                        String value = entry.getValue() == null ? null : entry.getValue().toString();
//                        builder.add(entry.getKey(),value);
//                    }
//                }
//                RequestBody requestBody = builder.build();
//                String json = paramObj instanceof String ? (String)paramObj : JSON.toJSONString(paramObj);

                if(StringUtils.isBlank(contentType) || contentType.indexOf(CONTENT_TYPE_FORM) >= 0) {
                    //构建查询参数，主要是为了处理metadata信息以及其它类型的值转为String
                    Map<String, String> param = new HashMap<>();
                    if (paramJo!=null&&!paramJo.isEmpty()){
                        for(Map.Entry<String,Object> entry:paramJo.entrySet()){
                            if(entry.getValue() instanceof JSONObject){
                                JSONObject valueJo = (JSONObject)entry.getValue();
                                //解决spring mvc参数注入@ModelAttribute Domain domain时，metadata作为Map类型的注入问题
                                for(Map.Entry<String,Object> tmpEntry:valueJo.entrySet()) {
                                    if(tmpEntry.getValue() == null) {
                                        continue;
                                    }
                                    param.put(entry.getKey() + "[" +tmpEntry.getKey()+"]", tmpEntry.getValue().toString());
                                }
                            }else {
                                //避免为空(null)的值(value)在okhttp调用时报错，这里就不传入了
                                if(entry.getValue() == null) {
                                    continue;
                                }
//                            String value = entry.getValue() == null ? null : entry.getValue().toString();
                                param.put(entry.getKey(),entry.getValue().toString());
                            }
                        }
                    }
                    resp = OkHttpUtils
                            .post().headers(headersMap)
                            .url(url).params(param)
//                        .mediaType(MediaType.parse("application/json; charset=utf-8"))
//                            .mediaType(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"))
                            .build()
                            //3小时过期
                            .connTimeOut(1000L * 60L * 60L * 3)
                            .readTimeOut(1000L * 60L * 60L * 3)
                            .writeTimeOut(1000L * 60L * 60L * 3)
                            .execute();
                }else if(contentType.indexOf(CONTENT_TYPE_JSON) >= 0){
                    resp = OkHttpUtils
                            .postString().headers(headersMap)
                            .url(url).content(paramJo.toJSONString())
                            .mediaType(MediaType.parse("application/json; charset=utf-8"))
//                        .mediaType(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"))
                            .build()
                            //3小时过期
                            .connTimeOut(1000L * 60L * 60L * 3)
                            .readTimeOut(1000L * 60L * 60L * 3)
                            .writeTimeOut(1000L * 60L * 60L * 3)
                            .execute();
                }else{
                    log.error(String.format("不支持的contentType[%s]", contentType));
                    return null;
                }
            }else{ //GET方式
                resp = OkHttpUtils
                        .get().headers(headersMap)
                        .url(url).params((Map)JSON.toJSON(paramObj))
                        .build()
                        //3小时过期
                        .connTimeOut(1000L*60L*60L*3)
                        .readTimeOut(1000L*60L*60L*3)
                        .writeTimeOut(1000L*60L*60L*3)
                        .execute();
            }
            if(resp.isSuccessful()){
                log.info(String.format("远程调用[%s]成功,code:[%s]", url, resp.code()));
                return resp.body().string();
//                String retVal = new String(resp.body().bytes(), "UTF-8");
//                log.info("==============================================================");
//	            log.info("远程调用结果:"+retVal);
//                log.info("==============================================================");
            }else{
                log.error(String.format("远程调用["+url+"]发生失败,code:[%s], message:[%s]", resp.code(),resp.message()));
                return resp.body().string();
            }
        } catch (Exception e) {
            log.error(String.format("远程调用["+url+"]发生异常,message:[%s]", e.getMessage()), e);
        }
        return null;
    }

    /**
     * 执行导出
     * @param title
     * @param workbook
     * @param response
     */
    private void write(String title, SXSSFWorkbook workbook, HttpServletResponse response){
        if (workbook != null) {
            try {
                String fileNameDownload = title + ".xlsx";

                response.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(fileNameDownload, "utf-8") + "\"");
                //this.getResponse().setContentType("application/x-xls;charset=UTF-8");
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                OutputStream out = response.getOutputStream();
                workbook.write(out);
                out.close();
//                    OutputStream out = response.getOutputStream();
//                    response.reset();
//                    response.setHeader("Content-disposition", "attachment; filename=details.xls");
//                    response.setContentType("application/msexcel");
//                    workbook.write(out);
//                    out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * 列头单元格样式
     */
    private CellStyle getHeaderColumnStyle(SXSSFWorkbook workbook) {
        // 设置字体
        Font font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short) 11);
        //字体加粗
        font.setBold(true);
        //字体颜色
        font.setColor(IndexedColors.ROYAL_BLUE.index);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        CellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(IndexedColors.BLACK.index);
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(IndexedColors.BLACK.index);
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(IndexedColors.BLACK.index);
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(IndexedColors.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置前景色样式
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //设置前景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /*
     * 列数据信息单元格样式
     */
    private CellStyle getDataColumnStyle(SXSSFWorkbook workbook) {
        // 设置字体
        Font font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short)10);
        //字体加粗
        //font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        CellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(IndexedColors.BLACK.index);
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(IndexedColors.BLACK.index);
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(IndexedColors.BLACK.index);
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(IndexedColors.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
